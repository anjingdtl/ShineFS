package com.shinefs.core.compass

import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.compass.pose.HoldPoseState
import org.junit.Assert.assertEquals
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

    @Test
    fun `动态引导按磁扰姿态稳定精度和通过状态给出具体动作`() {
        val blockedCompass = compass(interference = true, stability = StabilityLevel.FAIR)
        val blockedReadiness = PreCastReadinessEvaluator.evaluate(
            blockedCompass,
            HoldPoseState(pose = HoldPose.TRANSITION),
        )
        assertTrue(
            PreCastGuidanceResolver.resolve(
                blockedCompass,
                HoldPoseState(pose = HoldPose.TRANSITION),
                blockedReadiness,
            ).headline.contains("磁场"),
        )

        val transition = HoldPoseState(pose = HoldPose.TRANSITION)
        val transitionReadiness = PreCastReadinessEvaluator.evaluate(compass(), transition)
        val transitionGuidance = PreCastGuidanceResolver.resolve(compass(), transition, transitionReadiness)
        assertTrue(transitionGuidance.headline.contains("持握"))

        val unstableCompass = compass(stability = StabilityLevel.FAIR)
        val unstableReadiness = PreCastReadinessEvaluator.evaluate(unstableCompass, flat)
        assertEquals("请保持稳定", PreCastGuidanceResolver.resolve(unstableCompass, flat, unstableReadiness).headline)

        val ready = PreCastReadinessEvaluator.evaluate(compass(), flat)
        val readyGuidance = PreCastGuidanceResolver.resolve(compass(), flat, ready)
        assertTrue(readyGuidance.ready)
        assertTrue(readyGuidance.headline.contains("通过"))
    }
}
