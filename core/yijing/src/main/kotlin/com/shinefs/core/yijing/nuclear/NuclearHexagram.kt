package com.shinefs.core.yijing.nuclear

import com.shinefs.core.yijing.model.Hexagram
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.rules.HexagramOps

/** 互卦策略（DOCS/YIJING_RULES.md §7，V2.0 默认 STANDARD_234_345）。 */
enum class NuclearPolicy {
    /** 标准法：下互=原卦第2、3、4爻；上互=第3、4、5爻；乾坤照常有互。 */
    STANDARD_234_345,

    /** 旧说"乾坤无互"（登记未启用；启用须升版本并重算）。 */
    LEGACY_QIAN_KUN_NO_NUCLEAR,
}

/** 互卦（`nuclear-hexagram-v1`，B 级）：由原卦 2,3,4 / 3,4,5 爻重新拆卦。 */
data class NuclearHexagram(
    val lower: Trigram,
    val upper: Trigram,
    val hexagram: Hexagram,
)

object NuclearOps {

    fun compute(original: Hexagram, policy: NuclearPolicy = NuclearPolicy.STANDARD_234_345): NuclearHexagram? {
        if (policy == NuclearPolicy.LEGACY_QIAN_KUN_NO_NUCLEAR &&
            original.lowerTrigram == Trigram.QIAN && original.upperTrigram == Trigram.QIAN
        ) return null
        if (policy == NuclearPolicy.LEGACY_QIAN_KUN_NO_NUCLEAR &&
            original.lowerTrigram == Trigram.KUN && original.upperTrigram == Trigram.KUN
        ) return null

        val lines = original.lines
        val lower = Trigram.fromLines(lines.subList(1, 4)) // 第2、3、4爻
        val upper = Trigram.fromLines(lines.subList(2, 5)) // 第3、4、5爻
        return NuclearHexagram(lower = lower, upper = upper, hexagram = HexagramOps.fromTrigrams(lower, upper))
    }
}
