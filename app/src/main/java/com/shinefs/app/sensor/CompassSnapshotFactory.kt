package com.shinefs.app.sensor

import com.shinefs.core.compass.CompassState
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.pose.HoldPoseState
import com.shinefs.core.compass.snapshot.LockedCompassSnapshot

/** 从当前状态做一次字段级复制；不重新创建引擎、不重复喂入任何传感器样本。 */
object CompassSnapshotFactory {
    fun fromCurrentState(
        capturedAt: Long,
        compass: CompassState,
        holdPose: HoldPoseState,
        displayRotation: Int,
        northReference: NorthReference,
        facingMountain: String?,
        sittingMountain: String?,
        directionTrigram: String?,
    ): LockedCompassSnapshot? {
        if (compass.smoothedAzimuth == null) return null
        return LockedCompassSnapshot(
            capturedAt = capturedAt,
            rawAzimuth = compass.rawAzimuth,
            smoothedAzimuth = compass.smoothedAzimuth,
            pitchDeg = compass.pitchDeg,
            rollDeg = compass.rollDeg,
            holdPose = holdPose.pose,
            holdPoseConfidence = holdPose.confidence,
            poseStableMillis = holdPose.stableMillis,
            stability = compass.stability,
            stabilityStdDeg = compass.stabilityStdDeg.takeUnless { it.isNaN() },
            orientationAccuracy = compass.orientationAccuracy,
            magneticAccuracy = compass.magneticAccuracy,
            magneticMagnitudeUt = compass.magneticMagnitudeUt,
            magneticInterference = compass.magneticInterference,
            northReference = northReference,
            displayRotation = displayRotation,
            facingMountain = facingMountain,
            sittingMountain = sittingMountain,
            directionTrigram = directionTrigram,
            samples = compass.samples,
            glitchSuppressed = compass.glitchSuppressed,
        )
    }
}
