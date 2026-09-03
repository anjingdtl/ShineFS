package com.shinefs.core.yijing.model

/**
 * 六十四卦（别卦）之一：King Wen 序号 + 卦名 + 上下经卦。
 *
 * 卦辞、爻辞等原典文本**有意不在本模型中**：原典必须来自人工核定的
 * 版本化数据（产品方案 §0.5、待决策 D-09），于 Cycle 05 引入，
 * 禁止由 AI 生成或用占位文本冒充。
 *
 * [lines] 六爻自下而上；[symbol] 为 Unicode 易经卦符（U+4DC0 起，按 King Wen 序）。
 */
data class Hexagram(
    val kingWenOrder: Int,
    val chineseName: String,
    val lowerTrigram: Trigram,
    val upperTrigram: Trigram,
) {
    val lines: List<Int> get() = lowerTrigram.lines + upperTrigram.lines
    val symbol: String get() = String(Character.toChars(HEXAGRAM_SYMBOL_BASE + kingWenOrder - 1))

    companion object {
        const val HEXAGRAM_SYMBOL_BASE = 0x4DC0

        fun requireValidOrder(order: Int) {
            require(order in 1..64) { "kingWenOrder must be in 1..64, got $order" }
        }
    }
}
