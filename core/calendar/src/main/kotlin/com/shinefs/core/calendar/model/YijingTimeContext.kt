package com.shinefs.core.calendar.model

/**
 * 演算时间上下文（V2.0 方案 §5，字段对齐并做 API 24 兼容类型替换：
 * Instant→epochMillis、ZoneId→zoneId 字符串、LocalDateTime→[civil]）。
 *
 * 这是全系统唯一的正式时间输入结构：起卦层只消费本结构，不得自行取系统时钟。
 */
data class YijingTimeContext(
    val epochMillis: Long,
    val zoneId: String,
    val civil: CivilDateTime,
    /** 日界策略生效后的"有效民用日期"（ZI_HOUR_START_23 时 23:00–23:59 归次日）。 */
    val effectiveCivil: CivilDateTime,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val leapMonth: Boolean,
    val yearStem: HeavenlyStem,
    val yearBranch: EarthlyBranch,
    /** 节气月建（立春起寅月；与农历月数分字段，仅上下文用）。 */
    val monthBranch: EarthlyBranch?,
    val dayGanzhi: Ganzhi,
    val shichen: Shichen,
    val hourBranch: EarthlyBranch,
    /** 梅花起卦四数：年支数（子1…亥12）。 */
    val yearBranchNumber: Int,
    /** 梅花起卦四数：农历月数（闰月 SAME_MONTH_NUMBER 政策）。 */
    val lunarMonthNumber: Int,
    /** 梅花起卦四数：农历日数。 */
    val lunarDayNumber: Int,
    /** 梅花起卦四数：时辰数（子1…亥12）。 */
    val hourBranchNumber: Int,
    val solarTerm: SolarTermInfo?,
    val calendarVersion: String,
    val dayBoundaryPolicy: DayBoundaryPolicy,
    val leapMonthPolicy: LeapMonthPolicy,
    val trace: CalendarTrace,
) {
    val lunarDisplay: String
        get() = ChineseDate(lunarYear, lunarMonth, lunarDay, leapMonth).display

    val timeDisplay: String
        get() = buildString {
            append(dayGanzhi.name).append("日 ")
            append(shichen.display)
        }
}
