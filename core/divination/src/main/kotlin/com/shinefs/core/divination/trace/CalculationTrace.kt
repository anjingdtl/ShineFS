package com.shinefs.core.divination.trace

/**
 * 演算轨迹（V2.0 方案 §20）：每一步取数与推导必须可复算。
 * 用于 UI 展示、测试断言、Bug 定位、历史复算与规则迁移比对。
 */
data class CalculationTraceEntry(
    val step: String,
    val detail: String,
)

data class CalculationTrace(
    val entries: List<CalculationTraceEntry>,
) {
    fun render(): String = entries.joinToString("\n") { "${it.step}：${it.detail}" }

    operator fun plus(other: CalculationTrace): CalculationTrace =
        CalculationTrace(entries + other.entries)
}
