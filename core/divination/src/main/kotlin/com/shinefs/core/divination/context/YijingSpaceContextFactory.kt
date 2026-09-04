package com.shinefs.core.divination.context

import com.shinefs.core.compass.CompassState
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.rules.LaterHeavenBagua
import com.shinefs.core.yijing.rules.Mountains24

/**
 * 时空融合装配（V2.0 方案 §7/§33-10G）：罗盘引擎状态 → 空间上下文。
 *
 * 锚定方向约定：手机顶部所指为"向"（facing），平滑角为输入（`orientation-v1`）。
 * 北参考默认磁北（TD-V2-08）。
 */
object YijingSpaceContextFactory {

    fun fromCompassState(
        state: CompassState,
        northReference: NorthReference = NorthReference.MAGNETIC,
    ): YijingSpaceContext? {
        val azimuth = state.smoothedAzimuth ?: return null
        val sitting = (azimuth + 180f) % 360f
        return YijingSpaceContext(
            rawAzimuth = state.rawAzimuth,
            smoothedAzimuth = azimuth,
            northReference = northReference,
            facingMountain = Mountains24.mountainAt(azimuth),
            sittingMountain = Mountains24.mountainAt(sitting),
            directionTrigram = LaterHeavenBagua.trigramAt(azimuth),
            sensorAccuracy = state.accuracyState,
            stable = state.stability == StabilityLevel.GOOD,
            magneticInterference = state.magneticInterference,
        )
}

    /** 便捷重载：直接取 Trigram（供展示层）。 */
    fun trigramAt(azimuth: Float): Trigram = LaterHeavenBagua.trigramAt(azimuth)
}
