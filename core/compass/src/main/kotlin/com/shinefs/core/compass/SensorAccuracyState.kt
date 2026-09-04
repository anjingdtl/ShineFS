package com.shinefs.core.compass

/**
 * 北参考（V2.0 方案 §6.5）：V2.0 默认磁北；真北/磁偏角补偿为 TD-V2-08（未启用）。
 */
enum class NorthReference {
    MAGNETIC,
    TRUE,
}

/**
 * 传感器精度状态分离（V2.0 方案 §6.6）：
 * 朝向源（Rotation Vector / 回退链）与磁力计的 accuracy 不得互相覆盖。
 */
data class SensorAccuracyState(
    val orientationAccuracy: SensorAccuracy,
    val magneticAccuracy: SensorAccuracy,
)
