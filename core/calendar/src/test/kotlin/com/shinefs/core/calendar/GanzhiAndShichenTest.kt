package com.shinefs.core.calendar

import com.shinefs.core.calendar.calc.GanzhiCalculator
import com.shinefs.core.calendar.model.Ganzhi
import com.shinefs.core.calendar.model.Shichen
import org.junit.Assert.assertEquals
import org.junit.Test

class GanzhiAndShichenTest {

    @Test
    fun `六十甲子首尾与配对`() {
        assertEquals("甲子", Ganzhi(0).name)
        assertEquals("癸亥", Ganzhi(59).name)
        assertEquals("甲戌", Ganzhi(10).name)
        assertEquals("甲寅", Ganzhi(50).name)
    }

    @Test
    fun `年干支轮转锚点`() {
        assertEquals("甲子", Ganzhi(Ganzhi.yearCycleIndex(1984)).name)
        assertEquals("甲辰", Ganzhi(Ganzhi.yearCycleIndex(2024)).name)
        assertEquals("乙巳", Ganzhi(Ganzhi.yearCycleIndex(2025)).name)
        assertEquals("癸卯", Ganzhi(Ganzhi.yearCycleIndex(2023)).name)
        assertEquals("庚子", Ganzhi(Ganzhi.yearCycleIndex(1900)).name)
    }

    @Test
    fun `日干支双锚互验`() {
        // 1900-01-01 = 甲戌日；1949-10-01 = 甲子日；2000-01-01 = 戊午日
        assertEquals("甲戌", GanzhiCalculator.dayGanzhiOf(1900, 1, 1).name)
        assertEquals("甲子", GanzhiCalculator.dayGanzhiOf(1949, 10, 1).name)
        assertEquals("戊午", GanzhiCalculator.dayGanzhiOf(2000, 1, 1).name)
        // 1970-01-01 = 辛巳日（epochDay 0）
        assertEquals("辛巳", GanzhiCalculator.dayGanzhiOf(1970, 1, 1).name)
    }

    @Test
    fun `月建随节气推进`() {
        // 立春起寅月；逢节换月（惊蛰仍进一位在 floor(ordinal/2) 中体现）
        assertEquals(2, GanzhiCalculator.monthBranchZodiacIndexAt(SolarTermOrdinal.LI_CHUN))
        assertEquals(2, GanzhiCalculator.monthBranchZodiacIndexAt(SolarTermOrdinal.YU_SHUI))
        assertEquals(3, GanzhiCalculator.monthBranchZodiacIndexAt(SolarTermOrdinal.JING_ZHE))
        assertEquals(3, GanzhiCalculator.monthBranchZodiacIndexAt(SolarTermOrdinal.CHUN_FEN))
        assertEquals(0, GanzhiCalculator.monthBranchZodiacIndexAt(SolarTermOrdinal.DA_XUE)) // 大雪起子月
        assertEquals(1, GanzhiCalculator.monthBranchZodiacIndexAt(SolarTermOrdinal.XIAO_HAN)) // 小寒起丑月
    }

    @Test
    fun `十二时辰全小时映射与边界`() {
        assertEquals(Shichen.ZI, Shichen.ofHour(23))
        assertEquals(Shichen.ZI, Shichen.ofHour(0))
        assertEquals(Shichen.CHOU, Shichen.ofHour(1))
        assertEquals(Shichen.CHOU, Shichen.ofHour(2))
        assertEquals(Shichen.YIN, Shichen.ofHour(3))
        assertEquals(Shichen.MAO, Shichen.ofHour(5))
        assertEquals(Shichen.CHEN, Shichen.ofHour(7))
        assertEquals(Shichen.SI, Shichen.ofHour(9))
        assertEquals(Shichen.WU, Shichen.ofHour(11))
        assertEquals(Shichen.WEI, Shichen.ofHour(13))
        assertEquals(Shichen.SHEN, Shichen.ofHour(15))
        assertEquals(Shichen.YOU, Shichen.ofHour(17))
        assertEquals(Shichen.XU, Shichen.ofHour(19))
        assertEquals(Shichen.HAI, Shichen.ofHour(21))
        assertEquals(Shichen.HAI, Shichen.ofHour(22))
        // 时辰数（起卦用）
        assertEquals(1, Shichen.ZI.number)
        assertEquals(4, Shichen.MAO.number)
        assertEquals(9, Shichen.SHEN.number)
        assertEquals(12, Shichen.HAI.number)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `非法小时拒绝`() {
        Shichen.ofHour(24)
    }

    /** 测试辅助：节气 ordinal 常量（与 SolarTerm 枚举序一致）。 */
    private object SolarTermOrdinal {
        const val LI_CHUN = 0
        const val YU_SHUI = 1
        const val JING_ZHE = 2
        const val CHUN_FEN = 3
        const val DA_XUE = 20
        const val XIAO_HAN = 22
    }
}
