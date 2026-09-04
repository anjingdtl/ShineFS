package com.shinefs.app.sensor

import com.shinefs.core.compass.CompassEngine
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.SensorAccuracy
import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.compass.pose.HoldPoseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LockedCompassSnapshotTest {
    @Test
    fun `快照逐字段复制定盘瞬间真实状态`() {
        val engine = CompassEngine()
        engine.onAccuracy(SensorAccuracy.HIGH, CompassEngine.AccuracySource.ORIENTATION)
        engine.onAccuracy(SensorAccuracy.MEDIUM, CompassEngine.AccuracySource.MAGNETIC)
        engine.onMagneticMagnitudeUt(47.3f)
        repeat(40) { engine.onAzimuth(182.4f + if (it == 0) 0.2f else 0f, 4.5f, -2.3f) }
        val pose = HoldPoseState(
            pose = HoldPose.FLAT,
            confidence = 0.91f,
            pitchDeg = 4.5f,
            rollDeg = -2.3f,
            stableMillis = 1_234L,
        )

        val snapshot = CompassSnapshotFactory.fromCurrentState(
            capturedAt = 123_456_789L,
            compass = engine.state,
            holdPose = pose,
            displayRotation = 2,
            northReference = NorthReference.MAGNETIC,
            facingMountain = "午",
            sittingMountain = "子",
            directionTrigram = "离",
        )

        assertNotNull(snapshot)
        snapshot!!
        assertEquals(123_456_789L, snapshot.capturedAt)
        assertEquals(engine.state.rawAzimuth, snapshot.rawAzimuth)
        assertEquals(engine.state.smoothedAzimuth, snapshot.smoothedAzimuth)
        assertEquals(4.5f, snapshot.pitchDeg)
        assertEquals(-2.3f, snapshot.rollDeg)
        assertEquals(HoldPose.FLAT, snapshot.holdPose)
        assertEquals(0.91f, snapshot.holdPoseConfidence, 0.0001f)
        assertEquals(1_234L, snapshot.poseStableMillis)
        assertEquals(engine.state.stabilityStdDeg, snapshot.stabilityStdDeg)
        assertEquals(SensorAccuracy.HIGH, snapshot.orientationAccuracy)
        assertEquals(SensorAccuracy.MEDIUM, snapshot.magneticAccuracy)
        assertEquals(47.3f, snapshot.magneticMagnitudeUt)
        assertFalse(snapshot.magneticInterference)
        assertEquals(2, snapshot.displayRotation)
        assertEquals("午", snapshot.facingMountain)
        assertEquals("子", snapshot.sittingMountain)
        assertEquals("离", snapshot.directionTrigram)
        assertTrue(snapshot.instant == snapshot.capturedAt)
    }
}
