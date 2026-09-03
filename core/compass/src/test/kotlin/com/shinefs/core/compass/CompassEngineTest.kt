package com.shinefs.core.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassEngineTest {

    private fun feedStable(engine: CompassEngine, azimuth: Float, times: Int, jitterDeg: Float = 0f) {
        repeat(times) { i ->
            val j = if (jitterDeg == 0f) 0f else (((i % 5) - 2) * jitterDeg)
            engine.onAzimuth(azimuth + j)
        }
    }

    @Test
    fun `首帧直接输出且平滑值等于原始值`() {
        val engine = CompassEngine()
        val s = engine.onAzimuth(123.4f)
        assertEquals(123.4f, s.smoothedAzimuth)
        assertEquals(1, s.samples)
    }

    @Test
    fun `359到1跨界平滑不绕整圈`() {
        val engine = CompassEngine()
        feedStable(engine, 359f, 30)
        assertEquals(StabilityLevel.GOOD, engine.state.stability)
        val before = engine.state.smoothedAzimuth!!
        val s = engine.onAzimuth(1f)
        val moved = CircularMath.shortestDiff(before, s.smoothedAzimuth!!)
        assertTrue("应沿最短路径前进小幅移动，实际移动 $moved", moved in 0.1f..2f)
        // 平滑值应停留在跨 0° 小邻域，而非跑到对侧
        val a = s.smoothedAzimuth!!
        assertTrue(a > 358f || a < 2f)
    }

    @Test
    fun `连续快速旋转跨0度仍走最短路径`() {
        val engine = CompassEngine()
        var az = 300f
        repeat(60) {
            az = (az + 25f) % 360f
            engine.onAzimuth(az)
        }
        val s = engine.state
        assertTrue(s.hasFix)
        assertTrue(s.smoothedAzimuth!! in 0f..359.99f)
    }

    @Test
    fun `微小抖动达到良好-中等抖动不到良好`() {
        val fine = CompassEngine()
        feedStable(fine, 90f, 40, jitterDeg = 0.05f)
        assertEquals(StabilityLevel.GOOD, fine.state.stability)

        val coarse = CompassEngine()
        feedStable(coarse, 90f, 40, jitterDeg = 3f)
        assertNotEquals(StabilityLevel.GOOD, coarse.state.stability)
    }

    @Test
    fun `样本不足时为不稳定`() {
        val engine = CompassEngine()
        repeat(5) { engine.onAzimuth(90f) }
        assertEquals(StabilityLevel.UNSTABLE, engine.state.stability)
    }

    @Test
    fun `稳定后的单点突跳被抑制`() {
        val engine = CompassEngine()
        feedStable(engine, 90f, 40)
        val before = engine.state.smoothedAzimuth!!
        val s = engine.onAzimuth(270f)
        assertEquals("毛刺应被丢弃", before, s.smoothedAzimuth!!, 0.001f)
        assertEquals(1, s.glitchSuppressed)
    }

    @Test
    fun `未稳定期不启用毛刺抑制`() {
        val engine = CompassEngine()
        engine.onAzimuth(0f)
        val s = engine.onAzimuth(178f) // 未达 GOOD，不判毛刺
        assertEquals(0, s.glitchSuppressed)
        assertNotEquals(0f, s.smoothedAzimuth!!)
    }

    @Test
    fun `磁场异常与恢复正常`() {
        val engine = CompassEngine()
        engine.onMagneticMagnitudeUt(45f)
        assertFalse(engine.state.magneticInterference)
        engine.onMagneticMagnitudeUt(150f)
        assertTrue(engine.state.magneticInterference)
        engine.onMagneticMagnitudeUt(30f)
        assertFalse(engine.state.magneticInterference)
    }

    @Test
    fun `倾斜超限标记`() {
        val engine = CompassEngine()
        engine.onAzimuth(10f, pitchDeg = 50f, rollDeg = 0f)
        assertTrue(engine.state.tooTilted)
        engine.onAzimuth(10f, pitchDeg = 5f, rollDeg = 3f)
        assertFalse(engine.state.tooTilted)
    }

    @Test
    fun `精度映射与reset`() {
        val engine = CompassEngine()
        engine.onAccuracy(SensorAccuracy.fromAndroidValue(3))
        assertEquals(SensorAccuracy.HIGH, engine.state.accuracy)
        assertEquals(SensorAccuracy.UNRELIABLE, SensorAccuracy.fromAndroidValue(0))
        engine.onAzimuth(10f)
        engine.reset()
        assertEquals(0, engine.state.samples)
        assertEquals(null, engine.state.smoothedAzimuth)
    }
}
