package com.shinefs.core.yijing.data

import com.shinefs.core.yijing.model.Hexagram
import com.shinefs.core.yijing.model.Trigram

/**
 * 六十四卦结构表（King Wen 序，通行本卦序）。
 *
 * ⚠️ 数据状态：结构数据（卦序/卦名/上下卦）由 Agent 按通行本《周易》录入，
 * 已用自动化测试验证 8×8 全覆盖与结构一致性；**卦名与卦序仍待人工复核**
 * （见 DOCS/YIJING_RULES.md"数据核定状态"）。卦辞/爻辞原典一律不在此表，
 * 待 D-09 决策底本后于 Cycle 05 以版本化数据引入。
 */
object Hexagrams {

    val all: List<Hexagram> = listOf(
        Hexagram(1, "乾", Trigram.QIAN, Trigram.QIAN),
        Hexagram(2, "坤", Trigram.KUN, Trigram.KUN),
        Hexagram(3, "屯", Trigram.ZHEN, Trigram.KAN),
        Hexagram(4, "蒙", Trigram.KAN, Trigram.GEN),
        Hexagram(5, "需", Trigram.QIAN, Trigram.KAN),
        Hexagram(6, "讼", Trigram.KAN, Trigram.QIAN),
        Hexagram(7, "师", Trigram.KAN, Trigram.KUN),
        Hexagram(8, "比", Trigram.KUN, Trigram.KAN),
        Hexagram(9, "小畜", Trigram.QIAN, Trigram.XUN),
        Hexagram(10, "履", Trigram.DUI, Trigram.QIAN),
        Hexagram(11, "泰", Trigram.QIAN, Trigram.KUN),
        Hexagram(12, "否", Trigram.KUN, Trigram.QIAN),
        Hexagram(13, "同人", Trigram.LI, Trigram.QIAN),
        Hexagram(14, "大有", Trigram.QIAN, Trigram.LI),
        Hexagram(15, "谦", Trigram.GEN, Trigram.KUN),
        Hexagram(16, "豫", Trigram.KUN, Trigram.ZHEN),
        Hexagram(17, "随", Trigram.ZHEN, Trigram.DUI),
        Hexagram(18, "蛊", Trigram.XUN, Trigram.GEN),
        Hexagram(19, "临", Trigram.DUI, Trigram.KUN),
        Hexagram(20, "观", Trigram.KUN, Trigram.XUN),
        Hexagram(21, "噬嗑", Trigram.ZHEN, Trigram.LI),
        Hexagram(22, "贲", Trigram.LI, Trigram.GEN),
        Hexagram(23, "剥", Trigram.KUN, Trigram.GEN),
        Hexagram(24, "复", Trigram.ZHEN, Trigram.KUN),
        Hexagram(25, "无妄", Trigram.ZHEN, Trigram.QIAN),
        Hexagram(26, "大畜", Trigram.QIAN, Trigram.GEN),
        Hexagram(27, "颐", Trigram.ZHEN, Trigram.GEN),
        Hexagram(28, "大过", Trigram.XUN, Trigram.DUI),
        Hexagram(29, "坎", Trigram.KAN, Trigram.KAN),
        Hexagram(30, "离", Trigram.LI, Trigram.LI),
        Hexagram(31, "咸", Trigram.GEN, Trigram.DUI),
        Hexagram(32, "恒", Trigram.XUN, Trigram.ZHEN),
        Hexagram(33, "遁", Trigram.GEN, Trigram.QIAN),
        Hexagram(34, "大壮", Trigram.QIAN, Trigram.ZHEN),
        Hexagram(35, "晋", Trigram.KUN, Trigram.LI),
        Hexagram(36, "明夷", Trigram.LI, Trigram.KUN),
        Hexagram(37, "家人", Trigram.LI, Trigram.XUN),
        Hexagram(38, "睽", Trigram.DUI, Trigram.LI),
        Hexagram(39, "蹇", Trigram.GEN, Trigram.KAN),
        Hexagram(40, "解", Trigram.KAN, Trigram.ZHEN),
        Hexagram(41, "损", Trigram.DUI, Trigram.GEN),
        Hexagram(42, "益", Trigram.ZHEN, Trigram.XUN),
        Hexagram(43, "夬", Trigram.QIAN, Trigram.DUI),
        Hexagram(44, "姤", Trigram.XUN, Trigram.QIAN),
        Hexagram(45, "萃", Trigram.KUN, Trigram.DUI),
        Hexagram(46, "升", Trigram.XUN, Trigram.KUN),
        Hexagram(47, "困", Trigram.KAN, Trigram.DUI),
        Hexagram(48, "井", Trigram.XUN, Trigram.KAN),
        Hexagram(49, "革", Trigram.LI, Trigram.DUI),
        Hexagram(50, "鼎", Trigram.XUN, Trigram.LI),
        Hexagram(51, "震", Trigram.ZHEN, Trigram.ZHEN),
        Hexagram(52, "艮", Trigram.GEN, Trigram.GEN),
        Hexagram(53, "渐", Trigram.GEN, Trigram.XUN),
        Hexagram(54, "归妹", Trigram.DUI, Trigram.ZHEN),
        Hexagram(55, "丰", Trigram.LI, Trigram.ZHEN),
        Hexagram(56, "旅", Trigram.GEN, Trigram.LI),
        Hexagram(57, "巽", Trigram.XUN, Trigram.XUN),
        Hexagram(58, "兑", Trigram.DUI, Trigram.DUI),
        Hexagram(59, "涣", Trigram.KAN, Trigram.XUN),
        Hexagram(60, "节", Trigram.DUI, Trigram.KAN),
        Hexagram(61, "中孚", Trigram.DUI, Trigram.XUN),
        Hexagram(62, "小过", Trigram.GEN, Trigram.ZHEN),
        Hexagram(63, "既济", Trigram.LI, Trigram.KAN),
        Hexagram(64, "未济", Trigram.KAN, Trigram.LI),
    )

    private val byOrder: Map<Int, Hexagram> =
        all.associateBy { it.kingWenOrder }

    private val byTrigramPair: Map<Pair<Trigram, Trigram>, Hexagram> =
        all.associateBy { Pair(it.lowerTrigram, it.upperTrigram) }

    fun byKingWenOrder(order: Int): Hexagram {
        Hexagram.requireValidOrder(order)
        return byOrder.getValue(order)
    }

    fun byTrigrams(lower: Trigram, upper: Trigram): Hexagram? =
        byTrigramPair[Pair(lower, upper)]

    fun requireByTrigrams(lower: Trigram, upper: Trigram): Hexagram =
        requireNotNull(byTrigrams(lower, upper)) {
            "no hexagram for lower=${lower.chineseName}, upper=${upper.chineseName}"
        }
}
