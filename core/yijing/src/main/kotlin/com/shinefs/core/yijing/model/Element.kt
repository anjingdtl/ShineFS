package com.shinefs.core.yijing.model

/**
 * 五行（`trigram-element-v1` / `element-relation-v1`，B 级，DOCS/YIJING_RULES.md §8.2/§8.3）。
 *
 * 八卦五行：乾兑金、震巽木、坎水、离火、坤艮土。
 * 相生：金→水→木→火→土→金；相克：金→木→土→水→火→金。
 * 枚举声明序即相生循环序（金水木火土），故 generates=+1、controls=+2。
 * 核心只判事实关系，不断绝对吉凶。
 */
enum class Element(val chinese: String) {
    METAL("金"),
    WATER("水"),
    WOOD("木"),
    FIRE("火"),
    EARTH("土");

    /** 我生者（金生水…）。 */
    val generates: Element get() = entries[(ordinal + 1) % 5]

    /** 我克者（金克木…）。 */
    val controls: Element get() = entries[(ordinal + 2) % 5]

    companion object {
        fun fromChinese(chinese: String): Element =
            entries.first { it.chinese == chinese }
    }
}

/** 八卦五行归属（`trigram-element-v1`）。 */
object TrigramElements {
    fun of(trigram: Trigram): Element = Element.fromChinese(trigram.element)
}

/** 体用五行关系（仅事实层，V2.0 方案 §15）。 */
enum class ElementRelation(val display: String) {
    TI_GENERATES_YONG("体生用"),
    YONG_GENERATES_TI("用生体"),
    TI_CONTROLS_YONG("体克用"),
    YONG_CONTROLS_TI("用克体"),
    SAME("比和"),
}

object ElementRelations {

    /** 以 [ti] 为体、[yong] 为用判定五行关系。 */
    fun of(ti: Element, yong: Element): ElementRelation = when {
        ti == yong -> ElementRelation.SAME
        ti.generates == yong -> ElementRelation.TI_GENERATES_YONG
        yong.generates == ti -> ElementRelation.YONG_GENERATES_TI
        ti.controls == yong -> ElementRelation.TI_CONTROLS_YONG
        else -> ElementRelation.YONG_CONTROLS_TI
    }
}
