package com.shinefs.core.yijing.divination

import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.rules.LaterHeavenBagua
import java.time.Instant
import java.time.ZoneId

/**
 * ⚠️ 临时联调规则（Fixture）——**不是正式周易规则**。
 *
 * 正式部分（产品方案 §4.2 模式 A，已定）：
 *   上卦 = 向首方位所属后天八卦
 *
 * 临时部分（仅用于 UI/流程联调，待决策 D-01/D-04/D-05 拍板后整体替换）：
 *   下卦：公历(年+月+日+24小时制时) 之和 mod 8，余 0 记 8（先天卦数）
 *   动爻：公历(年+月+日+时+分) 之和 mod 6，余 0 记 6
 *
 * 时间口径为公历数字直取，这是**临时选择**而非术数定论；
 * 正式口径（农历/干支/梅花易数取数等）见 DOCS/YIJING_RULES.md 待决策项。
 */
class FixtureDirectionRule(private val timeZone: ZoneId) : DirectionDivinationRule {

    override val ruleId: String = "fixture-direction"
    override val displayName: String = "方位起卦 · 临时联调口径（非正式）"

    override fun cast(input: DirectionCastInput): DivinationOutcome {
        val azimuth = ((input.azimuth % 360f) + 360f) % 360f
        val upper = LaterHeavenBagua.trigramAt(azimuth)

        // Fixture tests must not inherit the runner's default timezone; production
        // V2.1 paths inject the device timezone explicitly at the app boundary.
        val t = Instant.ofEpochMilli(input.epochMillis).atZone(timeZone)
        val y = t.year
        val mo = t.monthValue
        val d = t.dayOfMonth
        val h = t.hour
        val mi = t.minute

        val lowerNum = (y + mo + d + h) % 8
        val lower = Trigram.entries[if (lowerNum == 0) 7 else lowerNum - 1]
        val line = (y + mo + d + h + mi) % 6
        val changingLine = if (line == 0) 6 else line

        return DivinationOutcome(
            ruleId = ruleId,
            upperTrigram = upper,
            lowerTrigram = lower,
            changingLine = changingLine,
        )
    }
}
