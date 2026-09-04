package com.shinefs.core.calendar.provider

import com.shinefs.core.calendar.model.ChineseDate
import com.shinefs.core.calendar.model.CivilDateTime

/**
 * 农历服务接口（V2.0 方案 §5.2）。
 *
 * 业务层只依赖本接口，不直接触碰 android.icu（方案约束）；JVM 测试注入 Fake。
 * 生产实现：[com.shinefs.core.calendar.provider.TableChineseCalendarProvider]。
 */
interface ChineseCalendarProvider {
    val version: String

    /** 公历民用时刻 → 农历日期。 */
    fun resolve(civil: CivilDateTime): ChineseDate
}

/** 生产实现：内置版本化历表（1900–2100）。 */
class TableChineseCalendarProvider : ChineseCalendarProvider {
    override val version: String
        get() = com.shinefs.core.calendar.table.LunarTableData.VERSION

    override fun resolve(civil: CivilDateTime): ChineseDate =
        com.shinefs.core.calendar.table.TableLunarCalendar.solarToLunar(
            civil.year, civil.month, civil.day,
        )
}
