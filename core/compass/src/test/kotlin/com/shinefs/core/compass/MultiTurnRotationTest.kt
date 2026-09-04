package com.shinefs.core.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * 多圈旋转与精度分离专项（V2.0 方案 §6.6，Cycle 10G）：
 * ① shortestDiff 任意量级正确性（含旧实现的负余数回归锚点）
 * ② 连续顺时针 5 圈 / 逆时针 5 圈 / 正反交替引擎行为
 * ③ 盘面累积旋转（UI 最短路径累积逻辑的纯数学复现）
 * ④ 朝向/磁力计精度互不覆盖
 */
class MultiTurnRotationTest {

    // ---------- ① shortestDiff 量级正确性 ----------

    @Test
    fun `旧实现负余数回归锚点`() {
        // 旧式 ((to-from+540)%360)-180 在此输入得 -270（错）；修复后应为 +90
        assertEquals(90f, CircularMath.shortestDiff(-720f, -630f), 1e-4f)
        assertEquals(90f, CircularMath.shortestDiff(720f, 810f), 1e-4f)
        assertEquals(-90f, CircularMath.shortestDiff(-720f, -810f), 1e-4f)
    }

    @Test
    fun `任意量级属性 - 值域与模一致性`() {
        val magnitudes = floatArrayOf(0f, 90f, 179.99f, 180f, 180.01f, 359.99f, 360f, 540f, 719.9f, 1080f, 3600f, 18000f)
        for (from in magnitudes) {
            for (to in magnitudes) {
                val d = CircularMath.shortestDiff(from, to)
                assertTrue("range $from->$to = $d", d >= -180f && d < 180f)
                assertEquals("mod $from->$to", CircularMath.normalize(to - from), CircularMath.normalize(d), 0.001f)
            }
        }
        for (from in magnitudes) {
            for (to in magnitudes) {
                val d = CircularMath.shortestDiff(-from, -to)
                assertTrue("range neg $from->$to = $d", d >= -180f && d < 180f)
                assertEquals("mod neg", CircularMath.normalize(-to + from), CircularMath.normalize(d), 0.001f)
            }
        }
    }

    @Test
    fun `基本跨零场景不回归`() {
        assertEquals(2f, CircularMath.shortestDiff(359f, 1f), 1e-4f)
        assertEquals(-2f, CircularMath.shortestDiff(1f, 359f), 1e-4f)
        assertEquals(-180f, CircularMath.shortestDiff(0f, 180f), 1e-4f)
        assertEquals(179.5f, CircularMath.shortestDiff(0f, 179.5f), 1e-4f)
    }

    // ---------- ② 引擎多圈 ----------

    private fun feedTurns(engine: CompassEngine, startDeg: Float, totalDeg: Float, samples: Int): CompassState {
        var state = engine.state
        for (i in 1..samples) {
            val unnormalized = startDeg + totalDeg * i / samples
            state = engine.onAzimuth(unnormalized, pitchDeg = 0f, rollDeg = 0f)
            val smoothed = state.smoothedAzimuth!!
            assertTrue("smoothed in [0,360): $smoothed", smoothed >= 0f && smoothed < 360f)
            assertTrue("smoothed not NaN", !smoothed.isNaN())
        }
        return state
    }

    @Test
    fun `连续顺时针五圈`() {
        val engine = CompassEngine(alpha = 0.6f)
        val state = feedTurns(engine, startDeg = 10f, totalDeg = 1800f, samples = 300)
        // 终态：10+1800=1810 ≡ 130°；滞后有界（<30°）
        val target = CircularMath.normalize(10f + 1800f)
        val err = abs(CircularMath.shortestDiff(state.smoothedAzimuth!!, target))
        assertTrue("final error $err", err < 30f)
        assertEquals(0, state.glitchSuppressed) // 真实连续旋转不得判为毛刺
    }

    @Test
    fun `连续逆时针五圈`() {
        val engine = CompassEngine(alpha = 0.6f)
        val state = feedTurns(engine, startDeg = 200f, totalDeg = -1800f, samples = 300)
        val target = CircularMath.normalize(200f - 1800f)
        val err = abs(CircularMath.shortestDiff(state.smoothedAzimuth!!, target))
        assertTrue("final error $err", err < 30f)
        assertEquals(0, state.glitchSuppressed)
    }

    @Test
    fun `正反交替五组`() {
        val engine = CompassEngine(alpha = 0.6f)
        var state = engine.state
        var current = 30f
        for (round in 1..5) {
            val dir = if (round % 2 == 1) 1 else -1
            for (i in 1..60) {
                current += dir * 6f
                state = engine.onAzimuth(current, 0f, 0f)
                val s = state.smoothedAzimuth!!
                assertTrue(s >= 0f && s < 360f)
            }
        }
        val target = CircularMath.normalize(current)
        val err = abs(CircularMath.shortestDiff(state.smoothedAzimuth!!, target))
        assertTrue("final error $err", err < 45f)
        assertEquals(0, state.glitchSuppressed)
    }

    @Test
    fun `快速转动不误判毛刺`() {
        val engine = CompassEngine()
        var state = engine.state
        // 建立稳定
        repeat(40) { state = engine.onAzimuth(5f, 0f, 0f) }
        assertEquals(StabilityLevel.GOOD, state.stability)
        // 快速真实转动 60°/样本（约 3000°/s）
        var deg = 5f
        repeat(30) {
            deg += 60f
            state = engine.onAzimuth(deg, 0f, 0f)
        }
        assertEquals("快速连续转动不得抑制", 0, state.glitchSuppressed)
    }

    // ---------- ③ 盘面累积旋转（UI 逻辑数学复现） ----------

    @Test
    fun `盘面累积旋转多圈一致性`() {
        // 复现 CompassDial：targetRotation += shortestDiff(targetRotation, -azimuth)
        var targetRotation = 0f
        var maxStep = 0f
        var azimuth = 0f
        for (i in 1..3600) { // 十圈
            azimuth = CircularMath.normalize(azimuth + 1f)
            val step = CircularMath.shortestDiff(targetRotation, -azimuth)
            targetRotation += step
            maxStep = maxOf(maxStep, abs(step))
        }
        assertTrue("单步不得超过180", maxStep <= 180f)
        // 累积角 ≡ -azimuth (mod 360)
        assertEquals(CircularMath.normalize(-azimuth), CircularMath.normalize(targetRotation), 0.01f)
        // 累积值无爆炸（十圈应约 -3600 量级，而非错误路径漂移）
        assertTrue(abs(targetRotation) < 3700f)
    }

    // ---------- ④ 精度分离 ----------

    @Test
    fun `朝向与磁力计精度互不覆盖`() {
        val engine = CompassEngine()
        engine.onAccuracy(SensorAccuracy.HIGH, CompassEngine.AccuracySource.ORIENTATION)
        engine.onAccuracy(SensorAccuracy.UNRELIABLE, CompassEngine.AccuracySource.MAGNETIC)
        assertEquals(SensorAccuracy.HIGH, engine.state.orientationAccuracy)
        assertEquals(SensorAccuracy.UNRELIABLE, engine.state.magneticAccuracy)
        assertEquals(SensorAccuracy.HIGH, engine.state.accuracy) // 别名 = 朝向精度

        engine.onAccuracy(SensorAccuracy.MEDIUM, CompassEngine.AccuracySource.MAGNETIC)
        assertEquals(SensorAccuracy.HIGH, engine.state.orientationAccuracy)
        assertEquals(SensorAccuracy.MEDIUM, engine.state.magneticAccuracy)

        assertEquals(
            SensorAccuracyState(SensorAccuracy.HIGH, SensorAccuracy.MEDIUM),
            engine.state.accuracyState,
        )
    }
}
