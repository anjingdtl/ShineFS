package com.shinefs.core.divination.result

import com.shinefs.core.calendar.model.YijingTimeContext
import com.shinefs.core.divination.context.YijingSpaceContext
import com.shinefs.core.divination.manifest.RuleManifest
import com.shinefs.core.divination.trace.CalculationTrace
import com.shinefs.core.divination.trace.CalculationTraceEntry
import com.shinefs.core.yijing.element.SeasonalQi
import com.shinefs.core.yijing.model.ElementRelation
import com.shinefs.core.yijing.model.ElementRelations
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.model.TrigramElements
import com.shinefs.core.yijing.nuclear.NuclearOps
import com.shinefs.core.yijing.nuclear.NuclearPolicy
import com.shinefs.core.yijing.rules.HexagramOps
import com.shinefs.core.yijing.tiyong.TiYongOps

/**
 * 结果装配器：上卦/下卦/动爻一经确定，本卦、变卦、互卦、体用、五行、时令全部确定性推出。
 * 空间数据只进入 [DivinationResult.spatialResponse]（事实层），不修改卦。
 */
object ResultAssembler {

    const val SPATIAL_RESPONSE_RULE_ID = "spatial-response-v1"

    fun assemble(
        manifest: RuleManifest,
        upper: Trigram,
        lower: Trigram,
        changingLine: Int,
        time: YijingTimeContext,
        space: YijingSpaceContext?,
        baseTrace: CalculationTrace,
        nuclearPolicy: NuclearPolicy = NuclearPolicy.STANDARD_234_345,
    ): DivinationResult {
        val original = HexagramOps.fromTrigrams(lower, upper)
        val changed = HexagramOps.withChangingLine(original, changingLine)
        val nuclear = NuclearOps.compute(original, nuclearPolicy)
        val tiYong = TiYongOps.of(original, changingLine)
        val elementRelation = ElementRelations.of(
            TrigramElements.of(tiYong.ti),
            TrigramElements.of(tiYong.yong),
        )
        val seasonalQi = time.solarTerm?.let { SeasonalQi.of(it.term) }
        val spatialResponse = space?.directionTrigram?.let { dir ->
            SpatialResponse(
                directionTrigram = dir,
                relationToTi = ElementRelations.of(TrigramElements.of(tiYong.ti), TrigramElements.of(dir)),
                sourceRuleId = SPATIAL_RESPONSE_RULE_ID,
            )
        }

        val trace = baseTrace + CalculationTrace(
            listOf(
                CalculationTraceEntry("本卦", "上${upper.chineseName} 下${lower.chineseName} → ${original.chineseName}（第${original.kingWenOrder}卦）"),
                CalculationTraceEntry("变卦", "第${changingLine}爻动 → ${changed.chineseName}（第${changed.kingWenOrder}卦）"),
                CalculationTraceEntry("互卦", nuclear?.let { "下互${it.lower.chineseName} 上互${it.upper.chineseName} → ${it.hexagram.chineseName}" } ?: "无（按当前取法）"),
                CalculationTraceEntry("体用", "体${tiYong.ti.chineseName}${TrigramElements.of(tiYong.ti).chinese} 用${tiYong.yong.chineseName}${TrigramElements.of(tiYong.yong).chinese}（动爻在${if (tiYong.movingPart == com.shinefs.core.yijing.tiyong.MovingPart.LOWER) "下" else "上"}卦）"),
                CalculationTraceEntry("五行", "${TrigramElements.of(tiYong.ti).chinese}与${TrigramElements.of(tiYong.yong).chinese} → ${elementRelation.display}"),
                CalculationTraceEntry("时令", seasonalQi?.let { "${it.season.chinese}（当令${it.dominantElement.chinese}${if (it.earthMonth) "，土旺月" else ""}）" } ?: "无节气数据"),
            ),
        )

        return DivinationResult(
            rule = manifest,
            upperTrigram = upper,
            lowerTrigram = lower,
            original = original,
            changingLine = changingLine,
            changed = changed,
            nuclear = nuclear,
            tiYong = tiYong,
            elementRelation = elementRelation,
            seasonalQi = seasonalQi,
            timeContext = time,
            spaceContext = space,
            spatialResponse = spatialResponse,
            trace = trace,
        )
    }
}
