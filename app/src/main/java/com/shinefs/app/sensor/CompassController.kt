package com.shinefs.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import com.shinefs.core.compass.CompassEngine
import com.shinefs.core.compass.CompassState
import com.shinefs.core.compass.PreCastReadiness
import com.shinefs.core.compass.PreCastReadinessEvaluator
import com.shinefs.core.compass.SensorAccuracy
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.compass.pose.HoldPoseDetector
import com.shinefs.core.compass.pose.HoldPoseState
import com.shinefs.core.compass.snapshot.LockedCompassSnapshot
import com.shinefs.core.compass.orientation.FlatOrientationResolver
import com.shinefs.core.compass.orientation.OrientationMath
import com.shinefs.core.compass.orientation.OrientationResolver
import com.shinefs.core.compass.orientation.UprightOrientationResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Android 传感器接线层（薄层，无 UI 依赖）：
 * Rotation Vector 优先，磁力计+加速度计回退；磁力计始终监听（供磁场干扰检测）。
 *
 * 生命周期契约：[start]/[stop] 必须成对调用（Compose 端用 DisposableEffect 保证）；
 * stop 注销全部 listener 并重置引擎，重新 start 可完整恢复。
 */
class CompassController(
    context: Context,
    private val displayRotationProvider: DisplayRotationProvider = AndroidDisplayRotationProvider(context),
) {

    data class UiState(
        val capability: CompassCapability,
        val compass: CompassState = CompassState(),
        val holdPose: HoldPoseState = HoldPoseState(),
        val displayRotation: Int = 0,
        val readiness: PreCastReadiness = PreCastReadiness(),
        val running: Boolean = false,
    ) {
        val calibrationRecommended: Boolean
            get() = compass.accuracy == SensorAccuracy.UNRELIABLE
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val capability: CompassCapability = CompassCapability.of(
        hasRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null,
        hasMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null,
        hasAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null,
    )

    private val engine = CompassEngine()
    private val holdPoseDetector = HoldPoseDetector()

    private val _state = MutableStateFlow(UiState(capability = capability))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val flatOrientationResolver: OrientationResolver = FlatOrientationResolver()
    private val uprightOrientationResolver: OrientationResolver = UprightOrientationResolver()

    private val rotationMatrix = FloatArray(9)
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var haveGravity = false
    private var haveGeomagnetic = false
    private var haveRotationMatrix = false

    private var holdPoseState = HoldPoseState()
    private var displayRotation = 0

    private var started = false

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    haveRotationMatrix = true
                    pushOrientation()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    geomagnetic = event.values.copyOf()
                    haveGeomagnetic = true
                    engine.onMagneticMagnitudeUt(
                        sqrt(
                            event.values[0] * event.values[0] +
                                event.values[1] * event.values[1] +
                                event.values[2] * event.values[2],
                        ),
                    )
                    tryFallbackIfReady()
                    publish()
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    gravity = event.values.copyOf()
                    haveGravity = true
                    tryFallbackIfReady()
                    if (rotationVectorSensor != null && haveRotationMatrix) pushOrientation()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 精度分离（V2.0 方案 §6.6-2）：按传感器类型路由，互不覆盖
            when (sensor?.type) {
                Sensor.TYPE_MAGNETIC_FIELD ->
                    engine.onAccuracy(SensorAccuracy.fromAndroidValue(accuracy), CompassEngine.AccuracySource.MAGNETIC)
                else ->
                    engine.onAccuracy(SensorAccuracy.fromAndroidValue(accuracy), CompassEngine.AccuracySource.ORIENTATION)
            }
            publish()
        }
    }

    @Synchronized
    fun start() {
        if (started) return
        started = true
        haveGravity = false
        haveGeomagnetic = false
        haveRotationMatrix = false
        holdPoseDetector.reset()
        holdPoseState = HoldPoseState()
        displayRotation = 0
        val delay = SensorManager.SENSOR_DELAY_GAME
        rotationVectorSensor?.let { sensorManager.registerListener(listener, it, delay) }
        accelerometerSensor?.let { sensorManager.registerListener(listener, it, delay) }
        // 磁力计始终监听：磁场干扰检测依赖它（Rotation Vector 模式下同样需要）
        magneticSensor?.let { sensorManager.registerListener(listener, it, delay) }
        _state.value = _state.value.copy(running = true)
    }

    @Synchronized
    fun stop() {
        if (!started) return
        started = false
        sensorManager.unregisterListener(listener)
        engine.reset()
        holdPoseDetector.reset()
        holdPoseState = HoldPoseState()
        haveRotationMatrix = false
        _state.value = UiState(capability = capability)
    }

    private fun tryFallbackIfReady() {
        if (rotationVectorSensor != null) return
        if (!haveGravity || !haveGeomagnetic) return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            haveRotationMatrix = true
            pushOrientation()
        }
    }

    /**
     * 先读取显示旋转补偿后的物理顶部，再由 HoldPose 选择平放/竖持 resolver。
     * 两个 resolver 都计算“手机顶部的水平投影”，因此同一方向换姿态不会凭空多出
     * 90°/180° 偏移；屏幕法向量只用于姿态判断，不冒充测量方向。
     */
    private fun pushOrientation() {
        val rotation = displayRotationProvider.currentRotation()
        val probe = OrientationMath.resolve(rotationMatrix, rotation) ?: run {
            holdPoseState = holdPoseDetector.update(
                pitchDeg = Float.NaN,
                rollDeg = Float.NaN,
                nowElapsedMillis = SystemClock.elapsedRealtime(),
            )
            displayRotation = rotation
            publish()
            return
        }
        val gravityMagnitude = if (haveGravity) {
            sqrt(
                gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2],
            )
        } else {
            null
        }
        holdPoseState = holdPoseDetector.update(
            pitchDeg = probe.pitchDeg,
            rollDeg = probe.rollDeg,
            nowElapsedMillis = SystemClock.elapsedRealtime(),
            screenNormalVerticalComponent = probe.screenNormalVerticalComponent,
            gravityMagnitude = gravityMagnitude,
        )
        val resolver = when (holdPoseState.pose) {
            HoldPose.UPRIGHT -> uprightOrientationResolver
            else -> flatOrientationResolver
        }
        val resolved = resolver.resolve(rotationMatrix, rotation) ?: probe
        val poseInvalid = holdPoseState.pose == HoldPose.INVALID
        engine.onAzimuth(
            rawAzimuthDeg = resolved.azimuthDeg,
            pitchDeg = resolved.pitchDeg,
            rollDeg = resolved.rollDeg,
            tooTiltedOverride = poseInvalid,
        )
        displayRotation = rotation
        publish()
    }

    private fun publish() {
        _state.value = _state.value.copy(
            compass = engine.state,
            holdPose = holdPoseState,
            displayRotation = displayRotation,
            readiness = PreCastReadinessEvaluator.evaluate(engine.state, holdPoseState),
        )
    }

    /** 定盘瞬间复制当前传感器状态；返回 null 表示当前没有可保存的方位 fix。 */
    fun captureSnapshot(
        capturedAt: Long,
        northReference: NorthReference,
        facingMountain: String?,
        sittingMountain: String?,
        directionTrigram: String?,
    ): LockedCompassSnapshot? = CompassSnapshotFactory.fromCurrentState(
        capturedAt = capturedAt,
        compass = _state.value.compass,
        holdPose = _state.value.holdPose,
        displayRotation = _state.value.displayRotation,
        northReference = northReference,
        facingMountain = facingMountain,
        sittingMountain = sittingMountain,
        directionTrigram = directionTrigram,
    )
}
