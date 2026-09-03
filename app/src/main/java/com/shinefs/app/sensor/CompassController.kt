package com.shinefs.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.shinefs.core.compass.CompassEngine
import com.shinefs.core.compass.CompassState
import com.shinefs.core.compass.SensorAccuracy
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
class CompassController(context: Context) {

    data class UiState(
        val capability: CompassCapability,
        val compass: CompassState = CompassState(),
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

    private val _state = MutableStateFlow(UiState(capability = capability))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var haveGravity = false
    private var haveGeomagnetic = false

    private var started = false

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
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
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            engine.onAccuracy(SensorAccuracy.fromAndroidValue(accuracy))
            publish()
        }
    }

    @Synchronized
    fun start() {
        if (started) return
        started = true
        haveGravity = false
        haveGeomagnetic = false
        val delay = SensorManager.SENSOR_DELAY_GAME
        rotationVectorSensor?.let { sensorManager.registerListener(listener, it, delay) }
        if (rotationVectorSensor == null) {
            accelerometerSensor?.let { sensorManager.registerListener(listener, it, delay) }
        }
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
        _state.value = UiState(capability = capability)
    }

    private fun tryFallbackIfReady() {
        if (rotationVectorSensor != null) return
        if (!haveGravity || !haveGeomagnetic) return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            pushOrientation()
        }
    }

    /**
     * 以"竖持罗盘"姿态计算方位：remap 到屏幕坐标系（AXIS_X/AXIS_Z），
     * 手机正对前方自然手持时方位角有效；pitch/roll 为该姿态下的倾角。
     */
    private fun pushOrientation() {
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            remappedMatrix,
        )
        SensorManager.getOrientation(remappedMatrix, orientation)
        val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
        engine.onAzimuth(azimuthDeg, pitchDeg, rollDeg)
        publish()
    }

    private fun publish() {
        _state.value = _state.value.copy(compass = engine.state)
    }
}
