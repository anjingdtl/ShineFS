package com.shinefs.core.yijing.rules

/**
 * 二十四山：360° 等分 24 山，每山 15°。
 *
 * 数组顺序与中心角严格按产品方案 §3.2：
 * index = floor(((azimuth + 7.5) % 360) / 15)，山 i 的中心角 = i × 15°，
 * 山 i 覆盖半开区间 [i×15°−7.5°, i×15°+7.5°)（mod 360）。
 */
object Mountains24 {

    val names: List<String> = listOf(
        "子", "癸", "丑", "艮", "寅", "甲",
        "卯", "乙", "辰", "巽", "巳", "丙",
        "午", "丁", "未", "坤", "申", "庚",
        "酉", "辛", "戌", "乾", "亥", "壬",
    )

    const val SECTOR_DEGREES = 15f

    fun mountainIndexAt(azimuth: Float): Int {
        Azimuths.requireValid(azimuth)
        val index = kotlin.math.floor(((azimuth + 7.5f) % 360f) / SECTOR_DEGREES).toInt()
        return index.coerceIn(0, names.lastIndex)
    }

    fun mountainAt(azimuth: Float): String = names[mountainIndexAt(azimuth)]

    /** 山 i 的中心角：子 0°、癸 15°、……、壬 345°。 */
    fun centerAngleOf(index: Int): Float {
        require(index in names.indices) { "mountain index must be in 0..23, got $index" }
        return index * SECTOR_DEGREES
    }

    /** 山 i 的下边界（含）。 */
    fun lowerBoundOf(index: Int): Float {
        require(index in names.indices)
        return (centerAngleOf(index) - 7.5f + 360f) % 360f
    }

    /** 山 i 的上边界（不含）。 */
    fun upperBoundOf(index: Int): Float {
        require(index in names.indices)
        return (centerAngleOf(index) + 7.5f) % 360f
    }
}
