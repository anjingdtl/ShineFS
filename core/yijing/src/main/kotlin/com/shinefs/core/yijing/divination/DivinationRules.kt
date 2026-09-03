package com.shinefs.core.yijing.divination

import com.shinefs.core.yijing.model.Hexagram
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.rules.HexagramOps

/**
 * 起卦结果：上卦、下卦、动爻一经确定，本卦与变卦由确定性演算推出。
 * AI 层只能消费本结构，不得改写（产品方案 §10.2）。
 */
data class DivinationOutcome(
    val ruleId: String,
    val upperTrigram: Trigram,
    val lowerTrigram: Trigram,
    val changingLine: Int,
) {
    init {
        require(changingLine in 1..6) { "changingLine must be in 1..6, got $changingLine" }
    }

    val originalHexagram: Hexagram
        get() = HexagramOps.fromTrigrams(lowerTrigram, upperTrigram)

    val changedHexagram: Hexagram
        get() = HexagramOps.withChangingLine(originalHexagram, changingLine)
}

/** 模式 A：方位起卦输入（向首方位角 + 定盘时刻）。 */
data class DirectionCastInput(val azimuth: Float, val epochMillis: Long)

/** 模式 B：时间起卦输入。 */
data class TimeCastInput(val epochMillis: Long)

/** 模式 C：手动数字起卦输入（2～3 个数字）。 */
data class NumberCastInput(val numbers: List<Int>)

/**
 * 起卦规则接口（模式 A/B/C）。
 *
 * ⚠️ 实现约束：具体取数公式（时间换算口径、求余与余 0 约定）尚未拍板，
 * 对应待决策项 D-01～D-05（DOCS/YIJING_RULES.md）。拍板前**不提供任何实现**，
 * 防止猜测口径混入术数规则。
 */
interface DirectionDivinationRule {
    val ruleId: String
    val displayName: String
    fun cast(input: DirectionCastInput): DivinationOutcome
}

interface TimeDivinationRule {
    val ruleId: String
    val displayName: String
    fun cast(input: TimeCastInput): DivinationOutcome
}

interface NumberDivinationRule {
    val ruleId: String
    val displayName: String
    fun cast(input: NumberCastInput): DivinationOutcome
}
