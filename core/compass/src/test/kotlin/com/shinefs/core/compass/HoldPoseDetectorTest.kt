package com.shinefs.core.compass

import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.compass.pose.HoldPoseDetector
import com.shinefs.core.compass.pose.HoldPoseState
import kotlin.math.cos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HoldPoseDetectorTest {

    private fun stableAt(detector: HoldPoseDetector, tiltDeg: Float): HoldPoseState {
        var state = HoldPoseState()
        val normal = cos(Math.toRadians(tiltDeg.toDouble())).toFloat()
        repeat(10) { index ->
            state = detector.update(
                pitchDeg = tiltDeg,
                rollDeg = 0f,
                nowElapsedMillis = index * 100L,
                screenNormalVerticalComponent = normal,
                gravityMagnitude = 9.8f,
            )
        }
        return state
    }

    @Test
    fun `属性阈值覆盖十到八十度`() {
        val expectations = listOf(
            10f to HoldPose.FLAT,
            20f to HoldPose.FLAT,
            30f to HoldPose.TRANSITION,
            45f to HoldPose.TRANSITION,
            60f to HoldPose.TRANSITION,
            70f to HoldPose.UPRIGHT,
            80f to HoldPose.UPRIGHT,
        )
        expectations.forEach { (tilt, expected) ->
            assertEquals("tilt=$tilt", expected, stableAt(HoldPoseDetector(), tilt).pose)
        }
    }

    @Test
    fun `姿态切换有迟滞且需要持续稳定`() {
        val detector = HoldPoseDetector(settleMillis = 800L)
        val flat = stableAt(detector, 10f)
        assertEquals(HoldPose.FLAT, flat.pose)

        val normal25 = cos(Math.toRadians(25.0)).toFloat()
        assertEquals(
            "平放退出阈值以内保持平放",
            HoldPose.FLAT,
            detector.update(25f, 0f, 1_000L, normal25, 9.8f).pose,
        )
        val normal80 = cos(Math.toRadians(80.0)).toFloat()
        assertEquals(HoldPose.TRANSITION, detector.update(80f, 0f, 1_400L, normal80, 9.8f).pose)
        assertEquals(HoldPose.TRANSITION, detector.update(80f, 0f, 2_000L, normal80, 9.8f).pose)
        assertEquals(HoldPose.UPRIGHT, detector.update(80f, 0f, 2_300L, normal80, 9.8f).pose)
        assertTrue(detector.update(80f, 0f, 2_400L, normal80, 9.8f).stableMillis >= 800L)
    }

    @Test
    fun `屏幕朝下和无效重力立即无效`() {
        val detector = HoldPoseDetector()
        val down = detector.update(0f, 0f, 0L, -1f, 9.8f)
        assertEquals(HoldPose.INVALID, down.pose)
        val badGravity = detector.update(0f, 0f, 100L, 1f, 20f)
        assertEquals(HoldPose.INVALID, badGravity.pose)
    }

    @Test
    fun `短时间内剧烈姿态变化标记无效`() {
        val detector = HoldPoseDetector()
        detector.update(0f, 0f, 0L, 1f, 9.8f)
        val state = detector.update(80f, 0f, 100L, cos(Math.toRadians(80.0)).toFloat(), 9.8f)
        assertEquals(HoldPose.INVALID, state.pose)
    }

    @Test
    fun `没有屏幕法向量时回退到pitch与roll合成倾角`() {
        val state = stableAt(HoldPoseDetector(), 10f)
        assertEquals(HoldPose.FLAT, state.pose)

        val detector = HoldPoseDetector()
        var fallback = HoldPoseState()
        repeat(10) { i -> fallback = detector.update(80f, 0f, i * 100L, gravityMagnitude = 9.8f) }
        assertEquals(HoldPose.UPRIGHT, fallback.pose)
    }
}
