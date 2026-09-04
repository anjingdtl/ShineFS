package com.shinefs.core.divination

import com.shinefs.core.compass.CompassEngine
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.SensorAccuracy
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.compass.snapshot.LockedCompassSnapshot
import com.shinefs.core.divination.context.YijingSpaceContextFactory
import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YijingSpaceContextFactoryTest {

    @Test
    fun `罗盘状态到空间上下文全字段`() {
        val engine = CompassEngine()
        engine.onAccuracy(SensorAccuracy.HIGH, CompassEngine.AccuracySource.ORIENTATION)
        engine.onAccuracy(SensorAccuracy.MEDIUM, CompassEngine.AccuracySource.MAGNETIC)
        repeat(40) { engine.onAzimuth(182.4f, 0f, 0f) }
        val ctx = YijingSpaceContextFactory.fromCompassState(engine.state)

        assertNotNull(ctx)
        assertEquals(182.4f, ctx!!.smoothedAzimuth!!, 0.5f)
        assertEquals("午", ctx.facingMountain)
        assertEquals("子", ctx.sittingMountain)
        assertEquals(Trigram.LI, ctx.directionTrigram)
        assertTrue(ctx.stable)
        assertTrue(!ctx.magneticInterference)
        assertEquals(SensorAccuracy.HIGH, ctx.sensorAccuracy!!.orientationAccuracy)
        assertEquals(SensorAccuracy.MEDIUM, ctx.sensorAccuracy!!.magneticAccuracy)
    }

    @Test
    fun `无定位返回 null 不伪造方向`() {
        assertNull(YijingSpaceContextFactory.fromCompassState(CompassEngine().state))
    }

    @Test
    fun `跨零方位`() {
        val engine = CompassEngine()
        repeat(40) { engine.onAzimuth(359.5f, 0f, 0f) }
        val ctx = YijingSpaceContextFactory.fromCompassState(engine.state)!!
        assertEquals("子", ctx.facingMountain) // 359.5° ∈ [352.5, 360) → 子
        assertEquals("午", ctx.sittingMountain)
        assertEquals(Trigram.KAN, ctx.directionTrigram) // 北
    }

    @Test
    fun `真实定盘快照字段原样进入空间上下文`() {
        val snapshot = LockedCompassSnapshot(
            capturedAt = 77L,
            rawAzimuth = 181.8f,
            smoothedAzimuth = 182.4f,
            pitchDeg = 6.2f,
            rollDeg = -1.3f,
            holdPose = HoldPose.UPRIGHT,
            holdPoseConfidence = 0.8f,
            poseStableMillis = 900L,
            stability = StabilityLevel.GOOD,
            stabilityStdDeg = 0.3f,
            orientationAccuracy = SensorAccuracy.HIGH,
            magneticAccuracy = SensorAccuracy.MEDIUM,
            magneticMagnitudeUt = 51.2f,
            magneticInterference = false,
            northReference = NorthReference.MAGNETIC,
            displayRotation = 1,
            facingMountain = "午",
            sittingMountain = "子",
            directionTrigram = "离",
        )
        val context = YijingSpaceContextFactory.fromLockedCompassSnapshot(snapshot)!!

        assertEquals(snapshot.capturedAt, context.snapshotCapturedAt)
        assertEquals(snapshot.rawAzimuth, context.rawAzimuth)
        assertEquals(snapshot.smoothedAzimuth, context.smoothedAzimuth)
        assertEquals(snapshot.holdPose, context.holdPose)
        assertEquals(snapshot.holdPoseConfidence, context.holdPoseConfidence, 0.001f)
        assertEquals(snapshot.poseStableMillis, context.poseStableMillis)
        assertEquals(snapshot.pitchDeg, context.pitchDeg)
        assertEquals(snapshot.rollDeg, context.rollDeg)
        assertEquals(snapshot.stabilityStdDeg, context.stabilityStdDeg)
        assertEquals(snapshot.magneticMagnitudeUt, context.magneticMagnitudeUt)
        assertEquals(SensorAccuracy.HIGH, context.sensorAccuracy!!.orientationAccuracy)
        assertEquals(SensorAccuracy.MEDIUM, context.sensorAccuracy.magneticAccuracy)
    }
}
