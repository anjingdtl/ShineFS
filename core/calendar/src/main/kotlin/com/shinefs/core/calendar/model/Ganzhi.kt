package com.shinefs.core.calendar.model

/**
 * 天干地支与六十甲子（`ganzhi-day-v1` / `ganzhi-year-v1`，E 级，见 DOCS/YIJING_RULES.md §9.3）。
 *
 * [EarthlyBranch.order] 即梅花年月日时起卦的"年支数"（子1…亥12），
 * 与十二地支的循环序（子0…亥11）相差 1，勿混用。
 */
enum class HeavenlyStem(val chinese: String, val element: String) {
    JIA("甲", "木"),
    YI("乙", "木"),
    BING("丙", "火"),
    DING("丁", "火"),
    WU("戊", "土"),
    JI("己", "土"),
    GENG("庚", "金"),
    XIN("辛", "金"),
    REN("壬", "水"),
    GUI("癸", "水");

    companion object {
        fun atCycleIndex(index: Int): HeavenlyStem = entries[Math.floorMod(index, 10)]
    }
}

enum class EarthlyBranch(val chinese: String, val zodiacIndex: Int, val order: Int) {
    ZI("子", 0, 1),
    CHOU("丑", 1, 2),
    YIN("寅", 2, 3),
    MAO("卯", 3, 4),
    CHEN("辰", 4, 5),
    SI("巳", 5, 6),
    WU("午", 6, 7),
    WEI("未", 7, 8),
    SHEN("申", 8, 9),
    YOU("酉", 9, 10),
    XU("戌", 10, 11),
    HAI("亥", 11, 12);

    companion object {
        fun atZodiacIndex(index: Int): EarthlyBranch = entries[Math.floorMod(index, 12)]
    }
}

/** 六十甲子中的一组干支（index 0=甲子 … 59=癸亥；干支序号奇偶配对恒定）。 */
data class Ganzhi(val cycleIndex: Int) {
    init {
        require(cycleIndex in 0..59) { "cycleIndex must be in 0..59, got $cycleIndex" }
    }

    val stem: HeavenlyStem get() = HeavenlyStem.atCycleIndex(cycleIndex)
    val branch: EarthlyBranch get() = EarthlyBranch.atZodiacIndex(cycleIndex)
    val name: String get() = stem.chinese + branch.chinese

    companion object {
        /** 公历年号 → 年干支循环序（4 CE = 甲子；配合农历年界使用，由调用方保证）。 */
        fun yearCycleIndex(lunarYear: Int): Int = Math.floorMod(lunarYear - 4, 60)
    }
}
