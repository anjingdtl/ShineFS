package com.shinefs.app.data

/**
 * 整宅测局摘要（纯函数构建）：逐场景坐向与卦象 + 五行分布；
 * V1.0 不做飞星/流派综合断语（产品方案 §Cycle 06 边界）。
 */
data class HouseAuditEntry(
    val sceneId: String,
    val sceneName: String,
    val caseId: String,
    val facingMountain: String?,
    val sittingMountain: String?,
    val element: String?,
    val hexagramName: String,
    val changingLine: Int,
    val changedHexagramName: String,
)

data class HouseAuditSummary(
    val auditId: String,
    val measuredCount: Int,
    val totalCount: Int,
    val entries: List<HouseAuditEntry>,
    val elementCounts: Map<String, Int>,
) {
    val complete: Boolean get() = measuredCount >= totalCount
}

object HouseSummarizer {
    fun summarize(auditId: String, cases: List<DivinationCase>): HouseAuditSummary {
        val byScene = cases.filter { it.houseAuditId == auditId }.associateBy { it.sceneId }
        val entries = Scenes.house.mapNotNull { scene ->
            byScene[scene.id]?.let { c ->
                HouseAuditEntry(
                    sceneId = scene.id,
                    sceneName = scene.name,
                    caseId = c.id,
                    facingMountain = c.facingMountain,
                    sittingMountain = c.sittingMountain,
                    element = c.facingElement,
                    hexagramName = c.originalHexagramName,
                    changingLine = c.changingLine,
                    changedHexagramName = c.changedHexagramName,
                )
            }
        }
        val counts = entries.mapNotNull { it.element }.groupingBy { it }.eachCount()
        return HouseAuditSummary(
            auditId = auditId,
            measuredCount = entries.size,
            totalCount = Scenes.house.size,
            entries = entries,
            elementCounts = counts,
        )
    }

    fun summaryText(summary: HouseAuditSummary): String = buildString {
        appendLine("已测 ${summary.measuredCount}/${summary.totalCount} 处。")
        summary.entries.forEach { e ->
            appendLine("· ${e.sceneName}：向${e.facingMountain}坐${e.sittingMountain}（${e.element}）——《${e.hexagramName}》${e.changingLine}爻动变《${e.changedHexagramName}》")
        }
        if (summary.elementCounts.isNotEmpty()) {
            append("向方五行分布：" + summary.elementCounts.entries.joinToString("、") { "${it.key}×${it.value}" } + "。")
        }
        append("V1.0 摘要仅汇总各处测量与卦象，不做飞星等综合断语。")
    }
}
