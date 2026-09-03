package com.shinefs.core.yijing.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrigramTest {

    @Test
    fun `八卦共八枚且名称符号唯一`() {
        assertEquals(8, Trigram.entries.size)
        assertEquals(8, Trigram.entries.map { it.chineseName }.toSet().size)
        assertEquals(8, Trigram.entries.map { it.symbol }.toSet().size)
        assertEquals(8, Trigram.entries.map { it.lines }.toSet().size)
    }

    @Test
    fun `爻序列均为自下而上三位0或1`() {
        Trigram.entries.forEach { t ->
            assertEquals(3, t.lines.size)
            assertTrue(t.lines.all { it == 0 || it == 1 })
        }
    }

    @Test
    fun `先天卦数为乾一兑二离三震四巽五坎六艮七坤八`() {
        val byName = mapOf(
            "乾" to 1, "兑" to 2, "离" to 3, "震" to 4,
            "巽" to 5, "坎" to 6, "艮" to 7, "坤" to 8,
        )
        Trigram.entries.forEach { t ->
            assertEquals(byName[t.chineseName], t.xiantianNumber)
        }
    }

    /** 逐卦属性表（方案 §3.1 后天八卦方位 + 通行属性）。 */
    @Test
    fun `逐卦属性-名称符号爻列方位角度五行象义亲属`() {
        val table = listOf(
            Triple(Trigram.QIAN, "☰ 乾 西北 315 金 天 父 [1,1,1]", listOf(1, 1, 1)),
            Triple(Trigram.DUI, "☱ 兑 西 270 金 泽 少女 [1,1,0]", listOf(1, 1, 0)),
            Triple(Trigram.LI, "☲ 离 南 180 火 火 中女 [1,0,1]", listOf(1, 0, 1)),
            Triple(Trigram.ZHEN, "☳ 震 东 90 木 雷 长男 [1,0,0]", listOf(1, 0, 0)),
            Triple(Trigram.XUN, "☴ 巽 东南 135 木 风 长女 [0,1,1]", listOf(0, 1, 1)),
            Triple(Trigram.KAN, "☵ 坎 北 0 水 水 中男 [0,1,0]", listOf(0, 1, 0)),
            Triple(Trigram.GEN, "☶ 艮 东北 45 土 山 少男 [0,0,1]", listOf(0, 0, 1)),
            Triple(Trigram.KUN, "☷ 坤 西南 225 土 地 母 [0,0,0]", listOf(0, 0, 0)),
        )
        table.forEach { (trigram, expected, lines) ->
            assertEquals(lines, trigram.lines)
            val parts = expected.split(" ")
            assertEquals(parts[0], trigram.symbol)
            assertEquals(parts[1], trigram.chineseName)
            assertEquals(parts[2], trigram.direction)
            assertEquals(parts[3].toFloat(), trigram.directionAngle)
            assertEquals(parts[4], trigram.element)
            assertEquals(parts[5], trigram.natureImage)
            assertEquals(parts[6], trigram.familyRole)
        }
    }

    @Test
    fun `fromLines 与 lines 互逆`() {
        Trigram.entries.forEach { t ->
            assertEquals(t, Trigram.fromLines(t.lines))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromLines 拒绝非法爻列`() {
        Trigram.fromLines(listOf(1, 2, 0))
    }
}
