package com.shinefs.core.calendar.table

import java.security.MessageDigest

/**
 * 中国公历-农历对照历表 1900–2100（`calendar-table-v1`，E 级，S-E01）。
 *
 * 数据形态：每年一个 32 位整数（通行压缩历表，广泛用于历书软件）：
 * - bit  3..0 ：闰月月号（1–12；0 = 无闰月）
 * - bit 16    ：闰月为大月（30 天）
 * - bit 15..4 ：正月…腊月大小（1 = 30 天大月，自高位 0x8000 起为正月）
 *
 * 锚点：1900-01-31 = 庚子年正月初一。
 * 核验：① 已知春节日期锚点（2000–2029 逐年 + 2033/2034）② 已知闰月年清单
 * ③ 1900–2100 全量内部一致性（逐年天数 353–385、月序单调、往返一致）
 * ④ 设备端锚点冒烟（androidTest，业务层不依赖 ICU）；当前项目既有 ICU 对照实现
 * 曾出现异常，暂不作为正式 Oracle，后续需单独核查初始化及字段读取方式。
 */
object LunarTableData {

    const val FIRST_YEAR = 1900
    const val LAST_YEAR = 2100
    const val VERSION = "calendar-table-v1"

    /** 1900-01-31（正月初一锚点）的儒略历日序。 */
    val ANCHOR_EPOCH_DAY: Long = com.shinefs.core.calendar.CivilTime.civilDateToEpochDay(1900, 1, 31)

    val DATA: IntArray = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, // 1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, // 1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, // 1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, // 1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, // 1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, // 1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, // 1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, // 1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, // 1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, // 1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, // 2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, // 2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, // 2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, // 2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, // 2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, // 2050-2059
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, // 2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, // 2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, // 2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, // 2090-2099
        0x0d520,                                                                                   // 2100
    )

    /** 历表规范串（checksum 基底）：十六进制逗号连接。 */
    val canonicalString: String
        get() = DATA.joinToString(",") { Integer.toHexString(it) }

    /** 历表 SHA-256（版本随检；改动历表必须升版本并重新核定）。 */
    val checksum: String
        get() {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(canonicalString.toByteArray(Charsets.UTF_8))
                .joinToString("") { String.format(java.util.Locale.ROOT, "%02x", it) }
        }

    fun dataOfYear(year: Int): Int {
        require(year in FIRST_YEAR..LAST_YEAR) { "lunar table covers $FIRST_YEAR..$LAST_YEAR, got $year" }
        return DATA[year - FIRST_YEAR]
    }

    /** 年闰月月号（1–12；0 = 无闰）。 */
    fun leapMonthOfYear(year: Int): Int = dataOfYear(year) and 0xF

    /** 年闰月天数（无闰为 0）。 */
    fun leapMonthDaysOfYear(year: Int): Int =
        if (leapMonthOfYear(year) == 0) 0 else if (dataOfYear(year) and 0x10000 != 0) 30 else 29

    /** 普通月（1–12）天数（29 或 30）。 */
    fun monthDays(year: Int, month: Int): Int {
        require(month in 1..12) { "month must be in 1..12, got $month" }
        return if (dataOfYear(year) and (0x10000 shr month) != 0) 30 else 29
    }

    /** 农历年总天数（含闰月）。 */
    fun yearDays(year: Int): Int {
        var sum = 348 // 12 × 29
        val data = dataOfYear(year)
        for (i in 0 until 12) {
            if (data and (0x8000 shr i) != 0) sum++
        }
        return sum + leapMonthDaysOfYear(year)
    }
}
