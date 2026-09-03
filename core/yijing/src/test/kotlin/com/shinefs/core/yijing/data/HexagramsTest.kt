package com.shinefs.core.yijing.data

import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HexagramsTest {

    @Test
    fun `共64卦-KingWen序1到64各一次`() {
        assertEquals(64, Hexagrams.all.size)
        assertEquals((1..64).toList(), Hexagrams.all.map { it.kingWenOrder }.sorted())
    }

    @Test
    fun `卦名非空且唯一`() {
        assertEquals(64, Hexagrams.all.map { it.chineseName }.toSet().size)
        Hexagrams.all.forEach { assertTrue(it.chineseName.isNotBlank()) }
    }

    @Test
    fun `上下卦组合覆盖全部8乘8-无重复`() {
        val pairs = Hexagrams.all.map { Pair(it.lowerTrigram, it.upperTrigram) }
        assertEquals(64, pairs.toSet().size)
        Trigram.entries.forEach { lower ->
            Trigram.entries.forEach { upper ->
                assertNotNull(
                    "缺卦：下${lower.chineseName}上${upper.chineseName}",
                    Hexagrams.byTrigrams(lower, upper),
                )
            }
        }
    }

    @Test
    fun `六爻恒等于下三爻加上三爻`() {
        Hexagrams.all.forEach { h ->
            assertEquals(h.lowerTrigram.lines + h.upperTrigram.lines, h.lines)
            assertEquals(6, h.lines.size)
        }
    }

    @Test
    fun `卦符为Unicode易经区块且随KingWen序`() {
        Hexagrams.all.forEach { h ->
            assertEquals(1, h.symbol.length)
            assertEquals(0x4DC0 + h.kingWenOrder - 1, h.symbol[0].code)
        }
        assertEquals("䷀", Hexagrams.byKingWenOrder(1).symbol)
        assertEquals("䷿", Hexagrams.byKingWenOrder(64).symbol)
    }

    /** 抽查锚点卦（含八纯卦与易错卦）。 */
    @Test
    fun `锚点卦-卦名与上下卦`() {
        fun assertHex(order: Int, name: String, lower: Trigram, upper: Trigram) {
            val h = Hexagrams.byKingWenOrder(order)
            assertEquals(name, h.chineseName)
            assertEquals(lower, h.lowerTrigram)
            assertEquals(upper, h.upperTrigram)
        }
        assertHex(1, "乾", Trigram.QIAN, Trigram.QIAN)
        assertHex(2, "坤", Trigram.KUN, Trigram.KUN)
        assertHex(3, "屯", Trigram.ZHEN, Trigram.KAN)
        assertHex(4, "蒙", Trigram.KAN, Trigram.GEN)
        assertHex(11, "泰", Trigram.QIAN, Trigram.KUN)
        assertHex(12, "否", Trigram.KUN, Trigram.QIAN)
        assertHex(29, "坎", Trigram.KAN, Trigram.KAN)
        assertHex(30, "离", Trigram.LI, Trigram.LI)
        assertHex(41, "损", Trigram.DUI, Trigram.GEN)
        assertHex(42, "益", Trigram.ZHEN, Trigram.XUN)
        assertHex(55, "丰", Trigram.LI, Trigram.ZHEN)
        assertHex(61, "中孚", Trigram.DUI, Trigram.XUN)
        assertHex(62, "小过", Trigram.GEN, Trigram.ZHEN)
        assertHex(63, "既济", Trigram.LI, Trigram.KAN)
        assertHex(64, "未济", Trigram.KAN, Trigram.LI)
    }
}
