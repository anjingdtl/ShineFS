package com.shinefs.core.compass

import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.compass.pose.HoldPoseState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreCastReadinessTest {
    private fun compass(
        interference: Boolean = false,
        stability: StabilityLevel = StabilityLevel.GOOD,
        orientation: SensorAccuracy = SensorAccuracy.MEDIUM,
        magnetic: SensorAccuracy = SensorAccuracy.MEDIUM,
    ) = CompassState(
        samples = 40,
        rawAzimuth = 90f,
        smoothedAzimuth = 90f,
        stability = stability,
        orientationAccuracy = orientation,
        magneticAccuracy = magnetic,
        magneticInterference = interference,
        magneticMagnitudeUt = 45f,
    )

    private val flat = HoldPoseState(
        pose = HoldPose.FLAT,
        confidence = 0.95f,
        pitchDeg = 1f,
        rollDeg = 2f,
        stableMillis = 1_000L,
    )

    @Test
    fun `有效姿态稳定无磁扰且精度足够才ready`() {
        val readiness = PreCastReadinessEvaluator.evaluate(compass(), flat)
        assertTrue(readiness.validPose)
        assertTrue(readiness.stable)
        assertTrue(readiness.magneticOk)
        assertTrue(readiness.sensorAccuracyOk)
        assertTrue(readiness.ready)
        assertTrue(readiness.reasons.isEmpty())
    }

    @Test
    fun `每个阻断原因结构化暴露`() {
        val readiness = PreCastReadinessEvaluator.evaluate(
            compass(interference = true, stability = StabilityLevel.FAIR, orientation = SensorAccuracy.UNRELIABLE),
            HoldPoseState(pose = HoldPose.TRANSITION),
        )
        assertFalse(readiness.ready)
        assertTrue(readiness.reasons.contains("请调整持握姿态"))
        assertTrue(readiness.reasons.contains("请保持稳定"))
        assertTrue(readiness.reasons.contains("磁场干扰"))
        assertTrue(readiness.reasons.contains("传感器精度不足"))
    }
}
