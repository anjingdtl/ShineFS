package com.shinefs.core.classics

import com.shinefs.core.yijing.data.Hexagrams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 原典库核验（V2.0 方案 §18/§26）：
 * 结构完整性（64/384/2）+ 与卦表交叉 + checksum 复算 + 双源锚点抽查（源B：结构化录入核对）。
 */
class CanonicalCorpusTest {

    @Test
    fun `六十四卦完整且与卦表一致`() {
        assertEquals(64, CanonicalCorpus.all.size)
        assertEquals(
            Hexagrams.all.map { it.chineseName },
            CanonicalCorpus.all.map { it.name },
        )
        assertEquals((1..64).toList(), CanonicalCorpus.all.map { it.kingWenOrder })
    }

    @Test
    fun `三百八十四爻加用九用六完整`() {
        val totalLines = CanonicalCorpus.all.sumOf { it.lines.size }
        assertEquals(384, totalLines)
        val withUse = CanonicalCorpus.all.filter { it.specialUseText != null }
        assertEquals(2, withUse.size)
        assertEquals("乾", withUse[0].name)
        assertEquals("坤", withUse[1].name)
        assertTrue(withUse[0].specialUseText!!.contains("用九"))
        assertTrue(withUse[1].specialUseText!!.contains("用六"))
        assertNotNull(withUse[0].specialUseSmallImage)
        assertNotNull(withUse[1].specialUseSmallImage)
    }

    @Test
    fun `逐卦结构与非空校验`() {
        for (e in CanonicalCorpus.all) {
            assertTrue("${e.name} 卦辞空", e.judgment.isNotBlank())
            assertTrue("${e.name} 彖空", !e.tuan.isNullOrBlank())
            assertTrue("${e.name} 大象空", !e.greatImage.isNullOrBlank())
            assertEquals("${e.name} 爻数", 6, e.lines.size)
            assertEquals("${e.name} 爻序", listOf(1, 2, 3, 4, 5, 6), e.lines.map { it.line })
            for (l in e.lines) {
                assertTrue("${e.name} 第${l.line}爻辞空", l.text.isNotBlank())
                assertTrue("${e.name} 第${l.line}爻小象空", !l.smallImage.isNullOrBlank())
            }
            assertTrue(e.verified)
            assertEquals("S-AE2", e.sourceId)
            assertTrue(e.sourceEdition.isNotBlank())
        }
    }

    @Test
    fun `逐卦 checksum 复算一致`() {
        for (e in CanonicalCorpus.all) {
            assertEquals("${e.name} checksum", e.checksum, CanonicalCorpus.computeChecksum(e))
        }
        assertEquals(64, CanonicalCorpus.corpusChecksum.length)
        assertTrue(CanonicalCorpus.corpusChecksum.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun `版本与来源声明`() {
        assertEquals("zhouyi-corpus-v1", CanonicalCorpus.version)
        assertTrue(CanonicalCorpus.edition.contains("wikisource"))
        assertNotNull(CanonicalCorpus.byKingWenOrder(1))
        assertEquals("未济", CanonicalCorpus.byKingWenOrder(64)!!.name)
    }

    // ---------- 双源锚点抽查（源B：独立于维基文库的结构化核对） ----------

    @Test
    fun `乾卦锚点`() {
        val qian = CanonicalCorpus.byKingWenOrder(1)!!
        assertEquals("元亨。利贞。", qian.judgment)
        assertEquals("潜龙勿用。", qian.lines[0].text)
        assertEquals("见龙在田，利见大人。", qian.lines[1].text)
        assertEquals("君子终日干干，夕惕若；厉，无咎。", qian.lines[2].text)
        assertEquals("或跃在渊，无咎。", qian.lines[3].text)
        assertEquals("飞龙在天，利见大人。", qian.lines[4].text)
        assertEquals("亢龙，有悔。", qian.lines[5].text)
        assertEquals("用九：见群龙无首，吉。", qian.specialUseText)
        assertEquals("天行健，君子以自强不息。", qian.greatImage)
        assertEquals("潜龙勿用，阳在下也。", qian.lines[0].smallImage)
        assertTrue(qian.tuan!!.startsWith("大哉乾元"))
    }

    @Test
    fun `坤卦锚点`() {
        val kun = CanonicalCorpus.byKingWenOrder(2)!!
        assertEquals("履霜，坚冰至。", kun.lines[0].text)
        assertEquals("直方大，不习无不利。", kun.lines[1].text)
        assertEquals("黄裳，元吉。", kun.lines[4].text)
        assertEquals("龙战于野，其血玄黄。", kun.lines[5].text)
        assertEquals("地势坤，君子以厚德载物。", kun.greatImage)
        assertTrue(kun.specialUseText!!.contains("利永贞"))
        assertTrue(kun.judgment.contains("利牝马之贞"))
    }

    @Test
    fun `散卦锚点抽查`() {
        // 去标点比对：校验文字内容；标点风格随底本（句读以电子底本为准）
        fun norm(x: String?) = x!!.filter { it !in "，。；：、！？（）" }
        fun hex(order: Int) = CanonicalCorpus.byKingWenOrder(order)!!
        // 屯（3）
        assertTrue(norm(hex(3).judgment).contains(norm("勿用有攸往，利建侯")))
        // 蒙（4）
        assertTrue(norm(hex(4).judgment).contains(norm("匪我求童蒙，童蒙求我")))
        // 师（7）初六
        assertTrue(norm(hex(7).lines[0].text).contains(norm("师出以律")))
        // 谦（15）初六
        assertEquals("谦谦君子，用涉大川，吉。", hex(15).lines[0].text)
        // 蛊（18）卦辞
        assertTrue(norm(hex(18).judgment).contains(norm("元亨，利涉大川")))
        assertTrue(norm(hex(18).judgment).contains(norm("先甲三日，后甲三日")))
        // 观（20）卦辞
        assertTrue(norm(hex(20).judgment).contains(norm("盥而不荐，有孚颙若")))
        // 复（24）卦辞
        assertTrue(norm(hex(24).judgment).contains(norm("出入无疾，朋来无咎")))
        // 咸（31）卦辞
        assertTrue(norm(hex(31).judgment).contains(norm("亨，利贞，取女吉")))
        // 姤（44）卦辞
        assertTrue(norm(hex(44).judgment).contains(norm("女壮，勿用取女")))
        // 鼎（50）卦辞
        assertTrue(norm(hex(50).judgment).contains(norm("元吉，亨")))
        // 中孚（61）卦辞
        assertTrue(norm(hex(61).judgment).contains(norm("豚鱼吉")))
        // 既济（63）大象
        assertEquals("水在火上，既济；君子以思患而豫防之。", hex(63).greatImage)
        // 未济（64）卦辞
        assertTrue(norm(hex(64).judgment).contains(norm("小狐汔济，濡其尾，无攸利")))
        // 无妄（25）六三
        assertTrue(norm(hex(25).lines[2].text).contains(norm("无妄之灾")))
        // 大过（28）大象
        assertTrue(norm(hex(28).greatImage).contains(norm("泽灭木，大过；君子以独立不惧，遁世无闷")))
        // 坎（29）卦辞以习坎开头（卦辞正文）
        assertTrue(norm(hex(29).judgment).startsWith(norm("习坎")))
        // 遯（33）卦辞
        assertTrue(norm(hex(33).judgment).contains(norm("亨，小利贞")))
    }

    @Test
    fun `异文透明保留`() {
        val totalVariants = CanonicalCorpus.all.sumOf { it.textualVariants.size }
        assertTrue(totalVariants >= 1)
        assertTrue(CanonicalCorpus.byKingWenOrder(1)!!.textualVariants.contains("一作太和"))
    }
}
