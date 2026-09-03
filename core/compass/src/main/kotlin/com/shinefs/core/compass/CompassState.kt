package com.shinefs.core.compass

/**
 * 罗盘引擎的完整输出状态（纯数据，无 Android 依赖）。
 *
 * 分层约定：本状态描述"传感器 + 数学滤波"层；
 * 风水坐向解释由 `core:yijing` 基于 [smoothedAzimuth] 派生，两层不得耦合。
 */
data class CompassState(
    val samples: Int = 0,
    val rawAzimuth: Float? = null,
    val smoothedAzimuth: Float? = null,
    val stability: StabilityLevel = StabilityLevel.UNSTABLE,
    val stabilityStdDeg: Float = Float.NaN,
    val accuracy: SensorAccuracy = SensorAccuracy.NO_CONTACT,
    val magneticInterference: Boolean = false,
    val magneticMagnitudeUt: Float? = null,
    val tooTilted: Boolean = false,
    val pitchDeg: Float? = null,
    val rollDeg: Float? = null,
    val glitchSuppressed: Int = 0,
) {
    val hasFix: Boolean get() = smoothedAzimuth != null
}
