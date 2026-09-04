package com.shinefs.core.calendar

import com.shinefs.core.calendar.calc.SolarTermCalculator
import com.shinefs.core.calendar.model.CivilDateTime
import com.shinefs.core.calendar.model.SolarTerm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class SolarTermCalculatorTest {

    private val beijing = TimeZone.getTimeZone("Asia/Shanghai")

    private fun millis(y: Int, m: Int, d: Int, h: Int, min: Int = 0): Long =
        CivilTime.toEpochMillis(CivilDateTime(y, m, d, h, min), beijing)

    /** 已知节气日期锚点（北京时间，日级）。 */
    @Test
    fun `已知节气日期锚点`() {
        val cases = listOf(
            Triple(SolarTerm.LI_CHUN, millis(2024, 2, 4, 12), "2024-02-04"),
            Triple(SolarTerm.CHUN_FEN, millis(2024, 3, 19, 12), "2024-03-20"),
            Triple(SolarTerm.XIA_ZHI, millis(2024, 6, 20, 12), "2024-06-21"),
            Triple(SolarTerm.QIU_FEN, millis(2024, 9, 21, 12), "2024-09-22"),
            Triple(SolarTerm.DONG_ZHI, millis(2024, 12, 20, 12), "2024-12-21"),
            Triple(SolarTerm.LI_CHUN, millis(2025, 2, 2, 12), "2025-02-03"),
            Triple(SolarTerm.DONG_ZHI, millis(2023, 12, 21, 12), "2023-12-22"),
            Triple(SolarTerm.LI_CHUN, millis(2026, 2, 3, 12), "2026-02-04"),
            Triple(SolarTerm.DONG_ZHI, millis(2000, 12, 20, 12), "2000-12-21"),
            Triple(SolarTerm.LI_CHUN, millis(2000, 2, 3, 12), "2000-02-04"),
            Triple(SolarTerm.LI_DONG, millis(2024, 11, 6, 12), "2024-11-07"),
        )
        for ((term, near, expectDate) in cases) {
            val start = SolarTermCalculator.termStartEpochMillis(term, near)
            val civil = CivilTime.toCivilDateTime(start, beijing)
            val actual = "%04d-%02d-%02d".format(civil.year, civil.month, civil.day)
            assertEquals("${term.chinese} $expectDate", expectDate, actual)
        }
    }

    @Test
    fun `termAt 依时刻判定节气`() {
        // 2024 立春 = 2024-02-04 16:26 前后：2月3日仍大寒，2月5日已立春
        assertEquals(SolarTerm.DA_HAN, SolarTermCalculator.termAt(millis(2024, 2, 3, 10)))
        assertEquals(SolarTerm.LI_CHUN, SolarTermCalculator.termAt(millis(2024, 2, 5, 10)))
        assertEquals(SolarTerm.LI_QIU, SolarTermCalculator.termAt(millis(2024, 8, 15, 10)))
        assertEquals(SolarTerm.CHU_SHU, SolarTermCalculator.termAt(millis(2026, 9, 4, 10))) // 白露(9/7)前仍处暑
    }

    @Test
    fun `节气时刻年际单调且间隔合理`() {
        var prev = SolarTermCalculator.termStartEpochMillis(SolarTerm.LI_CHUN, millis(2020, 2, 3, 12))
        for (year in 2021..2050) {
            val cur = SolarTermCalculator.termStartEpochMillis(SolarTerm.LI_CHUN, millis(year, 2, 3, 12))
            val deltaDays = (cur - prev) / 86400000.0
            assertTrue("立春间隔异常 $year: $deltaDays", deltaDays in 364.0..366.5)
            prev = cur
        }
    }

    @Test
    fun `termInfoAt 与 termAt 一致`() {
        val at = millis(2024, 8, 15, 10)
        val info = SolarTermCalculator.termInfoAt(at)
        assertEquals(SolarTermCalculator.termAt(at), info.term)
        assertTrue("节气开始早于当前时刻", info.startEpochMillis <= at)
    }
}
