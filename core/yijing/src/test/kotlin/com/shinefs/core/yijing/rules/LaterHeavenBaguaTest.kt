package com.shinefs.core.yijing.rules

import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Test

class LaterHeavenBaguaTest {

    /** 方案 §12.1 八卦角度边界。 */
    @Test
    fun `方案指定角度边界`() {
        assertEquals(Trigram.KAN, LaterHeavenBagua.trigramAt(0f))
        assertEquals(Trigram.KAN, LaterHeavenBagua.trigramAt(22.49f))
        assertEquals(Trigram.GEN, LaterHeavenBagua.trigramAt(22.5f))
        assertEquals(Trigram.GEN, LaterHeavenBagua.trigramAt(44.99f))
        assertEquals(Trigram.GEN, LaterHeavenBagua.trigramAt(45f))
        assertEquals(Trigram.GEN, LaterHeavenBagua.trigramAt(67.49f))
        assertEquals(Trigram.ZHEN, LaterHeavenBagua.trigramAt(67.5f))
        assertEquals(Trigram.XUN, LaterHeavenBagua.trigramAt(135f))
        assertEquals(Trigram.LI, LaterHeavenBagua.trigramAt(180f))
        assertEquals(Trigram.KUN, LaterHeavenBagua.trigramAt(225f))
        assertEquals(Trigram.DUI, LaterHeavenBagua.trigramAt(270f))
        assertEquals(Trigram.QIAN, LaterHeavenBagua.trigramAt(315f))
        assertEquals(Trigram.QIAN, LaterHeavenBagua.trigramAt(337.49f))
        assertEquals(Trigram.KAN, LaterHeavenBagua.trigramAt(337.5f))
        assertEquals(Trigram.KAN, LaterHeavenBagua.trigramAt(359.99f))
    }

    @Test
    fun `每卦后天方位中心角落于本卦`() {
        Trigram.entries.forEach { t ->
            assertEquals(t, LaterHeavenBagua.trigramAt(t.directionAngle))
        }
    }

    /** 二十四山与后天八卦的领属关系（每卦领三山），双实现交叉验证。 */
    @Test
    fun `每山中心角的卦与山领属表一致`() {
        val mountainToTrigram = mapOf(
            "壬" to Trigram.KAN, "子" to Trigram.KAN, "癸" to Trigram.KAN,
            "丑" to Trigram.GEN, "艮" to Trigram.GEN, "寅" to Trigram.GEN,
            "甲" to Trigram.ZHEN, "卯" to Trigram.ZHEN, "乙" to Trigram.ZHEN,
            "辰" to Trigram.XUN, "巽" to Trigram.XUN, "巳" to Trigram.XUN,
            "丙" to Trigram.LI, "午" to Trigram.LI, "丁" to Trigram.LI,
            "未" to Trigram.KUN, "坤" to Trigram.KUN, "申" to Trigram.KUN,
            "庚" to Trigram.DUI, "酉" to Trigram.DUI, "辛" to Trigram.DUI,
            "戌" to Trigram.QIAN, "乾" to Trigram.QIAN, "亥" to Trigram.QIAN,
        )
        assertEquals(24, mountainToTrigram.size)
        Mountains24.names.forEachIndexed { index, mountain ->
            assertEquals(
                mountainToTrigram.getValue(mountain),
                LaterHeavenBagua.trigramAt(Mountains24.centerAngleOf(index)),
            )
        }
    }
}
