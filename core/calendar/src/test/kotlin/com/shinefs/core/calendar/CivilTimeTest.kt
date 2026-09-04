package com.shinefs.core.calendar

import com.shinefs.core.calendar.model.CivilDateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class CivilTimeTest {

    @Test
    fun `epochDay 锚点`() {
        assertEquals(0L, CivilTime.civilDateToEpochDay(1970, 1, 1))
        assertEquals(10957L, CivilTime.civilDateToEpochDay(2000, 1, 1))
        assertEquals(-25567L, CivilTime.civilDateToEpochDay(1900, 1, 1))
        assertEquals(-25537L, CivilTime.civilDateToEpochDay(1900, 1, 31))
    }

    @Test
    fun `epochDay 往返一致（含闰年与世纪边界）`() {
        val samples = listOf(
            Triple(1900, 2, 28), Triple(1900, 3, 1), // 1900 非闰年
            Triple(2000, 2, 28), Triple(2000, 3, 1), // 2000 闰年
            Triple(2100, 2, 28), Triple(2100, 3, 1), // 2100 非闰年
            Triple(1970, 12, 31), Triple(2024, 2, 29), Triple(2026, 9, 4),
            Triple(2033, 12, 22), Triple(2050, 6, 15),
        )
        for ((y, m, d) in samples) {
            val epochDay = CivilTime.civilDateToEpochDay(y, m, d)
            assertEquals(Triple(y, m, d), CivilTime.epochDayToCivilDate(epochDay))
        }
    }

    @Test
    fun `epochMillis 与民用时刻往返（东八区）`() {
        val tz = TimeZone.getTimeZone("Asia/Shanghai")
        val civil = CivilDateTime(2024, 2, 10, 16, 26, 30)
        val millis = CivilTime.toEpochMillis(civil, tz)
        assertEquals(civil, CivilTime.toCivilDateTime(millis, tz))
    }

    @Test
    fun `东八区固定偏移换算`() {
        val tz = TimeZone.getTimeZone("Asia/Shanghai")
        // UTC 2024-02-10T00:00Z → 东八区 08:00
        val utcMidnight = CivilTime.toEpochMillis(CivilDateTime(2024, 2, 10, 0, 0), TimeZone.getTimeZone("UTC"))
        val civil = CivilTime.toCivilDateTime(utcMidnight, tz)
        assertEquals(8, civil.hour)
        assertEquals(10, civil.day)
    }

    @Test
    fun `1988 年中国夏令时偏移 +9 生效`() {
        val tz = TimeZone.getTimeZone("Asia/Shanghai")
        val utc = CivilTime.toEpochMillis(CivilDateTime(1988, 6, 15, 4, 0), TimeZone.getTimeZone("UTC"))
        val civil = CivilTime.toCivilDateTime(utc, tz)
        assertEquals(13, civil.hour) // DST 期间 UTC+9
    }
}
