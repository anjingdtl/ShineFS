package com.shinefs.core.compass

/** 环形角度数学。约定：角度域 [0,360)，北=0°，顺时针为正。 */
object CircularMath {

    fun normalize(degrees: Float): Float {
        if (degrees.isNaN()) return Float.NaN
        val wrapped = degrees % 360f
        val result = if (wrapped < 0f) wrapped + 360f else wrapped
        // 浮点负零漂（如 -5.9e-15 + 360）可能恰好落在 360.0，收回 0
        return if (result >= 360f) 0f else result
    }

    /** 从 from 到 to 的最短有符号差，范围 [-180, 180)。 */
    fun shortestDiff(from: Float, to: Float): Float {
        return ((to - from + 540f) % 360f) - 180f
    }

    /** 环形均值（方向统计均值角）。输入为角度列表。 */
    fun circularMean(angles: List<Float>): Float {
        require(angles.isNotEmpty())
        val sinSum = angles.sumOf { kotlin.math.sin(Math.toRadians(it.toDouble())) }
        val cosSum = angles.sumOf { kotlin.math.cos(Math.toRadians(it.toDouble())) }
        if (sinSum == 0.0 && cosSum == 0.0) return Float.NaN
        val mean = Math.toDegrees(kotlin.math.atan2(sinSum, cosSum))
        return normalize(mean.toFloat())
    }

    /** 环形标准差（sqrt(-2 ln R)，R 为合成向量长度），单位度。空列表返回 NaN。 */
    fun circularStdDeg(angles: List<Float>): Float {
        if (angles.isEmpty()) return Float.NaN
        val n = angles.size
        val sinSum = angles.sumOf { kotlin.math.sin(Math.toRadians(it.toDouble())) }
        val cosSum = angles.sumOf { kotlin.math.cos(Math.toRadians(it.toDouble())) }
        val r = kotlin.math.sqrt(sinSum * sinSum + cosSum * cosSum) / n
        if (r >= 1.0) return 0f
        return kotlin.math.sqrt(-2.0 * kotlin.math.ln(r)).toFloat() * (180f / Math.PI.toFloat())
    }
}
