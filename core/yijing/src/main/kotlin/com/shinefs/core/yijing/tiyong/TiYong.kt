package com.shinefs.core.yijing.tiyong

import com.shinefs.core.yijing.model.Hexagram
import com.shinefs.core.yijing.model.Trigram

/** 动爻所在宫位（下卦/上卦）。 */
enum class MovingPart {
    LOWER,
    UPPER,
}

/**
 * 体用（`tiyong-v1`，B 级，DOCS/YIJING_RULES.md §8.1）：
 * 动爻 1–3 → 下卦为用、上卦为体；4–6 → 上卦为用、下卦为体。
 */
data class TiYong(
    val ti: Trigram,
    val yong: Trigram,
    val movingPart: MovingPart,
)

object TiYongOps {

    fun of(original: Hexagram, changingLine: Int): TiYong {
        require(changingLine in 1..6) { "changingLine must be in 1..6, got $changingLine" }
        return if (changingLine <= 3) {
            TiYong(
                ti = original.upperTrigram,
                yong = original.lowerTrigram,
                movingPart = MovingPart.LOWER,
            )
        } else {
            TiYong(
                ti = original.lowerTrigram,
                yong = original.upperTrigram,
                movingPart = MovingPart.UPPER,
            )
        }
    }
}
