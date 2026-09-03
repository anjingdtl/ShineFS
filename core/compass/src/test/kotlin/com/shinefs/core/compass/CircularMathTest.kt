package com.shinefs.core.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CircularMathTest {

    @Test
    fun `normalize 负角与大角`() {
        assertEquals(350f, CircularMath.normalize(-10f))
        assertEquals(10f, CircularMath.normalize(370f))
        assertEquals(0f, CircularMath.normalize(360f))
        assertEquals(0f, CircularMath.normalize(0f))
        assertEquals(359.99f, CircularMath.normalize(359.99f))
    }

    @Test
    fun `shortestDiff 走最短路径`() {
        assertEquals(20f, CircularMath.shortestDiff(350f, 10f))
        assertEquals(-20f, CircularMath.shortestDiff(10f, 350f))
        assertEquals(2f, CircularMath.shortestDiff(359f, 1f))
        assertEquals(-2f, CircularMath.shortestDiff(1f, 359f))
        assertEquals(179f, CircularMath.shortestDiff(0f, 179f))
        assertEquals(-180f, CircularMath.shortestDiff(0f, 180f))
    }

    @Test
    fun `circularMean 跨界均值`() {
        assertEquals(0f, CircularMath.circularMean(listOf(350f, 10f)))
        assertEquals(0f, CircularMath.circularMean(listOf(359f, 1f, 0f)))
        assertEquals(90f, CircularMath.circularMean(listOf(80f, 100f)))
    }

    @Test
    fun `circularStdDeg 恒定序列为零-抖动序列大于零`() {
        assertEquals(0f, CircularMath.circularStdDeg(listOf(90f, 90f, 90f)))
        assertTrue(CircularMath.circularStdDeg(listOf(89f, 91f, 90f, 90.5f)) > 0f)
        // 对径双点集：数学上 R→0 时标准差→∞；浮点下为有限大数，断言充分大即可
        assertTrue(CircularMath.circularStdDeg(listOf(0f, 180f)) > 10f)
    }
}
