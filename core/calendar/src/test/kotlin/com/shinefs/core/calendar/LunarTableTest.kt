package com.shinefs.core.calendar

import com.shinefs.core.calendar.table.LunarTableData
import com.shinefs.core.calendar.table.TableLunarCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LunarTableTest {

    @Test
    fun `历表规模与版本`() {
        assertEquals(201, LunarTableData.DATA.size)
        assertEquals("calendar-table-v1", LunarTableData.VERSION)
        assertEquals(64, LunarTableData.checksum.length)
    }

    @Test
    fun `已知闰月年清单（1900-2039 高置信子集）`() {
        val expected = mapOf(
            1900 to 8, 1903 to 5, 1906 to 4, 1909 to 2, 1911 to 6, 1914 to 5, 1917 to 2,
            1919 to 7, 1922 to 5, 1925 to 4, 1928 to 2, 1930 to 6, 1933 to 5, 1936 to 3,
            1938 to 7, 1941 to 6, 1944 to 4, 1947 to 2, 1949 to 7, 1952 to 5, 1955 to 3,
            1957 to 8, 1960 to 6, 1963 to 4, 1966 to 3, 1968 to 7, 1971 to 5, 1974 to 4,
            1976 to 8, 1979 to 6, 1982 to 4, 1984 to 10, 1987 to 6, 1990 to 5, 1993 to 3,
            1995 to 8, 1998 to 5, 2001 to 4, 2004 to 2, 2006 to 7, 2009 to 5, 2012 to 4,
            2014 to 9, 2017 to 6, 2020 to 4, 2023 to 2, 2025 to 6, 2028 to 5, 2031 to 3,
            2033 to 11, 2036 to 6, 2039 to 5,
        )
        for ((year, leap) in expected) {
            assertEquals("闰月月号 $year", leap, LunarTableData.leapMonthOfYear(year))
        }
    }

    @Test
    fun `逐年天数落在 353-385 且月序求和一致`() {
        for (year in LunarTableData.FIRST_YEAR..LunarTableData.LAST_YEAR) {
            val yearDays = LunarTableData.yearDays(year)
            assertTrue("$year days=$yearDays", yearDays in 353..385)
            // 月序列求和（含闰月位）必须等于年天数
            var sum = 0
            var m = 1
            var isLeap = false
            val leap = LunarTableData.leapMonthOfYear(year)
            while (m <= 12) {
                sum += if (isLeap) LunarTableData.leapMonthDaysOfYear(year) else LunarTableData.monthDays(year, m)
                if (!isLeap && leap == m) {
                    isLeap = true
                } else {
                    m++
                    isLeap = false
                }
            }
            assertEquals("月序求和 $year", yearDays, sum)
        }
    }

    @Test
    fun `春节锚点 2000-2029 逐年`() {
        val cny = mapOf(
            2000 to "2000-02-05", 2001 to "2001-01-24", 2002 to "2002-02-12", 2003 to "2003-02-01",
            2004 to "2004-01-22", 2005 to "2005-02-09", 2006 to "2006-01-29", 2007 to "2007-02-18",
            2008 to "2008-02-07", 2009 to "2009-01-26", 2010 to "2010-02-14", 2011 to "2011-02-03",
            2012 to "2012-01-23", 2013 to "2013-02-10", 2014 to "2014-01-31", 2015 to "2015-02-19",
            2016 to "2016-02-08", 2017 to "2017-01-28", 2018 to "2018-02-16", 2019 to "2019-02-05",
            2020 to "2020-01-25", 2021 to "2021-02-12", 2022 to "2022-02-01", 2023 to "2023-01-22",
            2024 to "2024-02-10", 2025 to "2025-01-29", 2026 to "2026-02-17", 2027 to "2027-02-06",
            2028 to "2028-01-26", 2029 to "2029-02-13",
        )
        for ((year, date) in cny) {
            val (y, m, d) = date.split("-").map { it.toInt() }.let { Triple(it[0], it[1], it[2]) }
            val lunar = TableLunarCalendar.solarToLunar(y, m, d)
            assertEquals("春节 $year 年份", year, lunar.lunarYear)
            assertEquals("春节 $year 月", 1, lunar.lunarMonth)
            assertEquals("春节 $year 日", 1, lunar.lunarDay)
            assertTrue("春节 $year 非闰", !lunar.leapMonth)
        }
    }

    @Test
    fun `2033 年问题 - 闰冬月与次年春节`() {
        // 2033 闰十一月初一 = 2033-12-22；2034 年正月初一 = 2034-02-19
        val leap11 = TableLunarCalendar.solarToLunar(2033, 12, 22)
        assertEquals(2033, leap11.lunarYear)
        assertEquals(11, leap11.lunarMonth)
        assertTrue(leap11.leapMonth)
        assertEquals(1, leap11.lunarDay)
        val cny2034 = TableLunarCalendar.solarToLunar(2034, 2, 19)
        assertEquals(2034, cny2034.lunarYear)
        assertEquals(1, cny2034.lunarMonth)
        assertEquals(1, cny2034.lunarDay)
    }

    @Test
    fun `已知闰月初一锚点`() {
        // 2023 闰二月初一 = 2023-03-22；2025 闰六月初一 = 2025-07-25
        val a = TableLunarCalendar.solarToLunar(2023, 3, 22)
        assertEquals(2, a.lunarMonth); assertTrue(a.leapMonth); assertEquals(1, a.lunarDay)
        val b = TableLunarCalendar.solarToLunar(2025, 7, 25)
        assertEquals(6, b.lunarMonth); assertTrue(b.leapMonth); assertEquals(1, b.lunarDay)
        // 前一日仍是普通月末日
        val before = TableLunarCalendar.solarToLunar(2025, 7, 24)
        assertEquals(6, before.lunarMonth); assertTrue(!before.leapMonth)
    }

    @Test
    fun `1900 锚点与支持区间`() {
        val anchor = TableLunarCalendar.solarToLunar(1900, 1, 31)
        assertEquals(1900, anchor.lunarYear)
        assertEquals(1, anchor.lunarMonth)
        assertEquals(1, anchor.lunarDay)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `区间外日期拒绝 - 早于锚点`() {
        TableLunarCalendar.solarToLunar(1900, 1, 30)
    }

    @Test
    fun `全量往返一致 1900-2100（约 73k 日）`() {
        var epochDay = LunarTableData.ANCHOR_EPOCH_DAY
        val last = TableLunarCalendar.SUPPORTED_LAST_EPOCH_DAY
        var count = 0
        while (epochDay <= last) {
            val (y, m, d) = CivilTime.epochDayToCivilDate(epochDay)
            val lunar = TableLunarCalendar.solarToLunar(y, m, d)
            val back = TableLunarCalendar.lunarToSolar(lunar.lunarYear, lunar.lunarMonth, lunar.lunarDay, lunar.leapMonth)
            assertEquals("roundtrip $y-$m-$d", Triple(y, m, d), back)
            epochDay++
            count++
        }
        assertTrue(count > 70000)
    }
}
