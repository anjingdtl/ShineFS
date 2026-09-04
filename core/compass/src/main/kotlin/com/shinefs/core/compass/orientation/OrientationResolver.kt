package com.shinefs.core.compass.orientation

import com.shinefs.core.compass.CircularMath
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

/** 旋转矩阵经显示旋转补偿后的方位与姿态测量结果。 */
data class OrientationSample(
    val azimuthDeg: Float,
    val pitchDeg: Float,
    val rollDeg: Float,
    val screenNormalVerticalComponent: Float,
    val displayRotation: Int,
)

/** 平放与竖持 resolver 的统一契约。 */
interface OrientationResolver {
    fun resolve(rotationMatrix: FloatArray, displayRotation: Int): OrientationSample?
}

/**
 * 平放解析器：屏幕法向量应接近世界竖直轴，手机顶部的水平投影作为“向”。
 * 方位不使用屏幕法向量，避免把屏幕朝向误当成手机顶部方向。
 */
class FlatOrientationResolver : OrientationResolver {
    override fun resolve(rotationMatrix: FloatArray, displayRotation: Int): OrientationSample? =
        OrientationMath.resolve(rotationMatrix, displayRotation)
}

/**
 * 竖持解析器：屏幕法向量接近水平时，仍取手机顶部的水平投影。
 * 这与平放共享同一物理“顶部=向”定义，区别在于姿态选择与稳定门禁，
 * 不用固定 90°/180° 偏移掩盖坐标错误。
 */
class UprightOrientationResolver : OrientationResolver {
    override fun resolve(rotationMatrix: FloatArray, displayRotation: Int): OrientationSample? =
        OrientationMath.resolve(rotationMatrix, displayRotation)
}

/** 纯 Kotlin 的 Android rotation-matrix 等价计算。矩阵为 3×3 row-major，R 将设备轴映射到世界轴。 */
object OrientationMath {
    fun resolve(rotationMatrix: FloatArray, displayRotation: Int): OrientationSample? {
        if (rotationMatrix.size != 9 || rotationMatrix.any { !it.isFinite() }) return null
        val axes = DisplayRotationMapping.axesFor(displayRotation)
        val right = vector(rotationMatrix, axes.right)
        val top = vector(rotationMatrix, axes.top)
        val normal = cross(right, top)
        val rightUnit = normalized(right) ?: return null
        val topUnit = normalized(top) ?: return null
        val normalUnit = normalized(normal) ?: return null
        val topHorizontal = hypot(topUnit[0].toDouble(), topUnit[1].toDouble()).toFloat()
        val rightHorizontal = hypot(rightUnit[0].toDouble(), rightUnit[1].toDouble()).toFloat()
        if (topHorizontal < 0.05f) return null

        val azimuth = Math.toDegrees(atan2(topUnit[0].toDouble(), topUnit[1].toDouble())).toFloat()
        val pitch = Math.toDegrees(atan2(topUnit[2].toDouble(), topHorizontal.toDouble())).toFloat()
        val roll = Math.toDegrees(atan2(rightUnit[2].toDouble(), rightHorizontal.toDouble())).toFloat()
        return OrientationSample(
            azimuthDeg = CircularMath.normalize(azimuth),
            pitchDeg = pitch,
            rollDeg = roll,
            screenNormalVerticalComponent = normalUnit[2],
            displayRotation = Math.floorMod(displayRotation, 4),
        )
    }

    private fun vector(matrix: FloatArray, axis: AxisVector): FloatArray {
        return floatArrayOf(
            axis.sign * matrix[axis.axis + 0],
            axis.sign * matrix[axis.axis + 3],
            axis.sign * matrix[axis.axis + 6],
        )
    }

    private fun cross(a: FloatArray, b: FloatArray): FloatArray = floatArrayOf(
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0],
    )

    private fun normalized(vector: FloatArray): FloatArray? {
        val length = sqrt(
            vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2],
        )
        if (!length.isFinite() || length < 1e-5f) return null
        return floatArrayOf(vector[0] / length, vector[1] / length, vector[2] / length)
    }
}
