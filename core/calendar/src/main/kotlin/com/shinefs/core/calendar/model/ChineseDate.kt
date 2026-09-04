package com.shinefs.core.calendar.model

/** 民用公历时刻（API 24 安全：不依赖 java.time；时区换算见 [com.shinefs.core.calendar.CivilTime]）。 */
data class CivilDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
) {
    /** API 24 安全的本地日期时间表示，作为历史留痕字段而不是演算输入。 */
    val isoLocal: String
        get() = buildString {
            append(year.toString().padStart(4, '0')).append('-')
            append(month.toString().padStart(2, '0')).append('-')
            append(day.toString().padStart(2, '0')).append('T')
            append(hour.toString().padStart(2, '0')).append(':')
            append(minute.toString().padStart(2, '0')).append(':')
            append(second.toString().padStart(2, '0'))
        }
}

/** 农历日期（`calendar-table-v1`，E 级）。 */
data class ChineseDate(
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val leapMonth: Boolean,
) {
    init {
        require(lunarMonth in 1..12) { "lunarMonth must be in 1..12, got $lunarMonth" }
        require(lunarDay in 1..30) { "lunarDay must be in 1..30, got $lunarDay" }
    }

    /** 年干支（随农历年正月初一轮转）。 */
    val yearGanzhi: Ganzhi get() = Ganzhi(Ganzhi.yearCycleIndex(lunarYear))

    /** 梅花起卦输入：年支数（子1…亥12）。 */
    val yearBranchNumber: Int get() = yearGanzhi.branch.order

    /**
     * 梅花起卦输入：农历月数。闰月政策 `SAME_MONTH_NUMBER`（显式工程政策，
     * 非梅花原文，DOCS/YIJING_RULES.md §9.4）。
     */
    val lunarMonthNumber: Int get() = lunarMonth

    val display: String
        get() = buildString {
            append(yearGanzhi.name).append("年")
            if (leapMonth) append("闰")
            append(CHINESE_MONTHS[lunarMonth - 1]).append("月")
            append(CHINESE_DAYS.getOrNull(lunarDay - 1) ?: lunarDay.toString())
        }

    companion object {
        private val CHINESE_MONTHS =
            listOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
        private val CHINESE_DAYS = listOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十",
        )
    }
}
