package com.shinefs.core.calendar

import com.shinefs.core.calendar.model.CivilDateTime
import java.util.Calendar
import java.util.TimeZone

/**
 * 民用时刻换算（epochMillis ↔ CivilDateTime / 儒略历日序）。
 *
 * 兼容性决策：minSdk 24 且未启用 core library desugaring，故不用 java.time，
 * 以 [java.util.TimeZone] + [java.util.Calendar] 实现（API 1 起可用，含历史夏令时数据）。
 * epochDay 与 java.time.LocalDate.toEpochDay 同值（1970-01-01 = 0），算法取
 * Howard Hinnant date 算法（公历 March-based civil 算法，floorDiv 贯穿）。
 */
object CivilTime {

    fun toCivilDateTime(epochMillis: Long, timeZone: TimeZone): CivilDateTime {
        val cal = Calendar.getInstance(timeZone)
        cal.timeInMillis = epochMillis
        return CivilDateTime(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH),
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            second = cal.get(Calendar.SECOND),
        )
    }

    fun toEpochMillis(civil: CivilDateTime, timeZone: TimeZone): Long {
        val cal = Calendar.getInstance(timeZone)
        cal.clear()
        cal.set(civil.year, civil.month - 1, civil.day, civil.hour, civil.minute, civil.second)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** 公历日期 → 儒略历日序（1970-01-01 = 0）。 */
    fun civilDateToEpochDay(year: Int, month: Int, day: Int): Long {
        val y = if (month <= 2) year - 1L else year.toLong()
        val era = Math.floorDiv(y, 400L)
        val yoe = y - era * 400L
        val mp = if (month > 2) month - 3 else month + 9
        val doy = (153L * mp + 2) / 5 + day - 1
        val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
        return era * 146097L + doe - 719468L
    }

    /** 儒略历日序 → 公历日期（年, 月, 日）。 */
    fun epochDayToCivilDate(epochDay: Long): Triple<Int, Int, Int> {
        val z = epochDay + 719468L
        val era = Math.floorDiv(z, 146097L)
        val doe = Math.floorMod(z, 146097L)
        val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
        val y = yoe + era * 400L
        val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
        val mp = (5L * doy + 2L) / 153L
        val d = (doy - (153L * mp + 2L) / 5L + 1L).toInt()
        val m = (if (mp < 10L) mp + 3L else mp - 9L).toInt()
        val year = (if (m <= 2) y + 1L else y).toInt()
        return Triple(year, m, d)
    }
}
