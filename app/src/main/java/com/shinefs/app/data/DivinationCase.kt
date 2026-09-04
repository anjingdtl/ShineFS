package com.shinefs.app.data

/**
 * 卦例（V2.0 方案 §28）：完整留存复算所需的全部输入与规则版本。
 *
 * V2 字段为可空/带默认——Room schema v2 由 v1 迁移而来，旧（V1 fixture）记录
 * 无这些字段值，[legacyFixture] = true 标记"仅可查看，非 V2 正式结果"（Cycle 10I）。
 */
data class DivinationCase(
    val id: String,
    val timestamp: Long,
    val sceneId: String,
    val sceneName: String,
    // 空间（V1 兼容）
    val azimuth: Float?,
    val facingMountain: String?,
    val sittingMountain: String?,
    val facingTrigram: String?,
    val facingElement: String?,
    val stability: String?,
    // 规则标识（V1 兼容）
    val ruleId: String,
    val ruleDisplayName: String,
    val rulesVersion: String,
    val interpretationVersion: String,
    // 卦象结构（V1 兼容）
    val upperTrigram: String,
    val lowerTrigram: String,
    val originalHexagramOrder: Int,
    val originalHexagramName: String,
    val changingLine: Int,
    val changedHexagramOrder: Int,
    val changedHexagramName: String,
    // ---- V2 新增（方案 §28）----
    /** 起卦模式：TIME（纯时间）/ TIME_SPACE（时空合参）。 */
    val castMode: String? = null,
    val zoneId: String? = null,
    val calendarVersion: String? = null,
    val ruleVersion: String? = null,
    val classicCorpusVersion: String? = null,
    val dayBoundaryPolicy: String? = null,
    val leapMonthPolicy: String? = null,
    val northReference: String? = null,
    val rawAzimuth: Float? = null,
    val smoothedAzimuth: Float? = null,
    val lunarYear: Int? = null,
    val lunarMonth: Int? = null,
    val lunarDay: Int? = null,
    val leapMonthFlag: Boolean? = null,
    val yearBranch: String? = null,
    val hourBranch: String? = null,
    /** 起卦四数（复算直接输入）。 */
    val yearBranchNumber: Int? = null,
    val lunarMonthNumber: Int? = null,
    val lunarDayNumber: Int? = null,
    val hourBranchNumber: Int? = null,
    val nuclearHexagramOrder: Int? = null,
    val nuclearHexagramName: String? = null,
    val tiTrigram: String? = null,
    val yongTrigram: String? = null,
    val elementRelation: String? = null,
    val seasonalQi: String? = null,
    val solarTerm: String? = null,
    /** 完整 CalculationTrace（人读格式）。 */
    val calculationTrace: String? = null,
    /** 九段本地报告全文（interpret-v1 生成，随例留存）。 */
    val reportText: String? = null,
    /** V1 Fixture 卦例标记（Cycle 10I：仅查看，不伪装 V2 正式结果）。 */
    val legacyFixture: Boolean = false,
    // 通用
    val houseAuditId: String? = null,
    val favorite: Boolean = false,
    val note: String? = null,
) {
    companion object {
        const val RULES_VERSION = "rules-v2.0"
        const val INTERPRETATION_VERSION = "interpret-v1"
        const val CAST_MODE_TIME = "TIME"
        const val CAST_MODE_TIME_SPACE = "TIME_SPACE"
    }
}
