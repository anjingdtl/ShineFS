package com.shinefs.core.calendar

import com.shinefs.core.calendar.model.ChineseDate
import com.shinefs.core.calendar.model.CivilDateTime
import com.shinefs.core.calendar.model.DayBoundaryPolicy
import com.shinefs.core.calendar.model.Shichen
import com.shinefs.core.calendar.provider.ChineseCalendarProvider
import com.shinefs.core.calendar.provider.TableChineseCalendarProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

/** JVM 测试注入的假历法提供者（V2.0 方案 §5.2：JVM 测试允许 FakeProvider）。 */
class FakeChineseCalendarProvider(private val fixed: ChineseDate) : ChineseCalendarProvider {
    override val version: String = "fake-calendar-test"

    override fun resolve(civil: CivilDateTime): ChineseDate = fixed
}

class YijingTimeResolverTest {

    private val beijing = TimeZone.getTimeZone("Asia/Shanghai")
    private val provider = TableChineseCalendarProvider()

    private fun millis(y: Int, m: Int, d: Int, h: Int, min: Int = 0): Long =
        CivilTime.toEpochMillis(CivilDateTime(y, m, d, h, min), beijing)

    @Test
    fun `春节当天全字段`() {
        val ctx = YijingTimeResolver(provider).resolve(millis(2024, 2, 10, 12), beijing)
        assertEquals(2024, ctx.lunarYear)
        assertEquals(1, ctx.lunarMonth)
        assertEquals(1, ctx.lunarDay)
        assertTrue(!ctx.leapMonth)
        assertEquals("甲辰", ctx.dayGanzhi.name)
        assertEquals("甲", ctx.yearStem.chinese)
        assertEquals("辰", ctx.yearBranch.chinese)
        assertEquals(5, ctx.yearBranchNumber)
        assertEquals(Shichen.WU, ctx.shichen)
        assertEquals(7, ctx.hourBranchNumber)
        assertEquals("calendar-table-v1", ctx.calendarVersion)
    }

    @Test
    fun `观梅占数字链路（FakeProvider 固定农历 辰年十二月十七 申时）`() {
        // 1940 = 庚辰年（支=辰 → 年支数 5）
        val resolver = YijingTimeResolver(FakeChineseCalendarProvider(ChineseDate(1940, 12, 17, false)))
        val ctx = resolver.resolve(millis(2026, 9, 4, 16), beijing) // 16 时 = 申时
        assertEquals(5, ctx.yearBranchNumber)
        assertEquals(12, ctx.lunarMonthNumber)
        assertEquals(17, ctx.lunarDayNumber)
        assertEquals(9, ctx.hourBranchNumber)
        assertEquals("辰", ctx.yearBranch.chinese)
        assertEquals("申", ctx.hourBranch.chinese)
        val rendered = ctx.trace.render()
        assertTrue(rendered.contains("时辰序号=9"))
        assertTrue(rendered.contains("农历"))
    }

    @Test
    fun `晚子时换日策略`() {
        val resolver = YijingTimeResolver(provider)
        // 2024-02-10 23:30：民用日界仍算 2/10（正月初一）；晚子时换日算 2/11（正月初二）
        val midnight = resolver.resolve(millis(2024, 2, 10, 23, 30), beijing, DayBoundaryPolicy.CIVIL_MIDNIGHT)
        assertEquals(10, midnight.effectiveCivil.day)
        assertEquals(1, midnight.lunarDay)
        assertEquals("甲辰", midnight.dayGanzhi.name)
        assertEquals(1, midnight.shichen.number) // 时辰为子

        val zi = resolver.resolve(millis(2024, 2, 10, 23, 30), beijing, DayBoundaryPolicy.ZI_HOUR_START_23)
        assertEquals(11, zi.effectiveCivil.day)
        assertEquals(2, zi.lunarDay)
        assertEquals(1, zi.shichen.number) // 时辰仍为子
    }

    @Test
    fun `22点59分仍亥时 23点整起子时`() {
        val resolver = YijingTimeResolver(provider)
        assertEquals(12, resolver.resolve(millis(2024, 6, 1, 22, 59), beijing).hourBranchNumber)
        assertEquals(1, resolver.resolve(millis(2024, 6, 1, 23, 0), beijing).hourBranchNumber)
        assertEquals(1, resolver.resolve(millis(2024, 6, 1, 0, 0), beijing).hourBranchNumber)
        assertEquals(2, resolver.resolve(millis(2024, 6, 1, 1, 0), beijing).hourBranchNumber)
    }

    @Test
    fun `同输入同输出（确定性）`() {
        val resolver = YijingTimeResolver(provider)
        val t = millis(2026, 9, 4, 10, 30)
        val a = resolver.resolve(t, beijing)
        val b = resolver.resolve(t, beijing)
        assertEquals(a, b)
        assertEquals(a.trace.render(), b.trace.render())
    }

    @Test
    fun `闰月 SAME_MONTH_NUMBER 政策取同月号`() {
        // 2025-07-25 = 闰六月初一 → lunarMonthNumber = 6
        val ctx = YijingTimeResolver(provider).resolve(millis(2025, 7, 25, 12), beijing)
        assertTrue(ctx.leapMonth)
        assertEquals(6, ctx.lunarMonth)
        assertEquals(6, ctx.lunarMonthNumber)
    }
}
