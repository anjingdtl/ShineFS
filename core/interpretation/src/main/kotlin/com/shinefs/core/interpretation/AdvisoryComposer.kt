package com.shinefs.core.interpretation

import com.shinefs.core.classics.CanonicalCorpus
import com.shinefs.core.classics.ClassicCorpus
import com.shinefs.core.calendar.model.DayBoundaryPolicy
import com.shinefs.core.calendar.model.LeapMonthPolicy
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.divination.manifest.RuleStatus
import com.shinefs.core.divination.result.DivinationResult
import com.shinefs.core.interpretation.interpreters.ElementInterpreter
import com.shinefs.core.interpretation.interpreters.LinePositionInterpreter
import com.shinefs.core.interpretation.interpreters.SeasonalInterpreter
import com.shinefs.core.interpretation.interpreters.SpatialResponseInterpreter
import com.shinefs.core.yijing.model.TrigramElements

/**
 * 本地白话释义总装配（V2.0 方案 §22/§24）：固定九段报告。
 *
 * 解释规则：模板 + 结构化变量，0 AI、0 随机、0 网络；
 * 相同 [DivinationResult] → 完全相同报告（interpret-v1）。
 */
class AdvisoryComposer(
    private val corpus: ClassicCorpus = CanonicalCorpus,
) {
    val interpreterVersion: String = "interpret-v1"

    fun compose(result: DivinationResult): InterpretationReport {
        val t = result.timeContext
        val tiElement = result.tiYong?.let { TrigramElements.of(it.ti) }
        val sections = listOf(
            sectionOne(result),
            sectionTwo(result),
            sectionThree(result),
            sectionFour(result),
            sectionFive(result),
            sectionSix(result),
            sectionSeven(result),
            sectionEight(result),
            sectionNine(result),
        )
        return InterpretationReport(sections = sections, interpreterVersion = interpreterVersion)
    }

    /** 一、时空数据 */
    private fun sectionOne(r: DivinationResult) = InterpretationSection(
        "一、时空数据",
        listOf(
            "公历：${r.timeContext.civil.year}-${r.timeContext.civil.month}-${r.timeContext.civil.day} ${r.timeContext.civil.hour}:${String.format(java.util.Locale.ROOT, "%02d", r.timeContext.civil.minute)}（${timeZoneLabel(r.timeContext.zoneId)}）",
            "农历：${r.timeContext.lunarDisplay}",
            "干支：${r.timeContext.dayGanzhi.name}日 ${r.timeContext.shichen.display}",
            "节气：${r.timeContext.solarTerm?.term?.chinese ?: "无数据"}（月建${r.timeContext.monthBranch?.chinese ?: "?"}）",
            "历法：传统农历历表；换日：${dayBoundaryLabel(r.timeContext.dayBoundaryPolicy)}；闰月：${leapMonthLabel(r.timeContext.leapMonthPolicy)}",
        ),
    )

    /** 二、起卦过程 */
    private fun sectionTwo(r: DivinationResult) = InterpretationSection(
        "二、起卦过程",
        r.trace.entries.map { "${it.step}：${it.detail}" },
    )

    /** 三、卦象结果 */
    private fun sectionThree(r: DivinationResult) = InterpretationSection(
        "三、卦象结果",
        listOf(
            "本卦：第${r.original.kingWenOrder}卦 ${r.original.chineseName} ${r.original.symbol}（上${r.upperTrigram.chineseName} 下${r.lowerTrigram.chineseName}）",
            "动爻：第${r.changingLine}爻（自下而上）",
            "变卦：第${r.changed.kingWenOrder}卦 ${r.changed.chineseName} ${r.changed.symbol}",
        ),
    )

    /** 四、周易原典 */
    private fun sectionFour(r: DivinationResult): InterpretationSection {
        val text = corpus.byKingWenOrder(r.original.kingWenOrder)
        val lines = mutableListOf<String>()
        if (text == null) {
            lines.add("原典缺失（不应发生）")
        } else {
            lines.add("《周易》${text.name}卦·卦辞：${text.judgment}")
            text.tuan?.let { lines.add("彖曰：$it") }
            text.greatImage?.let { lines.add("象曰：$it") }
            val lineText = text.lines.firstOrNull { it.line == r.changingLine }
            lineText?.let {
                lines.add("动爻（${lineName(r.changingLine)}）：《周易》${text.name}卦：${it.text}")
                it.smallImage?.let { si -> lines.add("小象曰：$si") }
            }
            text.specialUseText?.let { lines.add("特爻：$it") }
            text.specialUseSmallImage?.takeIf { r.changingLine == 6 && text.kingWenOrder in 1..2 }
                ?.let { lines.add("小象曰：$it") }
            lines.add("原典：周易通行本电子底本（${corpus.verificationStatus.label}）")
            if (text.textualVariants.isNotEmpty()) {
                lines.add("校勘注记：${text.textualVariants.joinToString("；")}")
            }
        }
        return InterpretationSection("四、周易原典", lines)
    }

    /** 五、互卦与体用 */
    private fun sectionFive(r: DivinationResult) = InterpretationSection(
        "五、互卦与体用",
        listOfNotNull(
            r.nuclear?.let {
                "互卦：上互${it.upper.chineseName} 下互${it.lower.chineseName} → 第${it.hexagram.kingWenOrder}卦 ${it.hexagram.chineseName}（事之中间环节）"
            } ?: "互卦：无（按当前取法）",
            r.tiYong?.let { ty ->
                "体用：动爻在第${r.changingLine}爻（${if (ty.movingPart == com.shinefs.core.yijing.tiyong.MovingPart.LOWER) "下" else "上"}卦），" +
                    "体卦${ty.ti.chineseName}（${TrigramElements.of(ty.ti).chinese}），用卦${ty.yong.chineseName}（${TrigramElements.of(ty.yong).chinese}）"
            },
        ),
    )

    /** 六、五行与时令 */
    private fun sectionSix(r: DivinationResult): InterpretationSection {
        val lines = mutableListOf<String>()
        val ty = r.tiYong
        val relation = r.elementRelation
        if (ty != null && relation != null) {
            val tiE = TrigramElements.of(ty.ti)
            val yongE = TrigramElements.of(ty.yong)
            lines.add("五行：体${ty.ti.chineseName}${tiE.chinese}、用${ty.yong.chineseName}${yongE.chinese}，${relation.display}。")
            lines.add(ElementInterpreter.relationTemplate(relation, tiE, yongE))
        }
        val qi = r.seasonalQi
        if (qi != null && ty != null) {
            lines.add(SeasonalInterpreter.interpret(qi, TrigramElements.of(ty.ti)))
        }
        return InterpretationSection("六、五行与时令", lines)
    }

    /** 七、方位与方应 */
    private fun sectionSeven(r: DivinationResult): InterpretationSection {
        val sp = r.spaceContext
        val lines = mutableListOf<String>()
        if (sp == null) {
            lines.add("本次起卦未含空间数据（纯时间卦）。")
        } else {
            lines.add(
                "方位角：${sp.smoothedAzimuth?.let { String.format(java.util.Locale.ROOT, "%.1f", it) } ?: "无"}°（以${northReferenceLabel(sp.northReference)}为北）",
            )
            lines.add(
                SpatialResponseInterpreter.interpret(
                    facingMountain = sp.facingMountain,
                    sittingMountain = sp.sittingMountain,
                    directionTrigram = r.spatialResponse?.directionTrigram,
                    relationToTi = r.spatialResponse?.relationToTi,
                    tiElement = r.tiYong?.let { TrigramElements.of(it.ti) } ?: com.shinefs.core.yijing.model.Element.EARTH,
                ),
            )
            lines.add(
                "测量状态：${if (sp.stable) "方位稳定" else "方位未稳"}，" +
                    if (sp.magneticInterference) "磁场存在干扰" else "磁场正常",
            )
        }
        return InterpretationSection("七、方位与方应", lines)
    }

    /** 八、本地白话释义 */
    private fun sectionEight(r: DivinationResult): InterpretationSection {
        val lines = mutableListOf<String>()
        val ty = r.tiYong
        val relation = r.elementRelation
        if (ty != null && relation != null) {
            lines.add(
                "本卦${r.original.chineseName}变为${r.changed.chineseName}：事情从「${r.original.chineseName}」的局面，" +
                    "经第${r.changingLine}爻之动，趋向「${r.changed.chineseName}」的结果。",
            )
            lines.add(ElementInterpreter.relationTemplate(relation, TrigramElements.of(ty.ti), TrigramElements.of(ty.yong)))
            r.nuclear?.let { lines.add("中间过程可参互卦${it.hexagram.chineseName}，是事情的过渡环节。") }
            lines.add(LinePositionInterpreter.describe(r.changingLine))
            r.seasonalQi?.let { lines.add(SeasonalInterpreter.interpret(it, TrigramElements.of(ty.ti))) }
            r.spatialResponse?.let { sr ->
                lines.add(
                    "空间上，" + SpatialResponseInterpreter.interpret(
                        r.spaceContext?.facingMountain,
                        r.spaceContext?.sittingMountain,
                        sr.directionTrigram,
                        sr.relationToTi,
                        TrigramElements.of(ty.ti),
                    ),
                )
            }
            lines.add("（以上内容依传统规则固定生成，不使用智能生成或随机内容。）")
        }
        return InterpretationSection("八、本地白话释义", lines)
    }

    /** 九、起卦依据与说明 */
    private fun sectionNine(r: DivinationResult) = InterpretationSection(
        "九、起卦依据与说明",
        listOf(
            "起卦方法：${ruleDisplayName(r.rule.ruleId)}",
            "依据出处：" + r.rule.sourceRefs.joinToString("；") { it.title },
            "说明：${ruleAssumptions(r.rule.ruleId)}",
            "核对状态：${ruleStatusLabel(r.rule.status)}",
            "典籍：周易通行本电子底本（${corpus.verificationStatus.label}）；历法：传统农历历表；方位基准：${r.spaceContext?.let { northReferenceLabel(it.northReference) } ?: "未使用"}",
            "解读方式：本地固定规则，不使用智能生成、不含随机内容、无需联网",
        ),
    )

    private fun ruleDisplayName(ruleId: String): String = when (ruleId) {
        "meihua-time-v1" -> "梅花易数 · 年月日时起卦"
        "time-cast-with-spatial-response-v1" -> "时空合参 · 时间卦与罗盘方应"
        "meihua-postheaven-v1" -> "梅花易数 · 后天端法（物象方位）"
        else -> "传统起卦方法"
    }

    private fun ruleAssumptions(ruleId: String): String = when (ruleId) {
        "meihua-time-v1" -> "年支以农历正月初一为界；闰月沿用本月序号；换日时刻按当前设置。"
        "time-cast-with-spatial-response-v1" -> "时间决定卦象，罗盘只补充方位信息，不改变时间起卦结果。"
        "meihua-postheaven-v1" -> "物象取卦只采用《说卦》明文；方位取后天八卦。"
        else -> "按当前页面所选方法与时间、方位取数。"
    }

    private fun ruleStatusLabel(status: RuleStatus): String = when (status) {
        RuleStatus.VERIFIED -> "已核定"
        RuleStatus.VERIFIED_WITH_EXPLICIT_ASSUMPTIONS -> "已核定，约定清楚"
        RuleStatus.ENGINEERING_POLICY -> "按既定约定执行"
        RuleStatus.PENDING -> "待核对"
    }

    private fun dayBoundaryLabel(policy: DayBoundaryPolicy): String = when (policy) {
        DayBoundaryPolicy.CIVIL_MIDNIGHT -> "民用午夜（00:00）"
        DayBoundaryPolicy.ZI_HOUR_START_23 -> "晚子时（23:00）"
    }

    private fun leapMonthLabel(policy: LeapMonthPolicy): String = when (policy) {
        LeapMonthPolicy.SAME_MONTH_NUMBER -> "闰月沿用本月序号"
    }

    private fun northReferenceLabel(reference: NorthReference): String = when (reference) {
        NorthReference.MAGNETIC -> "磁北"
        NorthReference.TRUE -> "真北"
    }

    private fun timeZoneLabel(zoneId: String): String =
        if (zoneId == "Asia/Shanghai") "中国标准时间" else "当地时间"

    private fun lineName(line: Int): String = when (line) {
        1 -> "初爻"
        2 -> "二爻"
        3 -> "三爻"
        4 -> "四爻"
        5 -> "五爻"
        else -> "上爻"
    }
}
