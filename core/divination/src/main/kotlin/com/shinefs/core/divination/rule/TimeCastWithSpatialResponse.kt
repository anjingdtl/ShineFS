package com.shinefs.core.divination.rule

import com.shinefs.core.divination.context.YijingMomentContext
import com.shinefs.core.divination.manifest.RuleManifest
import com.shinefs.core.divination.manifest.RuleStatus
import com.shinefs.core.divination.manifest.RuleSystem
import com.shinefs.core.divination.manifest.SourceRef
import com.shinefs.core.divination.result.DivinationResult
import com.shinefs.core.divination.result.ResultAssembler
import com.shinefs.core.divination.trace.CalculationTrace
import com.shinefs.core.divination.trace.CalculationTraceEntry
import com.shinefs.core.compass.NorthReference

/**
 * 正式起卦模式 C：时间卦 + 罗盘方应（时空合参，V2.0 默认模式）。
 *
 * 时间起卦产出全部卦象结构；罗盘产出二十四山/后天卦/坐向/方应，
 * 只进入事实层与解释层，**不修改时间卦**（V2.0 方案 §10）。
 */
class TimeCastWithSpatialResponse(
    private val timeRule: MeihuaTimeDivinationRuleV1 = MeihuaTimeDivinationRuleV1(),
) {

    val manifest: RuleManifest = RuleManifest(
        ruleId = "time-cast-with-spatial-response-v1",
        version = "1",
        system = RuleSystem.MEIHUA_YISHU_TRADITION,
        sourceRefs = listOf(
            SourceRef("S-B02", "《梅花易数》年月日时起例"),
            SourceRef("S-B09", "《梅花易数》占断总诀方位参断"),
            SourceRef("S-C02", "后天八卦方位领三山"),
        ),
        assumptions = listOf(
            "空间数据不修改时间卦（V2.0 方案 §10 红线）",
            "无空间数据（纯时间模式）时照常成卦，方应为空",
        ),
        status = RuleStatus.VERIFIED_WITH_EXPLICIT_ASSUMPTIONS,
    )

    fun cast(moment: YijingMomentContext): DivinationResult {
        val timeResult = timeRule.cast(moment.time)
        val space = moment.space
        if (space == null) return timeResult

        // 重建含空间的完整结果（ResultAssembler 保持确定性；不改动 A 法任何取数）
        val extraTrace = CalculationTrace(
            listOfNotNull(
                space.smoothedAzimuth?.let {
                    CalculationTraceEntry(
                        "方位角",
                        String.format(java.util.Locale.ROOT, "%.1f°", it) + "（${northReferenceLabel(space.northReference)}）",
                    )
                },
                space.facingMountain?.let { CalculationTraceEntry("向山", it) },
                space.sittingMountain?.let { CalculationTraceEntry("坐山", it) },
                space.directionTrigram?.let {
                    CalculationTraceEntry(
                        "方位卦",
                        "${it.chineseName}（${TrigramElementsCh(it)}）",
                    )
                },
            ),
        )
        val merged = ResultAssembler.assemble(
            timeResult.rule,
            timeResult.upperTrigram,
            timeResult.lowerTrigram,
            timeResult.changingLine,
            moment.time,
            space,
            timeResult.trace + extraTrace,
        )
        return merged
    }

    private fun TrigramElementsCh(t: com.shinefs.core.yijing.model.Trigram): String =
        com.shinefs.core.yijing.model.TrigramElements.of(t).chinese

    private fun northReferenceLabel(reference: NorthReference): String = when (reference) {
        NorthReference.MAGNETIC -> "磁北"
        NorthReference.TRUE -> "真北"
    }
}
