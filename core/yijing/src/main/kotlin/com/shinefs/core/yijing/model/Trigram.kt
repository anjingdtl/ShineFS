package com.shinefs.core.yijing.model

/**
 * 八卦（经卦）。
 *
 * 枚举声明顺序即先天八卦数：乾一、兑二、离三、震四、巽五、坎六、艮七、坤八，
 * [xiantianNumber] = ordinal + 1，供后续数字起卦取卦数使用（见 DOCS/YIJING_RULES.md）。
 *
 * [lines] 为爻序列，自下而上，1=阳爻，0=阴爻。
 * 方位/角度为后天八卦（文王卦位），见产品方案 §3.1。
 */
enum class Trigram(
    val chineseName: String,
    val symbol: String,
    val lines: List<Int>,
    val direction: String,
    val directionAngle: Float,
    val element: String,
    val natureImage: String,
    val familyRole: String,
    val keywords: List<String>,
) {
    QIAN("乾", "☰", listOf(1, 1, 1), "西北", 315f, "金", "天", "父", listOf("刚健", "创始", "自强")),
    DUI("兑", "☱", listOf(1, 1, 0), "西", 270f, "金", "泽", "少女", listOf("喜悦", "言说", "润泽")),
    LI("离", "☲", listOf(1, 0, 1), "南", 180f, "火", "火", "中女", listOf("光明", "附着", "文明")),
    ZHEN("震", "☳", listOf(1, 0, 0), "东", 90f, "木", "雷", "长男", listOf("动", "奋起", "惊蛰")),
    XUN("巽", "☴", listOf(0, 1, 1), "东南", 135f, "木", "风", "长女", listOf("入", "顺伏", "渗透")),
    KAN("坎", "☵", listOf(0, 1, 0), "北", 0f, "水", "水", "中男", listOf("陷", "险", "流通")),
    GEN("艮", "☶", listOf(0, 0, 1), "东北", 45f, "土", "山", "少男", listOf("止", "安静", "笃实")),
    KUN("坤", "☷", listOf(0, 0, 0), "西南", 225f, "土", "地", "母", listOf("厚德", "包容", "承载"));

    val xiantianNumber: Int get() = ordinal + 1

    companion object {
        /** 依自下而上三爻反查卦（如 [1,0,0] → 震）。 */
        fun fromLines(lines: List<Int>): Trigram {
            require(lines.size == 3 && lines.all { it == 0 || it == 1 }) {
                "trigram lines must be 3 entries of 0/1, got $lines"
            }
            return entries.first { it.lines == lines }
        }
    }
}
