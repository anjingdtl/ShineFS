package com.shinefs.core.divination.result

import com.shinefs.core.calendar.model.YijingTimeContext
import com.shinefs.core.divination.context.YijingSpaceContext
import com.shinefs.core.divination.manifest.RuleManifest
import com.shinefs.core.divination.trace.CalculationTrace
import com.shinefs.core.yijing.element.SeasonalQiContext
import com.shinefs.core.yijing.model.ElementRelation
import com.shinefs.core.yijing.model.Hexagram
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.nuclear.NuclearHexagram
import com.shinefs.core.yijing.tiyong.TiYong

/** 空间方应事实层（`spatial-response-v1`，B 级）：方位卦与体卦的五行关系描述，不改卦。 */
data class SpatialResponse(
    val directionTrigram: Trigram,
    val relationToTi: ElementRelation,
    val sourceRuleId: String,
)

/** 起卦正式结果（V2.0 方案 §21）。 */
data class DivinationResult(
    val rule: RuleManifest,
    val upperTrigram: Trigram,
    val lowerTrigram: Trigram,
    val original: Hexagram,
    val changingLine: Int,
    val changed: Hexagram,
    val nuclear: NuclearHexagram?,
    val tiYong: TiYong?,
    val elementRelation: ElementRelation?,
    val seasonalQi: SeasonalQiContext?,
    val timeContext: YijingTimeContext,
    val spaceContext: YijingSpaceContext?,
    val spatialResponse: SpatialResponse?,
    val trace: CalculationTrace,
)
