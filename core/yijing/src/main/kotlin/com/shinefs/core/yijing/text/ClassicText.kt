package com.shinefs.core.yijing.text

/**
 * 六十四卦原典文本（卦辞、象辞、爻辞）。
 *
 * ⚠️ 数据治理（产品方案 §0.5/待决策 D-09）：
 * - [verified] == true 的数据必须来自人工核定底本的版本化数据文件；
 * - fixture 数据 [verified] == false，仅供联调展示，UI 必须显著标注；
 * - 禁止由大模型生成经典原文并标记为已核定。
 */
data class ClassicHexagramText(
    val kingWenOrder: Int,
    val hexagramName: String,
    val judgment: String,
    val imageText: String,
    val lineTexts: List<String>,
    val version: String,
    val verified: Boolean,
) {
    val hasLineTexts: Boolean get() = lineTexts.isNotEmpty()
}

/** 原典仓储：Cycle 05 起接口化；正式数据待 D-09 核定后以版本化文件实现。 */
interface ClassicTextRepository {
    val version: String
    fun byKingWenOrder(order: Int): ClassicHexagramText?
}
