package com.shinefs.core.calendar.model

/**
 * 十二时辰（`shichen-v1`，E 级，DOCS/YIJING_RULES.md §9.1）。
 *
 * 民用小时 → 时辰：整点左闭右开配对（23:00 起为子时，跨午夜）；
 * [number] 即起卦用"时辰数"（子1…亥12）。禁止用 hourOfDay 直接入式。
 */
enum class Shichen(val branch: EarthlyBranch, val number: Int, val display: String) {
    ZI(EarthlyBranch.ZI, 1, "子时"),
    CHOU(EarthlyBranch.CHOU, 2, "丑时"),
    YIN(EarthlyBranch.YIN, 3, "寅时"),
    MAO(EarthlyBranch.MAO, 4, "卯时"),
    CHEN(EarthlyBranch.CHEN, 5, "辰时"),
    SI(EarthlyBranch.SI, 6, "巳时"),
    WU(EarthlyBranch.WU, 7, "午时"),
    WEI(EarthlyBranch.WEI, 8, "未时"),
    SHEN(EarthlyBranch.SHEN, 9, "申时"),
    YOU(EarthlyBranch.YOU, 10, "酉时"),
    XU(EarthlyBranch.XU, 11, "戌时"),
    HAI(EarthlyBranch.HAI, 12, "亥时");

    companion object {
        /** 民用小时（0..23）→ 时辰。23 与 0 均属子时。 */
        fun ofHour(hourOfDay: Int): Shichen {
            require(hourOfDay in 0..23) { "hourOfDay must be in 0..23, got $hourOfDay" }
            val zodiacIndex = ((hourOfDay + 1) / 2) % 12
            return entries.first { it.branch.zodiacIndex == zodiacIndex }
        }
    }
}
