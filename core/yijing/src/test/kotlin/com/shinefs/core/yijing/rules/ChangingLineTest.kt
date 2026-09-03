package com.shinefs.core.yijing.rules

import com.shinefs.core.yijing.data.Hexagrams
import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 变卦全覆盖测试：64 卦 × 6 动爻 = 384 组合（产品方案 §12.1）。
 */
class ChangingLineTest {

    @Test
    fun `384组合全部产生正确变卦`() {
        var cases = 0
        for (order in 1..64) {
            val original = Hexagrams.byKingWenOrder(order)
            for (line in 1..6) {
                val changed = HexagramOps.withChangingLine(original, line)
                cases++

                // 期望：只翻转指定爻，其余五爻不变
                val expectedLines = original.lines.toMutableList()
                expectedLines[line - 1] = 1 - expectedLines[line - 1]
                assertEquals("卦$order 爻$line 六爻不符", expectedLines, changed.lines)

                // 变卦确为六爻重新拆卦映射的结果
                assertEquals(
                    changed,
                    HexagramOps.fromTrigrams(
                        Trigram.fromLines(expectedLines.subList(0, 3)),
                        Trigram.fromLines(expectedLines.subList(3, 6)),
                    ),
                )

                // 变卦必异于本卦，且仍是 64 卦之一
                assertNotEquals(original, changed)
                assertTrue(changed.kingWenOrder in 1..64)
            }
        }
        assertEquals(384, cases)
    }

    @Test
    fun `同一爻翻转两次回到本卦`() {
        for (order in 1..64) {
            val original = Hexagrams.byKingWenOrder(order)
            for (line in 1..6) {
                val roundTrip = HexagramOps.withChangingLine(
                    HexagramOps.withChangingLine(original, line),
                    line,
                )
                assertEquals(original, roundTrip)
            }
        }
    }

    /** 方案 §10.1 示例：既济（离下坎上）三爻动 → 屯（震下坎上）。 */
    @Test
    fun `方案示例-既济三爻动变屯`() {
        val jiji = HexagramOps.fromTrigrams(Trigram.LI, Trigram.KAN)
        assertEquals(63, jiji.kingWenOrder)
        val changed = HexagramOps.withChangingLine(jiji, 3)
        assertEquals("屯", changed.chineseName)
        assertEquals(3, changed.kingWenOrder)
    }

    @Test
    fun `动爻编号非法即拒绝`() {
        val hexagram = Hexagrams.byKingWenOrder(1)
        assertThrows(IllegalArgumentException::class.java) {
            HexagramOps.withChangingLine(hexagram, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            HexagramOps.withChangingLine(hexagram, 7)
        }
    }
}
