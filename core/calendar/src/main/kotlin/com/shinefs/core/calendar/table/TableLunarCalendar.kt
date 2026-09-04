package com.shinefs.core.calendar.table

import com.shinefs.core.calendar.CivilTime
import com.shinefs.core.calendar.model.ChineseDate

/**
 * 历表农历换算引擎（`calendar-table-v1`，E 级）。
 *
 * 算法：以 1900-01-31 正月初一为锚，按历表逐年逐月（含闰月在其月号之后）消耗天数。
 * 覆盖公历 1900-01-31 .. 2101-02-27（2100 农历年全程）之间的日期；
 * 区间外 fail-fast 拒绝。
 */
object TableLunarCalendar {

    val SUPPORTED_FIRST_EPOCH_DAY: Long = LunarTableData.ANCHOR_EPOCH_DAY
    val SUPPORTED_LAST_EPOCH_DAY: Long =
        LunarTableData.ANCHOR_EPOCH_DAY + totalDaysOfAllYears() - 1L

    private fun totalDaysOfAllYears(): Long =
        (LunarTableData.FIRST_YEAR..LunarTableData.LAST_YEAR)
            .sumOf { LunarTableData.yearDays(it).toLong() }

    /** 公历日期 → 农历日期。 */
    fun solarToLunar(year: Int, month: Int, day: Int): ChineseDate {
        val epochDay = CivilTime.civilDateToEpochDay(year, month, day)
        require(epochDay in SUPPORTED_FIRST_EPOCH_DAY..SUPPORTED_LAST_EPOCH_DAY) {
            "date $year-$month-$day outside lunar table support range"
        }
        var offset = epochDay - LunarTableData.ANCHOR_EPOCH_DAY

        // 定位农历年
        var lunarYear = LunarTableData.FIRST_YEAR
        while (true) {
            val days = LunarTableData.yearDays(lunarYear).toLong()
            if (offset < days) break
            offset -= days
            lunarYear++
        }

        // 定位农历月（闰月跟在同名普通月之后）
        val leapMonth = LunarTableData.leapMonthOfYear(lunarYear)
        var lunarMonth = 1
        var isLeap = false
        while (true) {
            val days = monthLengthInSequence(lunarYear, lunarMonth, isLeap, leapMonth).toLong()
            if (offset < days) break
            offset -= days
            val next = advanceMonth(lunarMonth, isLeap, leapMonth)
            lunarMonth = next.first
            isLeap = next.second
        }
        return ChineseDate(
            lunarYear = lunarYear,
            lunarMonth = lunarMonth,
            lunarDay = (offset + 1).toInt(),
            leapMonth = isLeap,
        )
    }

    /** 农历日期 → 公历日期（年, 月, 日）。 */
    fun lunarToSolar(lunarYear: Int, lunarMonth: Int, lunarDay: Int, leap: Boolean): Triple<Int, Int, Int> {
        val leapMonth = LunarTableData.leapMonthOfYear(lunarYear)
        if (leap) {
            require(leapMonth == lunarMonth) {
                "year $lunarYear has no leap month $lunarMonth (leap=$leapMonth)"
            }
        }
        var offset = 0L
        for (y in LunarTableData.FIRST_YEAR until lunarYear) {
            offset += LunarTableData.yearDays(y)
        }
        var m = 1
        var isLeap = false
        while (m < lunarMonth || (m == lunarMonth && isLeap != leap)) {
            offset += monthLengthInSequence(lunarYear, m, isLeap, leapMonth)
            val next = advanceMonth(m, isLeap, leapMonth)
            m = next.first
            isLeap = next.second
            if (m == 1 && !isLeap) break // 防御：越出腊月即非法
        }
        val monthLen = monthLengthInSequence(lunarYear, m, isLeap, leapMonth)
        require(lunarDay in 1..monthLen) {
            "lunarDay $lunarDay out of range for month (len=$monthLen)"
        }
        offset += lunarDay - 1
        val epochDay = LunarTableData.ANCHOR_EPOCH_DAY + offset
        return CivilTime.epochDayToCivilDate(epochDay)
    }

    /** 年内月序列中某位的天数：普通月查表；闰月位查闰月标志。 */
    private fun monthLengthInSequence(year: Int, month: Int, isLeap: Boolean, leapMonth: Int): Int =
        if (isLeap) LunarTableData.leapMonthDaysOfYear(year) else LunarTableData.monthDays(year, month)

    /** 年内月序列推进：m 普通月 → （若有闰）闰 m → m+1 … 腊月 → 次年正月（调用方保证不越界）。 */
    private fun advanceMonth(month: Int, isLeap: Boolean, leapMonth: Int): Pair<Int, Boolean> {
        if (!isLeap && leapMonth == month) return month to true
        require(month < 12) { "advance past month 12 without year break" }
        return (month + 1) to false
    }
}
