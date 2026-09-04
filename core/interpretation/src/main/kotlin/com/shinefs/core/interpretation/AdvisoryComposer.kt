package com.shinefs.core.interpretation

import com.shinefs.core.classics.CanonicalCorpus
import com.shinefs.core.classics.ClassicCorpus
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
            "公历：${r.timeContext.civil.year}-${r.timeContext.civil.month}-${r.timeContext.civil.day} ${r.timeContext.civil.hour}:${String.format(java.util.Locale.ROOT, "%02d", r.timeContext.civil.minute)}（${r.timeContext.zoneId}）",
            "农历：${r.timeContext.lunarDisplay}",
            "干支：${r.timeContext.dayGanzhi.name}日 ${r.timeContext.shichen.display}",
            "节气：${r.timeContext.solarTerm?.term?.chinese ?: "无数据"}（月建${r.timeContext.monthBranch?.chinese ?: "?"}）",
            "历法：${r.timeContext.calendarVersion}；日界 ${r.timeContext.dayBoundaryPolicy}；闰月 ${r.timeContext.leapMonthPolicy}",
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
            lines.add("原典版本：${corpus.version}（${corpus.edition}）${if (text.verified) "，已核定" else "，未核定"}")
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
            } ?: "互卦：无（策略生效）",
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
                "方位角：${sp.smoothedAzimuth?.let { String.format(java.util.Locale.ROOT, "%.1f", it) } ?: "无"}°（北参考：${sp.northReference}）",
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
            lines.add("传感器：稳定=${if (sp.stable) "是" else "否"}，磁扰=${if (sp.magneticInterference) "有" else "无"}")
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
            lines.add("（以上为本地规则引擎依既定模板生成，非 AI 生成，不含随机成分。）")
        }
        return InterpretationSection("八、本地白话释义", lines)
    }

    /** 九、规则来源与版本 */
    private fun sectionNine(r: DivinationResult) = InterpretationSection(
        "九、规则来源与版本",
        listOf(
            "起卦体系：${r.rule.ruleId} v${r.rule.version}（${r.rule.system}）",
            "规则来源：" + r.rule.sourceRefs.joinToString("；") { "${it.sourceId} ${it.title}" },
            "显式假设：" + r.rule.assumptions.joinToString("；"),
            "规则状态：${r.rule.status}",
            "经典体系：${corpus.version}；历法：${r.timeContext.calendarVersion}；北参考：${r.spaceContext?.northReference ?: "未用"}",
            "解释器：$interpreterVersion（0 AI / 0 随机 / 0 网络）",
        ),
    )

    private fun lineName(line: Int): String = when (line) {
        1 -> "初爻"
        2 -> "二爻"
        3 -> "三爻"
        4 -> "四爻"
        5 -> "五爻"
        else -> "上爻"
    }
}
