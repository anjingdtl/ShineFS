package com.shinefs.core.yijing.nuclear

import com.shinefs.core.yijing.data.Hexagrams
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.nuclear.NuclearPolicy
import com.shinefs.core.yijing.nuclear.NuclearOps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NuclearOpsTest {

    @Test
    fun `六十四卦互卦全覆盖且落在卦表内`() {
        for (hex in Hexagrams.all) {
            val nuclear = NuclearOps.compute(hex)
            assertNotNull("互卦不得为空（STANDARD 政策，乾坤有互）：${hex.chineseName}", nuclear)
            assertEquals(hex.chineseName, nuclear!!.lower, nuclear.hexagram.lowerTrigram)
            assertEquals(hex.chineseName, nuclear.upper, nuclear.hexagram.upperTrigram)
            assertTrue(
                Hexagrams.all.any { it.kingWenOrder == nuclear.hexagram.kingWenOrder },
            )
        }
    }

    @Test
    fun `互卦锚点`() {
        // 乾六爻皆阳 → 互仍乾；坤皆阴 → 互仍坤
        assertEquals(1, NuclearOps.compute(Hexagrams.all[0])!!.hexagram.kingWenOrder)
        assertEquals(2, NuclearOps.compute(Hexagrams.all[1])!!.hexagram.kingWenOrder)
        // 既济（63）互未济（64）：101 010 → 下互010坎 上互101离；未济（64）互既济，往来互变
        assertEquals(64, NuclearOps.compute(Hexagrams.all[62])!!.hexagram.kingWenOrder)
        assertEquals(63, NuclearOps.compute(Hexagrams.all[63])!!.hexagram.kingWenOrder)
        // 咸（31 兑上艮下）互姤（44）：下互巽 上互乾
        val xian = NuclearOps.compute(Hexagrams.all[30])!!
        assertEquals(Trigram.XUN, xian.lower)
        assertEquals(Trigram.QIAN, xian.upper)
        assertEquals(44, xian.hexagram.kingWenOrder)
        // 革（49 兑上离下 101 110）互卦：下互=0,1,1巽 上互=1,1,1乾 → 姤
        val ge = NuclearOps.compute(Hexagrams.all[48])!!
        assertEquals(Trigram.XUN, ge.lower)
        assertEquals(Trigram.QIAN, ge.upper)
        assertEquals(44, ge.hexagram.kingWenOrder)
        // 姤（44 乾上巽下 011 111）中互纯乾：下互=1,1,1乾 上互=1,1,1乾 → 乾（1）
        val gou = NuclearOps.compute(Hexagrams.all[43])!!
        assertEquals(Trigram.QIAN, gou.lower)
        assertEquals(Trigram.QIAN, gou.upper)
        assertEquals(1, gou.hexagram.kingWenOrder)
    }

    @Test
    fun `互卦爻序定义 - 下互234上互345`() {
        // 泰（11 乾下坤上 111 000）：下互=1,1,0兑 上互=1,0,0震 → 雷泽归妹（54）
        val tai = NuclearOps.compute(Hexagrams.all[10])!!
        assertEquals(Trigram.DUI, tai.lower)
        assertEquals(Trigram.ZHEN, tai.upper)
        assertEquals(54, tai.hexagram.kingWenOrder)
    }

    @Test
    fun `旧说乾坤无互策略`() {
        assertNull(NuclearOps.compute(Hexagrams.all[0], NuclearPolicy.LEGACY_QIAN_KUN_NO_NUCLEAR))
        assertNull(NuclearOps.compute(Hexagrams.all[1], NuclearPolicy.LEGACY_QIAN_KUN_NO_NUCLEAR))
        // 非乾坤不受影响
        assertNotNull(NuclearOps.compute(Hexagrams.all[62], NuclearPolicy.LEGACY_QIAN_KUN_NO_NUCLEAR))
    }
}
