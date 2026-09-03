package com.shinefs.core.yijing.divination

import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DivinationOutcomeTest {

    @Test
    fun `本卦与变卦由上下卦与动爻确定性推出`() {
        val outcome = DivinationOutcome(
            ruleId = "test-rule",
            upperTrigram = Trigram.KAN,
            lowerTrigram = Trigram.LI,
            changingLine = 3,
        )
        assertEquals(63, outcome.originalHexagram.kingWenOrder)
        assertEquals("既济", outcome.originalHexagram.chineseName)
        assertEquals(3, outcome.changedHexagram.kingWenOrder)
        assertEquals("屯", outcome.changedHexagram.chineseName)
    }

    @Test
    fun `同输入必同输出-可重复`() {
        fun cast() = DivinationOutcome("r", Trigram.QIAN, Trigram.KUN, 2)
        assertEquals(cast(), cast())
    }

    @Test
    fun `动爻越界即拒绝`() {
        assertThrows(IllegalArgumentException::class.java) {
            DivinationOutcome("r", Trigram.QIAN, Trigram.KUN, 7)
        }
    }
}
