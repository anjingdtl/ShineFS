package com.shinefs.core.yijing.element

import com.shinefs.core.calendar.model.SolarTerm
import com.shinefs.core.yijing.element.Season
import com.shinefs.core.yijing.element.SeasonalQi
import com.shinefs.core.yijing.model.Element
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalQiTest {

    @Test
    fun `四季当令五行`() {
        // 立春（0）→ 春木；芒种（8）→ 夏火；白露（14）→ 秋金；大雪（20）→ 冬水
        val cases = listOf(
            Triple(SolarTerm.LI_CHUN, Season.SPRING, Element.WOOD),
            Triple(SolarTerm.JING_ZHE, Season.SPRING, Element.WOOD),
            Triple(SolarTerm.MANG_ZHONG, Season.SUMMER, Element.FIRE),
            Triple(SolarTerm.BAI_LU, Season.AUTUMN, Element.METAL),
            Triple(SolarTerm.DA_XUE, Season.WINTER, Element.WATER),
            Triple(SolarTerm.DONG_ZHI, Season.WINTER, Element.WATER),
        )
        for ((term, season, element) in cases) {
            val qi = SeasonalQi.of(term)
            assertEquals("${term.chinese} 季", season, qi.season)
            assertEquals("${term.chinese} 当令", element, qi.dominantElement)
            assertFalse(qi.earthMonth)
        }
    }

    @Test
    fun `辰戌丑未四月土旺`() {
        val earthTerms = listOf(
            SolarTerm.QING_MING, // 辰月起
            SolarTerm.GU_YU,
            SolarTerm.XIAO_SHU, // 未月起
            SolarTerm.DA_SHU,
            SolarTerm.HAN_LU, // 戌月起
            SolarTerm.SHUANG_JIANG,
            SolarTerm.XIAO_HAN, // 丑月起
            SolarTerm.DA_HAN,
        )
        for (term in earthTerms) {
            val qi = SeasonalQi.of(term)
            assertTrue("${term.chinese} 应为土旺月", qi.earthMonth)
            assertEquals(Element.EARTH, qi.dominantElement)
        }
        // 辰月仍属春、未月属夏（季节字段独立于土旺标记）
        assertEquals(Season.SPRING, SeasonalQi.of(SolarTerm.QING_MING).season)
        assertEquals(Season.SUMMER, SeasonalQi.of(SolarTerm.XIAO_SHU).season)
        assertEquals(Season.AUTUMN, SeasonalQi.of(SolarTerm.HAN_LU).season)
        assertEquals(Season.WINTER, SeasonalQi.of(SolarTerm.XIAO_HAN).season)
    }

    @Test
    fun `二十四节气全覆盖无异常`() {
        for (ordinal in 0..23) {
            val qi = SeasonalQi.of(ordinal)
            assertEquals("seasonal-qi-v1", qi.sourceRuleId)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `非法节气序拒绝`() {
        SeasonalQi.of(24)
    }
}
