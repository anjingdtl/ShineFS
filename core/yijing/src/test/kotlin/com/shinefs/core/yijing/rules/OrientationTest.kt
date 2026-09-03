package com.shinefs.core.yijing.rules

import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class OrientationTest {

    /** 方案 §12.1 坐向用例：0°→180°、90°→270°、359°→179°。 */
    @Test
    fun `方案指定坐向用例`() {
        val a = Orientation.fromAzimuth(0f)
        assertEquals("子", a.facingMountain)
        assertEquals("午", a.sittingMountain)

        val b = Orientation.fromAzimuth(90f)
        assertEquals("卯", b.facingMountain)
        assertEquals("酉", b.sittingMountain)

        val c = Orientation.fromAzimuth(359f)
        assertEquals("子", c.facingMountain)
        assertEquals("午", c.sittingMountain)
    }

    /** 方案 §10.1 AI 输入示例：182.4° → 向午坐子、离卦、火。 */
    @Test
    fun `方案10_1示例-182_4度向午坐子属离属火`() {
        val o = Orientation.fromAzimuth(182.4f)
        assertEquals("午", o.facingMountain)
        assertEquals("子", o.sittingMountain)
        assertEquals(Trigram.LI, o.facingTrigram)
        assertEquals("火", o.facingElement)
    }

    @Test
    fun `a的坐山等于a加180度的向山-且向坐卦恒相对`() {
        listOf(0f, 7.5f, 45f, 90f, 135.9f, 180f, 225f, 270.3f, 315f, 352.5f, 359.99f)
            .forEach { azimuth ->
                val o = Orientation.fromAzimuth(azimuth)
                val opposite = Orientation.fromAzimuth((azimuth + 180f) % 360f)
                assertEquals(opposite.facingMountain, o.sittingMountain)
                assertEquals(opposite.facingTrigram, o.sittingTrigram)
            }
    }

    @Test
    fun `非法方位角被拒绝`() {
        assertThrows(IllegalArgumentException::class.java) { Orientation.fromAzimuth(-1f) }
        assertThrows(IllegalArgumentException::class.java) { Orientation.fromAzimuth(360f) }
    }
}
