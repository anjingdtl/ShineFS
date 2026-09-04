package com.shinefs.app

import com.shinefs.core.calendar.table.TableLunarCalendar
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 历表设备端冒烟（calendar-table-v1）。
 *
 * 背景（2026-09-04 实测记录，详见 DOCS/PDCA_LOG.md Cycle 10J）：
 * 当前项目既有 ICU 对照实现曾出现异常——如 1900-12-31 与 2000-12-31 在本机模拟器
 * （API 37.1）出现「农历十一月三十一日」等非法农历日；因此暂不将 ICU 作为正式
 * Oracle，后续需单独核查初始化及字段读取方式。交叉核验改用 lunar_python（构建期，
 * 1900–2100 采样 603/603 与内置历表一致，见 DOCS/SOURCE_CATALOG.md S-E01 注记）。
 * 本测试负责设备端冒烟：锚点日期与内置历表结构一致性。
 */
class CalendarDeviceSmokeTest {

    private fun check(y: Int, m: Int, d: Int, ly: Int, lm: Int, ld: Int, leap: Boolean = false) {
        val lunar = TableLunarCalendar.solarToLunar(y, m, d)
        assertEquals("year $y-$m-$d", ly, lunar.lunarYear)
        assertEquals("month $y-$m-$d", lm, lunar.lunarMonth)
        assertEquals("day $y-$m-$d", ld, lunar.lunarDay)
        assertEquals("leap $y-$m-$d", leap, lunar.leapMonth)
    }

    @Test
    fun modernAnchorDates() {
        check(2000, 2, 5, 2000, 1, 1)      // 庚辰年正月初一
        check(2024, 2, 10, 2024, 1, 1)     // 甲辰年正月初一
        check(2025, 1, 29, 2025, 1, 1)     // 乙巳年正月初一
        check(2025, 7, 25, 2025, 6, 1, true) // 闰六月初一
        check(2023, 3, 22, 2023, 2, 1, true) // 闰二月初一
    }

    @Test
    fun historicalAnchorDates() {
        check(1900, 1, 31, 1900, 1, 1)     // 庚子年正月初一（锚点）
        check(1900, 2, 1, 1900, 1, 2)      // 正月初二
        check(1984, 2, 2, 1984, 1, 1)      // 甲子年正月初一
        check(2033, 12, 22, 2033, 11, 1, true) // 2033 闰冬月初一
        check(2100, 2, 9, 2100, 1, 1)      // 庚申年正月初一（历表末段）
    }

    @Test
    fun fullRoundTripSmoke() {
        // 设备端往返抽样（JVM 已有 7.3 万日全量，此处设备端抽查）
        for (t in listOf(
            Triple(1955, 7, 9), Triple(1999, 12, 31), Triple(2010, 4, 5),
            Triple(2037, 11, 30), Triple(2099, 6, 1),
        )) {
            val lunar = TableLunarCalendar.solarToLunar(t.first, t.second, t.third)
            val back = TableLunarCalendar.lunarToSolar(lunar.lunarYear, lunar.lunarMonth, lunar.lunarDay, lunar.leapMonth)
            assertEquals(t, back)
        }
    }
}
