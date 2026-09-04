package com.shinefs.core.divination.manifest

/** 规则所属术数体系（V2.0 方案 §1.4 分层，禁止混写）。 */
enum class RuleSystem {
    /** 《周易》经传正典（A 级）。 */
    YIJING_CLASSIC,

    /** 梅花易数 / 邵氏象数传统（B 级）。 */
    MEIHUA_YISHU_TRADITION,

    /** 罗经 / 地理术数传统（C 级）。 */
    LUOJING_GEOGRAPHY,

    /** 历法工程政策（E 级）。 */
    CALENDAR_ENGINEERING,
}

/** 规则状态（DOCS/RULE_MANIFEST.md §0）。 */
enum class RuleStatus {
    VERIFIED,
    VERIFIED_WITH_EXPLICIT_ASSUMPTIONS,
    ENGINEERING_POLICY,
    PENDING,
}

/** 文献/数据来源引用（编号见 DOCS/SOURCE_CATALOG.md）。 */
data class SourceRef(
    val sourceId: String,
    val title: String,
    val locator: String? = null,
)

/**
 * 规则清单条目（V2.0 方案 §19）：每条正式规则必须能回答"从哪里来"。
 * 与 DOCS/RULE_MANIFEST.md 人读总账同步维护。
 */
data class RuleManifest(
    val ruleId: String,
    val version: String,
    val system: RuleSystem,
    val sourceRefs: List<SourceRef>,
    val assumptions: List<String>,
    val status: RuleStatus,
)
