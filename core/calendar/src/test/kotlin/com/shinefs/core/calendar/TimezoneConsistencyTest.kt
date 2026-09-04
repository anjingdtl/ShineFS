package com.shinefs.core.calendar

import com.shinefs.core.calendar.model.CivilDateTime
import com.shinefs.core.calendar.provider.TableChineseCalendarProvider
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 11A：同一 instant 在不同设备时区下只改变本地历法视图，不改变 instant 本身。 */
class TimezoneConsistencyTest {

    private val resolver = YijingTimeResolver(TableChineseCalendarProvider())

    @Test
    fun `支持主要设备时区并保留offset与本地时间`() {
        val zones = listOf("Asia/Shanghai", "America/Los_Angeles", "UTC", "Asia/Tokyo")
        val instant = CivilTime.toEpochMillis(
            CivilDateTime(2026, 9, 5, 0, 30, 15),
            TimeZone.getTimeZone("Asia/Shanghai"),
        )

        val contexts = zones.map { id ->
            resolver.resolve(instant, TimeZone.getTimeZone(id))
        }

        assertEquals(zones, contexts.map { it.zoneId })
        assertTrue(contexts.all { it.epochMillis == instant && it.instant == instant })
        assertEquals(listOf(480, -420, 0, 540), contexts.map { it.utcOffsetMinutes })
        assertEquals("2026-09-05T00:30:15", contexts[0].localDateTime)
        assertEquals("2026-09-04T09:30:15", contexts[1].localDateTime)
        assertEquals("2026-09-04T16:30:15", contexts[2].localDateTime)
        assertEquals("2026-09-05T01:30:15", contexts[3].localDateTime)
        assertTrue(contexts[0].trace.render().contains("设备时区=Asia/Shanghai"))
        assertTrue(contexts[0].trace.render().contains(instant.toString()))
    }

    @Test
    fun `跨本地日边界使用各自设备时区参与历法`() {
        val shanghai = TimeZone.getTimeZone("Asia/Shanghai")
        val instant = CivilTime.toEpochMillis(CivilDateTime(2026, 9, 5, 0, 30), shanghai)
        val utc = resolver.resolve(instant, TimeZone.getTimeZone("UTC"))
        val losAngeles = resolver.resolve(instant, TimeZone.getTimeZone("America/Los_Angeles"))

        assertEquals(4, utc.civil.day)
        assertEquals(4, losAngeles.civil.day)
        assertEquals(16, utc.civil.hour)
        assertEquals(9, losAngeles.civil.hour)
        assertEquals(CivilDateTime(2026, 9, 4, 16, 30), utc.civil)
        assertEquals(CivilDateTime(2026, 9, 4, 9, 30), losAngeles.civil)
    }
}
