package com.shinefs.core.compass

/** 罗盘稳定度（产品方案 §6.3：不稳定/一般/良好；"已定盘"为独立的锁定状态）。 */
enum class StabilityLevel(val label: String) {
    UNSTABLE("不稳定"),
    FAIR("一般"),
    GOOD("良好"),
}

/** 传感器精度（映射 Android SensorManager 精度常量）。 */
enum class SensorAccuracy(val label: String) {
    NO_CONTACT("无读数"),
    UNRELIABLE("不可靠"),
    LOW("低"),
    MEDIUM("中"),
    HIGH("高");

    companion object {
        fun fromAndroidValue(value: Int): SensorAccuracy = when (value) {
            -1 -> NO_CONTACT
            0 -> UNRELIABLE
            1 -> LOW
            2 -> MEDIUM
            3 -> HIGH
            else -> UNRELIABLE
        }
    }
}
