package com.shinefs.core.interpretation

/** 报告分节（V2.0 方案 §24 固定九段）。 */
data class InterpretationSection(
    val title: String,
    val lines: List<String>,
) {
    fun render(): String = buildString {
        appendLine(title)
        lines.forEach { appendLine(it) }
    }
}

/**
 * 本地规则解释报告：全部由"规则模板 + 结构化变量"确定性生成（V2.0 方案 §22/§23）。
 * 相同 DivinationResult + 相同解释器版本 → 完全相同报告。
 */
data class InterpretationReport(
    val sections: List<InterpretationSection>,
    val interpreterVersion: String,
) {
    fun render(): String = sections.joinToString("\n") { it.render() }
}
