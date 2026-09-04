package com.shinefs.core.divination.context

import com.shinefs.core.compass.CompassState
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.SensorAccuracy
import com.shinefs.core.compass.SensorAccuracyState
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.compass.snapshot.LockedCompassSnapshot
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.rules.LaterHeavenBagua
import com.shinefs.core.yijing.rules.Mountains24

/**
 * 时空融合装配（V2.0 方案 §7/§33-10G）：罗盘引擎状态 → 空间上下文。
 *
 * 锚定方向约定：手机顶部所指为"向"（facing），平滑角为输入（`orientation-v1`）。
 * 北参考默认磁北（TD-V2-08）。
 */
object YijingSpaceContextFactory {

    /** V2.1：直接把定盘瞬间的不可变快照映射为空间上下文。 */
    fun fromLockedCompassSnapshot(
        snapshot: LockedCompassSnapshot,
    ): YijingSpaceContext? {
        val azimuth = snapshot.smoothedAzimuth ?: return null
        val sitting = (azimuth + 180f) % 360f
        return YijingSpaceContext(
            rawAzimuth = snapshot.rawAzimuth,
            smoothedAzimuth = azimuth,
            northReference = snapshot.northReference,
            facingMountain = snapshot.facingMountain ?: Mountains24.mountainAt(azimuth),
            sittingMountain = snapshot.sittingMountain ?: Mountains24.mountainAt(sitting),
            directionTrigram = LaterHeavenBagua.trigramAt(azimuth),
            sensorAccuracy = SensorAccuracyState(
                snapshot.orientationAccuracy,
                snapshot.magneticAccuracy,
            ),
            stable = snapshot.stability == StabilityLevel.GOOD,
            magneticInterference = snapshot.magneticInterference,
            holdPose = snapshot.holdPose,
            holdPoseConfidence = snapshot.holdPoseConfidence,
            poseStableMillis = snapshot.poseStableMillis,
            pitchDeg = snapshot.pitchDeg,
            rollDeg = snapshot.rollDeg,
            stabilityStdDeg = snapshot.stabilityStdDeg,
            magneticMagnitudeUt = snapshot.magneticMagnitudeUt,
            snapshotCapturedAt = snapshot.capturedAt,
        )
    }

    /**
     * V2 旧导航对象适配器：只把已有读数映射为空间事实，不创建 CompassEngine、
     * 不重复喂入角度，也不声称这些字段是新的真实快照。V2.1 UI 将只传入快照。
     */
    fun fromLegacyReading(
        rawAzimuth: Float?,
        smoothedAzimuth: Float?,
        facingMountain: String?,
        sittingMountain: String?,
        facingTrigram: String?,
        stable: Boolean,
        orientationAccuracy: String?,
        magneticAccuracy: String?,
        northReference: NorthReference = NorthReference.MAGNETIC,
    ): YijingSpaceContext? {
        val azimuth = smoothedAzimuth ?: return null
        return YijingSpaceContext(
            rawAzimuth = rawAzimuth,
            smoothedAzimuth = azimuth,
            northReference = northReference,
            facingMountain = facingMountain,
            sittingMountain = sittingMountain,
            directionTrigram = LaterHeavenBagua.trigramAt(azimuth),
            sensorAccuracy = SensorAccuracyState(
                orientationAccuracy = accuracyOf(orientationAccuracy),
                magneticAccuracy = accuracyOf(magneticAccuracy),
            ),
            stable = stable,
            magneticInterference = false,
        )
    }

    fun fromCompassState(
        state: CompassState,
        northReference: NorthReference = NorthReference.MAGNETIC,
    ): YijingSpaceContext? {
        val azimuth = state.smoothedAzimuth ?: return null
        val sitting = (azimuth + 180f) % 360f
        return YijingSpaceContext(
            rawAzimuth = state.rawAzimuth,
            smoothedAzimuth = azimuth,
            northReference = northReference,
            facingMountain = Mountains24.mountainAt(azimuth),
            sittingMountain = Mountains24.mountainAt(sitting),
            directionTrigram = LaterHeavenBagua.trigramAt(azimuth),
            sensorAccuracy = state.accuracyState,
            stable = state.stability == StabilityLevel.GOOD,
            magneticInterference = state.magneticInterference,
        )
}

    /** 便捷重载：直接取 Trigram（供展示层）。 */
    fun trigramAt(azimuth: Float): Trigram = LaterHeavenBagua.trigramAt(azimuth)

    private fun accuracyOf(label: String?): SensorAccuracy =
        SensorAccuracy.entries.firstOrNull { it.label == label } ?: SensorAccuracy.NO_CONTACT
}
