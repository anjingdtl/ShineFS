package com.shinefs.core.compass.orientation

/** 设备显示旋转的自然坐标轴映射，不依赖 Android 常量，便于 JVM 验证。 */
data class AxisVector(val axis: Int, val sign: Int) {
    init {
        require(axis in 0..2)
        require(sign == 1 || sign == -1)
    }
}

data class DisplayAxes(val right: AxisVector, val top: AxisVector)

object DisplayRotationMapping {
    /**
     * 返回显示坐标系的右轴/顶轴在自然传感器坐标系中的方向。
     * 约定：自然 +X=右、+Y=手机顶部、+Z=屏幕朝外。
     */
    fun axesFor(rotation: Int): DisplayAxes = when (Math.floorMod(rotation, 4)) {
        0 -> DisplayAxes(AxisVector(0, 1), AxisVector(1, 1))
        1 -> DisplayAxes(AxisVector(1, -1), AxisVector(0, 1))
        2 -> DisplayAxes(AxisVector(0, -1), AxisVector(1, -1))
        else -> DisplayAxes(AxisVector(1, 1), AxisVector(0, -1))
    }

    fun degreesFor(rotation: Int): Int = Math.floorMod(rotation, 4) * 90
}
