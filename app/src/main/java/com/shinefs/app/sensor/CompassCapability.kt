package com.shinefs.app.sensor

/**
 * 设备罗盘能力（纯逻辑，JVM 可测；Android 侧由 CompassController 探测传感器后传入）。
 *
 * FULL：可测磁方位（必须有磁力计，且有 Rotation Vector 或 加速度计回退组合）。
 * LIMITED：无磁力计——不得伪造方向，罗盘页仅可用有限模式（提示 + 手动输入方位）。
 */
enum class CompassCapabilityLevel { FULL, LIMITED }

data class CompassCapability(
    val hasRotationVector: Boolean,
    val hasMagneticField: Boolean,
    val hasAccelerometer: Boolean,
) {
    val level: CompassCapabilityLevel
        get() = if (hasMagneticField && (hasRotationVector || hasAccelerometer)) {
            CompassCapabilityLevel.FULL
        } else {
            CompassCapabilityLevel.LIMITED
        }

    companion object {
        /** 无磁力计设备：加速度计无法测向。 */
        fun of(hasRotationVector: Boolean, hasMagneticField: Boolean, hasAccelerometer: Boolean) =
            CompassCapability(hasRotationVector, hasMagneticField, hasAccelerometer)
    }
}
