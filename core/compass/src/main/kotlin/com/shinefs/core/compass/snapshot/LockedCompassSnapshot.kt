package com.shinefs.core.compass.snapshot

import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.SensorAccuracy
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.compass.pose.HoldPose

/**
 * 用户点击定盘时对当前状态的一次不可变复制。
 * capturedAt 是 wall-clock epoch millis；稳定计时本身由 poseStableMillis 留存。
 */
data class LockedCompassSnapshot(
    val capturedAt: Long,
    val rawAzimuth: Float?,
    val smoothedAzimuth: Float?,
    val pitchDeg: Float?,
    val rollDeg: Float?,
    val holdPose: HoldPose,
    val holdPoseConfidence: Float,
    val poseStableMillis: Long,
    val stability: StabilityLevel,
    val stabilityStdDeg: Float?,
    val orientationAccuracy: SensorAccuracy,
    val magneticAccuracy: SensorAccuracy,
    val magneticMagnitudeUt: Float?,
    val magneticInterference: Boolean,
    val northReference: NorthReference = NorthReference.MAGNETIC,
    val displayRotation: Int = 0,
    val facingMountain: String? = null,
    val sittingMountain: String? = null,
    val directionTrigram: String? = null,
    val samples: Int = 0,
    val glitchSuppressed: Int = 0,
) {
    /** 同一字段的语义别名，明确它是一个时间 instant。 */
    val instant: Long get() = capturedAt
}
