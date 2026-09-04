package com.shinefs.core.yijing.element

import com.shinefs.core.calendar.model.SolarTerm
import com.shinefs.core.yijing.model.Element

/** 四时（按节气月令，非农历月数；两者不得混用，V2.0 方案 §16）。 */
enum class Season(val chinese: String, val element: Element) {
    SPRING("春", Element.WOOD),
    SUMMER("夏", Element.FIRE),
    AUTUMN("秋", Element.METAL),
    WINTER("冬", Element.WATER),
}

/**
 * 时令/卦气事实层（`seasonal-qi-v1`，B 级）。
 *
 * 春木、夏火、秋金、冬水；辰戌丑未月（清明–立夏、小暑–立秋、寒露–立冬、小寒–立春）土旺。
 * 仅呈现当令五行事实；旺相休囚死细目为待决策 TD-V2-05，不做。
 * 节气 ordinal：0=立春、1=雨水…23=大寒（见 [SolarTerm]）。
 */
data class SeasonalQiContext(
    val season: Season,
    /** 是否辰/未/戌/丑月（土旺月）。 */
    val earthMonth: Boolean,
    /** 当令五行（土旺月取土，其余取季五行）。 */
    val dominantElement: Element,
    /** 规则来源（RuleManifest 对应 ruleId）。 */
    val sourceRuleId: String,
)

object SeasonalQi {

    const val RULE_ID = "seasonal-qi-v1"

    fun of(solarTermOrdinal: Int): SeasonalQiContext {
        require(solarTermOrdinal in 0..23) { "solarTermOrdinal must be in 0..23" }
        // 月建序（0=寅月…11=丑月）：立春起每两气进一月
        val monthOrder = solarTermOrdinal / 2
        val earth = monthOrder in setOf(2, 5, 8, 11) // 辰、未、戌、丑月
        val season = when (monthOrder) {
            0, 1 -> Season.SPRING // 寅卯月
            2 -> Season.SPRING // 辰月仍属春，但土旺
            3, 4 -> Season.SUMMER // 巳午月
            5 -> Season.SUMMER // 未月属夏，土旺
            6, 7 -> Season.AUTUMN // 申酉月
            8 -> Season.AUTUMN // 戌月属秋，土旺
            9, 10 -> Season.WINTER // 亥子月
            else -> Season.WINTER // 丑月属冬，土旺
        }
        return SeasonalQiContext(
            season = season,
            earthMonth = earth,
            dominantElement = if (earth) Element.EARTH else season.element,
            sourceRuleId = RULE_ID,
        )
    }

    fun of(term: SolarTerm): SeasonalQiContext = of(term.ordinal)
}
