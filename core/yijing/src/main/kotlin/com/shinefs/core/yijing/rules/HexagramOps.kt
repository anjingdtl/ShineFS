package com.shinefs.core.yijing.rules

import com.shinefs.core.yijing.data.Hexagrams
import com.shinefs.core.yijing.model.Hexagram
import com.shinefs.core.yijing.model.Trigram

/**
 * 卦象演算：本卦查询、动爻翻转、变卦推导。
 *
 * 动爻变化规则（产品方案 §4.3）：阴阳互翻，只变指定动爻，
 * 变卦由变化后的六爻重新按下三爻/上三爻拆卦映射。
 * 动爻编号 1..6，1=初爻（最下），6=上爻（最上）。
 */
object HexagramOps {

    fun fromTrigrams(lower: Trigram, upper: Trigram): Hexagram =
        Hexagrams.requireByTrigrams(lower, upper)

    /** 对 [hexagram] 的第 [changingLine] 爻（1=初爻…6=上爻）取变卦。 */
    fun withChangingLine(hexagram: Hexagram, changingLine: Int): Hexagram {
        require(changingLine in 1..6) { "changingLine must be in 1..6, got $changingLine" }
        val flipped = hexagram.lines.toMutableList()
        val index = changingLine - 1
        flipped[index] = 1 - flipped[index]
        val lower = Trigram.fromLines(flipped.subList(0, 3))
        val upper = Trigram.fromLines(flipped.subList(3, 6))
        return fromTrigrams(lower, upper)
    }
}
