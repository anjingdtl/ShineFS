package com.shinefs.core.divination.rule

import com.shinefs.core.calendar.model.YijingTimeContext
import com.shinefs.core.divination.manifest.RuleManifest
import com.shinefs.core.divination.manifest.RuleStatus
import com.shinefs.core.divination.manifest.RuleSystem
import com.shinefs.core.divination.manifest.SourceRef
import com.shinefs.core.divination.result.DivinationResult
import com.shinefs.core.divination.result.ResultAssembler
import com.shinefs.core.divination.trace.CalculationTrace
import com.shinefs.core.divination.trace.CalculationTraceEntry
import com.shinefs.core.yijing.model.Trigram

/**
 * 余数归一（`normalize-remainder-v1`，B 级，DOCS/YIJING_RULES.md §6.0）：
 * 余 0 取 8（坤）/ 余 0 取 6（上爻）。金标准古例全部按此复算通过。
 */
object MeihuaMath {
    fun normalize8(n: Int): Int = ((n - 1) % 8) + 1

    fun normalize6(n: Int): Int = ((n - 1) % 6) + 1
}

/**
 * 正式起卦模式 A：梅花年月日时（`meihua-time-v1`，B 级）。
 *
 * base = 年支数 + 农历月数 + 农历日数；
 * 上卦 = normalize8(base)；下卦 = normalize8(base + 时辰数)；动爻 = normalize6(base + 时辰数)。
 *
 * 假设（详见 DOCS/RULE_MANIFEST.md）：年界=农历正月初一（观梅占反推）；
 * 闰月 SAME_MONTH_NUMBER；日界随 YijingTimeContext 策略。
 */
class MeihuaTimeDivinationRuleV1 {

    val manifest: RuleManifest = RuleManifest(
        ruleId = "meihua-time-v1",
        version = "1",
        system = RuleSystem.MEIHUA_YISHU_TRADITION,
        sourceRefs = listOf(
            SourceRef("S-B02", "《梅花易数》年月日时起例"),
            SourceRef("S-E04", "十二时辰划分"),
            SourceRef("S-E05", "年支数/农历月日取数"),
        ),
        assumptions = listOf(
            "年支数以农历正月初一为界（观梅占'辰年十二月'反推；TD-V2-03 立春界不启用）",
            "闰月取同月号（SAME_MONTH_NUMBER，显式工程政策）",
            "日界随 YijingTimeContext.dayBoundaryPolicy（默认民用午夜）",
        ),
        status = RuleStatus.VERIFIED_WITH_EXPLICIT_ASSUMPTIONS,
    )

    fun cast(time: YijingTimeContext): DivinationResult {
        val base = time.yearBranchNumber + time.lunarMonthNumber + time.lunarDayNumber
        val upperNumber = MeihuaMath.normalize8(base)
        val lowerNumber = MeihuaMath.normalize8(base + time.hourBranchNumber)
        val changingLine = MeihuaMath.normalize6(base + time.hourBranchNumber)
        val upper = Trigram.fromXiantianNumber(upperNumber)
        val lower = Trigram.fromXiantianNumber(lowerNumber)

        val trace = CalculationTrace(
            listOf(
                CalculationTraceEntry("年支", "${time.yearBranch.chinese} → ${time.yearBranchNumber}"),
                CalculationTraceEntry("农历月", "第${time.lunarMonthNumber}月${if (time.leapMonth) "（闰）" else ""} → ${time.lunarMonthNumber}"),
                CalculationTraceEntry("农历日", "第${time.lunarDayNumber}日 → ${time.lunarDayNumber}"),
                CalculationTraceEntry("时辰", "${time.hourBranch.chinese}时 → ${time.hourBranchNumber}"),
                CalculationTraceEntry("基数", "$base = ${time.yearBranchNumber}+${time.lunarMonthNumber}+${time.lunarDayNumber}"),
                CalculationTraceEntry("上卦", "$base 除 8 余 $upperNumber → ${upper.chineseName}"),
                CalculationTraceEntry("下卦", "${base + time.hourBranchNumber} 除 8 余 $lowerNumber → ${lower.chineseName}"),
                CalculationTraceEntry("动爻", "${base + time.hourBranchNumber} 除 6 余 $changingLine → 第${changingLine}爻"),
            ),
        )
        return ResultAssembler.assemble(manifest, upper, lower, changingLine, time, null, trace)
    }
}

/**
 * 正式起卦模式 B：梅花后天端法（`meihua-postheaven-v1`，B 级）。
 *
 * 物象为上卦（先天数）、方位为下卦（方位所属后天八卦的先天数）；
 * 动爻 = normalize6(物象数 + 方位数 + 时辰数)。物象仅出自版本化类象表。
 */
class MeihuaPostHeavenObjectDirectionRuleV1 {

    val manifest: RuleManifest = RuleManifest(
        ruleId = "meihua-postheaven-v1",
        version = "1",
        system = RuleSystem.MEIHUA_YISHU_TRADITION,
        sourceRefs = listOf(
            SourceRef("S-B03", "《梅花易数》端法后天起卦"),
            SourceRef("S-B04", "类象表（说卦明文）"),
        ),
        assumptions = listOf(
            "物象配卦仅出自 meihua-classimage-v1 类象表（不扩充现代配卦）",
            "方位卦 = 方位角所属后天八卦（C 级 later-heaven-bagua-v1）",
            "时辰数取起念/观物之时，由调用方通过 YijingTimeContext 给出",
        ),
        status = RuleStatus.VERIFIED_WITH_EXPLICIT_ASSUMPTIONS,
    )

    fun cast(
        objectTrigram: Trigram,
        objectLabel: String,
        directionTrigram: Trigram,
        time: YijingTimeContext,
    ): DivinationResult {
        val objectNumber = objectTrigram.xiantianNumber
        val directionNumber = directionTrigram.xiantianNumber
        val changingLine = MeihuaMath.normalize6(objectNumber + directionNumber + time.hourBranchNumber)

        val trace = CalculationTrace(
            listOf(
                CalculationTraceEntry("物象", "$objectLabel → ${objectTrigram.chineseName} → $objectNumber"),
                CalculationTraceEntry("方位", "${directionTrigram.chineseName}（后天方位卦）→ $directionNumber"),
                CalculationTraceEntry("时辰", "${time.hourBranch.chinese}时 → ${time.hourBranchNumber}"),
                CalculationTraceEntry("动爻", "$objectNumber+$directionNumber+${time.hourBranchNumber} = ${objectNumber + directionNumber + time.hourBranchNumber} 除 6 余 $changingLine → 第${changingLine}爻"),
                CalculationTraceEntry("成卦", "上${objectTrigram.chineseName} 下${directionTrigram.chineseName}"),
            ),
        )
        return ResultAssembler.assemble(
            manifest, objectTrigram, directionTrigram, changingLine, time, null, trace,
        )
    }
}
