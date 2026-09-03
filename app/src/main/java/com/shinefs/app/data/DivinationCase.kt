package com.shinefs.app.data

import com.shinefs.core.yijing.divination.DivinationOutcome

/**
 * 卦例（产品方案 §5.4，映射为 V1 实用形态）。
 * interpretationVersion = 解释数据版本；rulesVersion = 术数规则版本（YIJING_RULES）。
 */
data class DivinationCase(
    val id: String,
    val timestamp: Long,
    val sceneId: String,
    val sceneName: String,
    val azimuth: Float?,
    val facingMountain: String?,
    val sittingMountain: String?,
    val facingTrigram: String?,
    val facingElement: String?,
    val stability: String?,
    val ruleId: String,
    val ruleDisplayName: String,
    val rulesVersion: String,
    val interpretationVersion: String,
    val upperTrigram: String,
    val lowerTrigram: String,
    val originalHexagramOrder: Int,
    val originalHexagramName: String,
    val changingLine: Int,
    val changedHexagramOrder: Int,
    val changedHexagramName: String,
    val houseAuditId: String? = null,
    val favorite: Boolean = false,
    val note: String? = null,
) {
    companion object {
        const val RULES_VERSION = "rules-v0.1"
        const val INTERPRETATION_VERSION = "interp-v0.1-fixture"
    }
}

fun DivinationOutcome.toCaseFields(): Triple<String, String, Int> =
    Triple(upperTrigram.chineseName, lowerTrigram.chineseName, changingLine)
