package com.shinefs.core.yijing.tiyong

import com.shinefs.core.yijing.data.Hexagrams
import com.shinefs.core.yijing.model.Element
import com.shinefs.core.yijing.model.ElementRelation
import com.shinefs.core.yijing.model.ElementRelations
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.model.TrigramElements
import com.shinefs.core.yijing.tiyong.MovingPart
import com.shinefs.core.yijing.tiyong.TiYongOps
import org.junit.Assert.assertEquals
import org.junit.Test

class TiYongOpsTest {

    @Test
    fun `三八四组体用全覆盖`() {
        for (hex in Hexagrams.all) {
            for (line in 1..6) {
                val tiYong = TiYongOps.of(hex, line)
                if (line <= 3) {
                    assertEquals("${hex.chineseName} $line 爻动下卦为用", MovingPart.LOWER, tiYong.movingPart)
                    assertEquals(hex.upperTrigram, tiYong.ti)
                    assertEquals(hex.lowerTrigram, tiYong.yong)
                } else {
                    assertEquals("${hex.chineseName} $line 爻动上卦为用", MovingPart.UPPER, tiYong.movingPart)
                    assertEquals(hex.lowerTrigram, tiYong.ti)
                    assertEquals(hex.upperTrigram, tiYong.yong)
                }
            }
        }
    }

    @Test
    fun `观梅占体用 - 革之初爻 体兑金用离火 火克金`() {
        val ge = Hexagrams.all[48] // 49 革：离下兑上
        val tiYong = TiYongOps.of(ge, 1)
        assertEquals(Trigram.DUI, tiYong.ti)
        assertEquals(Trigram.LI, tiYong.yong)
        assertEquals(Element.METAL, TrigramElements.of(tiYong.ti))
        assertEquals(Element.FIRE, TrigramElements.of(tiYong.yong))
        assertEquals(ElementRelation.YONG_CONTROLS_TI, ElementRelations.of(TrigramElements.of(tiYong.ti), TrigramElements.of(tiYong.yong)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `动爻越界拒绝`() {
        TiYongOps.of(Hexagrams.all[0], 7)
    }
}

class ElementTest {

    @Test
    fun `八卦五行归属`() {
        assertEquals(Element.METAL, TrigramElements.of(Trigram.QIAN))
        assertEquals(Element.METAL, TrigramElements.of(Trigram.DUI))
        assertEquals(Element.WOOD, TrigramElements.of(Trigram.ZHEN))
        assertEquals(Element.WOOD, TrigramElements.of(Trigram.XUN))
        assertEquals(Element.WATER, TrigramElements.of(Trigram.KAN))
        assertEquals(Element.FIRE, TrigramElements.of(Trigram.LI))
        assertEquals(Element.EARTH, TrigramElements.of(Trigram.KUN))
        assertEquals(Element.EARTH, TrigramElements.of(Trigram.GEN))
    }

    @Test
    fun `五行生克全二十五对`() {
        // 相生
        assertEquals(ElementRelation.TI_GENERATES_YONG, ElementRelations.of(Element.METAL, Element.WATER)) // 金生水
        assertEquals(ElementRelation.TI_GENERATES_YONG, ElementRelations.of(Element.WATER, Element.WOOD)) // 水生木
        assertEquals(ElementRelation.TI_GENERATES_YONG, ElementRelations.of(Element.WOOD, Element.FIRE)) // 木生火
        assertEquals(ElementRelation.TI_GENERATES_YONG, ElementRelations.of(Element.FIRE, Element.EARTH)) // 火生土
        assertEquals(ElementRelation.TI_GENERATES_YONG, ElementRelations.of(Element.EARTH, Element.METAL)) // 土生金
        // 相克
        assertEquals(ElementRelation.TI_CONTROLS_YONG, ElementRelations.of(Element.METAL, Element.WOOD)) // 金克木
        assertEquals(ElementRelation.TI_CONTROLS_YONG, ElementRelations.of(Element.WOOD, Element.EARTH)) // 木克土
        assertEquals(ElementRelation.TI_CONTROLS_YONG, ElementRelations.of(Element.EARTH, Element.WATER)) // 土克水
        assertEquals(ElementRelation.TI_CONTROLS_YONG, ElementRelations.of(Element.WATER, Element.FIRE)) // 水克火
        assertEquals(ElementRelation.TI_CONTROLS_YONG, ElementRelations.of(Element.FIRE, Element.METAL)) // 火克金
        // 比和
        for (e in Element.entries) {
            assertEquals(ElementRelation.SAME, ElementRelations.of(e, e))
        }
        // 全 25 对穷举：每对必须落入且仅落入一种关系
        for (ti in Element.entries) {
            for (yong in Element.entries) {
                val rel = ElementRelations.of(ti, yong)
                val expected = when {
                    ti == yong -> ElementRelation.SAME
                    ti.generates == yong -> ElementRelation.TI_GENERATES_YONG
                    yong.generates == ti -> ElementRelation.YONG_GENERATES_TI
                    ti.controls == yong -> ElementRelation.TI_CONTROLS_YONG
                    else -> ElementRelation.YONG_CONTROLS_TI
                }
                assertEquals(expected, rel)
            }
        }
    }
}
