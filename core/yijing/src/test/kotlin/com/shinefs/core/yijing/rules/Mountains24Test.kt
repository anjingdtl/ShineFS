package com.shinefs.core.yijing.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Mountains24Test {

    @Test
    fun `山序与方案3_2完全一致`() {
        assertEquals(
            listOf(
                "子", "癸", "丑", "艮", "寅", "甲",
                "卯", "乙", "辰", "巽", "巳", "丙",
                "午", "丁", "未", "坤", "申", "庚",
                "酉", "辛", "戌", "乾", "亥", "壬",
            ),
            Mountains24.names,
        )
        assertEquals(24, Mountains24.names.toSet().size)
    }

    @Test
    fun `每山中心角为其下标乘15度`() {
        for (i in Mountains24.names.indices) {
            assertEquals(i * 15f, Mountains24.centerAngleOf(i))
        }
    }

    @Test
    fun `中心角正对所属山`() {
        for (i in Mountains24.names.indices) {
            assertEquals(
                Mountains24.names[i],
                Mountains24.mountainAt(Mountains24.centerAngleOf(i)),
            )
        }
    }

    @Test
    fun `每山下边界含入本山`() {
        for (i in Mountains24.names.indices) {
            assertEquals(
                Mountains24.names[i],
                Mountains24.mountainAt(Mountains24.lowerBoundOf(i)),
            )
        }
    }

    @Test
    fun `每山上边界归入下一山-跨0度`() {
        for (i in Mountains24.names.indices) {
            val next = Mountains24.names[(i + 1) % 24]
            assertEquals(next, Mountains24.mountainAt(Mountains24.upperBoundOf(i)))
        }
    }

    /** 方案 §3.2 与 §12.1 指定的临界角。 */
    @Test
    fun `方案指定临界角`() {
        assertEquals("子", Mountains24.mountainAt(0f))
        assertEquals("子", Mountains24.mountainAt(7.49f))
        assertEquals("癸", Mountains24.mountainAt(7.5f))
        assertEquals("癸", Mountains24.mountainAt(14.99f))
        assertEquals("癸", Mountains24.mountainAt(15f))
        assertEquals("丑", Mountains24.mountainAt(30f))
        assertEquals("壬", Mountains24.mountainAt(345f))
        assertEquals("壬", Mountains24.mountainAt(352.49f))
        assertEquals("子", Mountains24.mountainAt(352.5f))
        assertEquals("子", Mountains24.mountainAt(359.99f))
    }

    @Test
    fun `非法方位角被拒绝`() {
        assertThrows(IllegalArgumentException::class.java) { Mountains24.mountainIndexAt(-0.1f) }
        assertThrows(IllegalArgumentException::class.java) { Mountains24.mountainIndexAt(360f) }
        assertThrows(IllegalArgumentException::class.java) { Mountains24.mountainIndexAt(Float.NaN) }
    }
}
