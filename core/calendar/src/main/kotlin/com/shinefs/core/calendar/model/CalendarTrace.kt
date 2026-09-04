package com.shinefs.core.calendar.model

/** 历法换算轨迹（V2.0 方案 §20 可复算要求的时间部分）。 */
data class CalendarTraceEntry(
    val key: String,
    val value: String,
    val note: String? = null,
)

data class CalendarTrace(
    val entries: List<CalendarTraceEntry>,
) {
    fun render(): String = entries.joinToString("\n") { e ->
        if (e.note == null) "${e.key}：${e.value}" else "${e.key}：${e.value}（${e.note}）"
    }
}
