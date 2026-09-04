package com.shinefs.core.compass

/**
 * 罗盘引擎的完整输出状态（纯数据，无 Android 依赖）。
 *
 * 分层约定：本状态描述"传感器 + 数学滤波"层；
 * 风水坐向解释由 `core:yijing` 基于 [smoothedAzimuth] 派生，两层不得耦合。
 *
 * 精度分离（V2.0 方案 §6.6-2）：朝向源（Rotation Vector/回退链）与磁力计的
 * accuracy 各自独立字段，互不覆盖；[accuracy] 保留为朝向精度的显示别名。
 */
data class CompassState(
    val samples: Int = 0,
    val rawAzimuth: Float? = null,
    val smoothedAzimuth: Float? = null,
    val stability: StabilityLevel = StabilityLevel.UNSTABLE,
    val stabilityStdDeg: Float = Float.NaN,
    val orientationAccuracy: SensorAccuracy = SensorAccuracy.NO_CONTACT,
    val magneticAccuracy: SensorAccuracy = SensorAccuracy.NO_CONTACT,
    val magneticInterference: Boolean = false,
    val magneticMagnitudeUt: Float? = null,
    val tooTilted: Boolean = false,
    val pitchDeg: Float? = null,
    val rollDeg: Float? = null,
    val glitchSuppressed: Int = 0,
) {
    /** 朝向源精度（显示用别名）。 */
    val accuracy: SensorAccuracy get() = orientationAccuracy

    val accuracyState: SensorAccuracyState
        get() = SensorAccuracyState(
            orientationAccuracy = orientationAccuracy,
            magneticAccuracy = magneticAccuracy,
        )

    val hasFix: Boolean get() = smoothedAzimuth != null
}
