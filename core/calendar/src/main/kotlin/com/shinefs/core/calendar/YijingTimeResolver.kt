package com.shinefs.core.calendar

import com.shinefs.core.calendar.calc.GanzhiCalculator
import com.shinefs.core.calendar.calc.SolarTermCalculator
import com.shinefs.core.calendar.model.CalendarTrace
import com.shinefs.core.calendar.model.CalendarTraceEntry
import com.shinefs.core.calendar.model.ChineseDate
import com.shinefs.core.calendar.model.CivilDateTime
import com.shinefs.core.calendar.model.DayBoundaryPolicy
import com.shinefs.core.calendar.model.EarthlyBranch
import com.shinefs.core.calendar.model.Shichen
import com.shinefs.core.calendar.model.YijingTimeContext
import com.shinefs.core.calendar.provider.ChineseCalendarProvider
import java.util.TimeZone

/**
 * 时间上下文装配器（CalendarCore 一级核心入口，V2.0 方案 §5）。
 *
 * 职责：epochMillis + 时区 → 民用时刻 →（日界策略）→ 农历/干支/时辰/节气 → [YijingTimeContext]。
 * 全链路确定性：同输入同政策必得同输出，并产出 [CalendarTrace] 供 UI/测试/复算。
 */
class YijingTimeResolver(
    private val calendarProvider: ChineseCalendarProvider,
) {
    fun resolve(
        epochMillis: Long,
        timeZone: TimeZone,
        dayBoundaryPolicy: DayBoundaryPolicy = DayBoundaryPolicy.CIVIL_MIDNIGHT,
    ): YijingTimeContext {
        val zoneId = timeZone.id
        val civil = CivilTime.toCivilDateTime(epochMillis, timeZone)

        // 日界策略：晚子时 23:00 起即算次日（DOCS/YIJING_RULES.md §9.4）
        val effectiveCivil = if (dayBoundaryPolicy == DayBoundaryPolicy.ZI_HOUR_START_23 && civil.hour == 23) {
            val nextDay = CivilTime.epochDayToCivilDate(CivilTime.civilDateToEpochDay(civil.year, civil.month, civil.day) + 1)
            civil.copy(year = nextDay.first, month = nextDay.second, day = nextDay.third)
        } else {
            civil
        }

        val chinese: ChineseDate = calendarProvider.resolve(effectiveCivil)
        val dayGanzhi = GanzhiCalculator.dayGanzhiOf(effectiveCivil.year, effectiveCivil.month, effectiveCivil.day)
        val shichen = Shichen.ofHour(civil.hour)
        val termInfo = SolarTermCalculator.termInfoAt(epochMillis)
        val monthBranch = EarthlyBranch.atZodiacIndex(
            GanzhiCalculator.monthBranchZodiacIndexAt(termInfo.term.ordinal),
        )

        val trace = CalendarTrace(
            listOf(
                CalendarTraceEntry("公历时刻", "${civil.year}-${civil.month}-${civil.day} ${civil.hour}:${"%02d".format(civil.minute)}", zoneId),
                CalendarTraceEntry(
                    "有效日期",
                    "${effectiveCivil.year}-${effectiveCivil.month}-${effectiveCivil.day}",
                    if (effectiveCivil != civil) "晚子时换日（${dayBoundaryPolicy}）" else "民用午夜日界（${dayBoundaryPolicy}）",
                ),
                CalendarTraceEntry("农历", chinese.display),
                CalendarTraceEntry("年干支", chinese.yearGanzhi.name, "年界=农历正月初一"),
                CalendarTraceEntry("日干支", dayGanzhi.name),
                CalendarTraceEntry("时辰", shichen.display, "时辰数=${shichen.number}"),
                CalendarTraceEntry("节气", termInfo.term.chinese, "月建=${monthBranch.chinese}（节气月，不入起卦）"),
                CalendarTraceEntry("历法版本", calendarProvider.version),
            ),
        )

        return YijingTimeContext(
            epochMillis = epochMillis,
            zoneId = zoneId,
            civil = civil,
            effectiveCivil = effectiveCivil,
            lunarYear = chinese.lunarYear,
            lunarMonth = chinese.lunarMonth,
            lunarDay = chinese.lunarDay,
            leapMonth = chinese.leapMonth,
            yearStem = chinese.yearGanzhi.stem,
            yearBranch = chinese.yearGanzhi.branch,
            monthBranch = monthBranch,
            dayGanzhi = dayGanzhi,
            shichen = shichen,
            hourBranch = shichen.branch,
            yearBranchNumber = chinese.yearBranchNumber,
            lunarMonthNumber = chinese.lunarMonthNumber,
            lunarDayNumber = chinese.lunarDay,
            hourBranchNumber = shichen.number,
            solarTerm = termInfo,
            calendarVersion = calendarProvider.version,
            dayBoundaryPolicy = dayBoundaryPolicy,
            leapMonthPolicy = com.shinefs.core.calendar.model.LeapMonthPolicy.SAME_MONTH_NUMBER,
            trace = trace,
        )
    }
}
