package com.shinefs.core.yijing.rules

import com.shinefs.core.yijing.model.Trigram

/**
 * 坐向换算结果（纯数学部分，不含传感器状态）。
 *
 * 产品方案 §3.3：手机所朝方向为"向"，相反方向为"坐"：
 * facing = azimuth；sitting = (azimuth + 180) % 360。
 * 传感器精度/稳定度/磁干扰等字段由 Cycle 02 的罗盘引擎叠加。
 */
data class MountainOrientation(
    val azimuth: Float,
    val facingMountain: String,
    val sittingMountain: String,
    val facingTrigram: Trigram,
) {
    val sittingTrigram: Trigram get() = LaterHeavenBagua.trigramAt((azimuth + 180f) % 360f)

    val facingElement: String get() = facingTrigram.element
}

object Orientation {
    fun fromAzimuth(azimuth: Float): MountainOrientation {
        Azimuths.requireValid(azimuth)
        return MountainOrientation(
            azimuth = azimuth,
            facingMountain = Mountains24.mountainAt(azimuth),
            sittingMountain = Mountains24.mountainAt((azimuth + 180f) % 360f),
            facingTrigram = LaterHeavenBagua.trigramAt(azimuth),
        )
    }
}
