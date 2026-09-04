package com.shinefs.core.interpretation.interpreters

import com.shinefs.core.yijing.element.SeasonalQiContext
import com.shinefs.core.yijing.model.Element
import com.shinefs.core.yijing.model.ElementRelation
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.model.TrigramElements

/**
 * 体用/五行关系释义模板（`element-relation-v1` + `tiyong-v1`，B 级）。
 *
 * 措辞原则：描述性影响倾向，不下绝对吉凶断语（DOCS/YIJING_RULES.md §8.3）。
 */
object ElementInterpreter {

    fun relationTemplate(relation: ElementRelation, ti: Element, yong: Element): String = when (relation) {
        ElementRelation.SAME ->
            "体用同为${ti.chinese}行，主客同气比和，事情多平顺稳定，容易形成合力与共识。"
        ElementRelation.TI_GENERATES_YONG ->
            "体卦${ti.chinese}生用卦${yong.chinese}，主体向外付出、有所耗泄，宜节制投入、量力而行。"
        ElementRelation.YONG_GENERATES_TI ->
            "用卦${yong.chinese}生体卦${ti.chinese}，外部条件滋养主体，多得助力与资源，事情较易推进。"
        ElementRelation.TI_CONTROLS_YONG ->
            "体卦${ti.chinese}克用卦${yong.chinese}，主体掌握主动、可主导局面，也须防用力过度。"
        ElementRelation.YONG_CONTROLS_TI ->
            "用卦${yong.chinese}克体卦${ti.chinese}，外部条件对主体形成较明显压力，宜守不宜攻，审慎应对。"
    }
}

/** 时令释义（`seasonal-qi-v1` 事实层；旺相休囚死细目为 TD-V2-05，不做）。 */
object SeasonalInterpreter {

    fun interpret(qi: SeasonalQiContext, tiElement: Element): String = buildString {
        append("时值${qi.season.chinese}季")
        if (qi.earthMonth) append("（四季土旺月）")
        append("，当令五行为${qi.dominantElement.chinese}。")
        if (tiElement == qi.dominantElement) {
            append("体卦${tiElement.chinese}行得当令之气，气势较足。")
        } else {
            append("体卦${tiElement.chinese}行非当令之气，力量受时令节制。")
        }
        append("（这里只说明当令五行，未展开更细的旺衰判断。）")
    }
}

/**
 * 动爻爻位释义：引《系辞下》明文（A 级，S-A05）。
 * "二多誉，四多惧""三多凶，五多功""初辞拟之，卒成之终"。
 */
object LinePositionInterpreter {

    fun describe(changingLine: Int): String = when (changingLine) {
        1 -> "初爻：事之初始、根基未固。《系辞》云「初辞拟之」，动在初爻多主事情发端、苗初现。"
        2 -> "二爻：居下卦之中。《系辞》云「二多誉」，多得主内之誉、居中得势。"
        3 -> "三爻：下卦之极、内外之际。《系辞》云「三多凶」，处多凶之地，宜谨慎过渡。"
        4 -> "四爻：近君之位。《系辞》云「四多惧」，进退多疑惧，宜审时度势。"
        5 -> "五爻：尊位、上卦之中。《系辞》云「五多功」，多主功业成就、得位得势。"
        else -> "上爻：卦之终。《系辞》云「卒成之终」，动在上爻多主事情末端、穷极将变。"
    }
}

/** 空间方应释义（`spatial-response-v1`，B 级事实层；空间不修改时间卦）。 */
object SpatialResponseInterpreter {

    fun interpret(
        facingMountain: String?,
        sittingMountain: String?,
        directionTrigram: Trigram?,
        relationToTi: ElementRelation?,
        tiElement: Element,
    ): String = buildString {
        if (facingMountain != null) {
            append("测时朝向${facingMountain}山")
            if (sittingMountain != null) append("，坐${sittingMountain}山")
            append("。")
        }
        if (directionTrigram != null && relationToTi != null) {
            val dirElement = TrigramElements.of(directionTrigram).chinese
            append("方位属${directionTrigram.chineseName}卦（${dirElement}），与体卦${tiElement.chinese}：")
            append(
                when (relationToTi) {
                    ElementRelation.SAME -> "比和同气，方位与主体相安。"
                    ElementRelation.TI_GENERATES_YONG -> "体生方位，主体向该方位有所付出。"
                    ElementRelation.YONG_GENERATES_TI -> "方位生体，该方位滋养主体。"
                    ElementRelation.TI_CONTROLS_YONG -> "体克方位，主体能驾驭该方位之事。"
                    ElementRelation.YONG_CONTROLS_TI -> "方位克体，该方位对主体形成一定压力。"
                },
            )
        }
        if (isEmpty()) append("本次起卦未锁定空间数据。")
    }
}
