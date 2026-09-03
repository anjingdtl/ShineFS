package com.shinefs.core.yijing.rules

import com.shinefs.core.yijing.model.Trigram

/**
 * 后天八卦方位（文王卦位），产品方案 §3.1。
 *
 * 每卦 45° 扇区，半开区间 [center−22.5°, center+22.5°)：
 * 坎[337.5,22.5) 艮[22.5,67.5) 震[67.5,112.5) 巽[112.5,157.5)
 * 离[157.5,202.5) 坤[202.5,247.5) 兑[247.5,292.5) 乾[292.5,337.5)
 *
 * 与二十四山的对应关系（每卦领三山）由测试交叉验证：
 * 坎领壬子癸、艮领丑艮寅、震领甲卯乙、巽领辰巽巳、
 * 离领丙午丁、坤领未坤申、兑领庚酉辛、乾领戌乾亥。
 */
object LaterHeavenBagua {

    private val bySectorFromNorth: List<Trigram> = listOf(
        Trigram.KAN, Trigram.GEN, Trigram.ZHEN, Trigram.XUN,
        Trigram.LI, Trigram.KUN, Trigram.DUI, Trigram.QIAN,
    )

    fun trigramAt(azimuth: Float): Trigram {
        Azimuths.requireValid(azimuth)
        val sector = kotlin.math.floor(((azimuth + 22.5f) % 360f) / 45f).toInt()
        return bySectorFromNorth[sector.coerceIn(0, 7)]
    }
}
