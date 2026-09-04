package com.shinefs.core.calendar.model

/** 日界策略（DOCS/YIJING_RULES.md §9.4，V2.0 默认 CIVIL_MIDNIGHT）。 */
enum class DayBoundaryPolicy {
    /** 民用午夜 00:00 换日（V2.0 默认）。 */
    CIVIL_MIDNIGHT,

    /** 晚子时 23:00 即换次日（高级策略；切换后须重新演算并记录规则版本）。 */
    ZI_HOUR_START_23,
}

/** 闰月政策：默认 SAME_MONTH_NUMBER（闰六月取月数 6，显式工程政策）。 */
enum class LeapMonthPolicy {
    SAME_MONTH_NUMBER,
}
