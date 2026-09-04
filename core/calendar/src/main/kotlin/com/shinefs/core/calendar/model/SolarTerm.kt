package com.shinefs.core.calendar.model

/**
 * 二十四节气（`solar-term-meeus-v1`，E 级，DOCS/YIJING_RULES.md §9.5）。
 *
 * [ordinal] 按太阳视黄经排序：0=立春(315°)、1=雨水(330°)…每 15° 一气。
 * 节气只进时令/旺衰上下文与展示，**不入起卦公式**；与农历月数分字段。
 */
enum class SolarTerm(val chinese: String, val targetLongitude: Int) {
    LI_CHUN("立春", 315),
    YU_SHUI("雨水", 330),
    JING_ZHE("惊蛰", 345),
    CHUN_FEN("春分", 0),
    QING_MING("清明", 15),
    GU_YU("谷雨", 30),
    LI_XIA("立夏", 45),
    XIAO_MAN("小满", 60),
    MANG_ZHONG("芒种", 75),
    XIA_ZHI("夏至", 90),
    XIAO_SHU("小暑", 105),
    DA_SHU("大暑", 120),
    LI_QIU("立秋", 135),
    CHU_SHU("处暑", 150),
    BAI_LU("白露", 165),
    QIU_FEN("秋分", 180),
    HAN_LU("寒露", 195),
    SHUANG_JIANG("霜降", 210),
    LI_DONG("立冬", 225),
    XIAO_XUE("小雪", 240),
    DA_XUE("大雪", 255),
    DONG_ZHI("冬至", 270),
    XIAO_HAN("小寒", 285),
    DA_HAN("大寒", 300);

    companion object {
        /** 视黄经所在节气区间（该瞬时已进入的节气）。 */
        fun ofApparentLongitude(lonDeg: Double): SolarTerm {
            val idx = Math.floorMod(Math.floor((lonDeg - 315.0) / 15.0).toInt(), 24)
            return entries[idx]
        }
    }
}

/** 某时刻所处的节气上下文：节气 + 该节气开始的精确时刻（UTC 毫秒）。 */
data class SolarTermInfo(
    val term: SolarTerm,
    val startEpochMillis: Long,
)
