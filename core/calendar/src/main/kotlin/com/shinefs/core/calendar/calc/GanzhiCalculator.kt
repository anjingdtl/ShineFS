package com.shinefs.core.calendar.calc

import com.shinefs.core.calendar.CivilTime
import com.shinefs.core.calendar.model.Ganzhi

/**
 * 干支计算（`ganzhi-day-v1` / `ganzhi-year-v1`，E 级，DOCS/YIJING_RULES.md §9.3）。
 *
 * 日干支锚点（双锚互验，差 18170 天 = 60 的整数倍 + 50，恰好从甲戌走到甲子）：
 * - 1900-01-01 = 甲戌日（cycleIndex 10）
 * - 1949-10-01 = 甲子日（cycleIndex 0）
 */
object GanzhiCalculator {

    /** 1970-01-01 的日干支循环序：辛巳（17）。由 1900/2000 锚点推得（2000-01-01=戊午 54）。 */
    private const val EPOCH_DAY_1970_CYCLE_INDEX = 17

    /** 公历日期 → 日干支。 */
    fun dayGanzhiOf(year: Int, month: Int, day: Int): Ganzhi {
        val epochDay = CivilTime.civilDateToEpochDay(year, month, day)
        return Ganzhi(Math.floorMod(epochDay.toInt() + EPOCH_DAY_1970_CYCLE_INDEX, 60))
    }

    /** 农历年号 → 年干支（年界=农历正月初一，由调用方保证传入农历年）。 */
    fun yearGanzhiOf(lunarYear: Int): Ganzhi = Ganzhi(Ganzhi.yearCycleIndex(lunarYear))

    /**
     * 节气月建（月支）：立春起寅月，逢"节"换月。传入 [solarTermOrdinal]
     * （[com.shinefs.core.calendar.model.SolarTerm] 的 ordinal，0=立春）。
     * 农历月数与此分字段：月建仅作上下文展示，不入起卦。
     */
    fun monthBranchZodiacIndexAt(solarTermOrdinal: Int): Int {
        require(solarTermOrdinal in 0..23)
        return Math.floorMod(2 + solarTermOrdinal / 2, 12)
    }
}
