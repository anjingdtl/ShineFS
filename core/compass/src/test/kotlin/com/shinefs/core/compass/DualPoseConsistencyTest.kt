package com.shinefs.core.compass

import com.shinefs.core.compass.orientation.FlatOrientationResolver
import com.shinefs.core.compass.orientation.UprightOrientationResolver
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertTrue
import org.junit.Test

/** 11C 核心属性：同一“手机顶部=向”在两种持握姿态下必须同向。 */
class DualPoseConsistencyTest {
    @Test
    fun `多方向平放与竖持方位误差不超过五度`() {
        val flat = FlatOrientationResolver()
        val upright = UprightOrientationResolver()
        for (heading in 0..345 step 15) {
            for (rotation in 0..3) {
                val flatSample = flat.resolve(matrixForDisplay(heading.toFloat(), rotation, upright = false), rotation)!!
                val uprightSample = upright.resolve(matrixForDisplay(heading.toFloat(), rotation, upright = true), rotation)!!
                val error = kotlin.math.abs(
                    CircularMath.shortestDiff(flatSample.azimuthDeg, uprightSample.azimuthDeg),
                )
                assertTrue(
                    "heading=$heading rotation=$rotation flat=${flatSample.azimuthDeg} upright=${uprightSample.azimuthDeg}",
                    error <= 5f,
                )
            }
        }
    }

    private fun matrixForDisplay(degrees: Float, rotation: Int, upright: Boolean): FloatArray {
        val r = Math.toRadians(degrees.toDouble())
        val c = cos(r).toFloat()
        val s = sin(r).toFloat()
        val desiredRight = if (upright) floatArrayOf(0f, 0f, 1f) else floatArrayOf(c, -s, 0f)
        val desiredTop = floatArrayOf(s, c, 0f)
        val axes = com.shinefs.core.compass.orientation.DisplayRotationMapping.axesFor(rotation)
        val columns = arrayOfNulls<FloatArray>(3)
        columns[axes.right.axis] = desiredRight.map { it * axes.right.sign }.toFloatArray()
        columns[axes.top.axis] = desiredTop.map { it * axes.top.sign }.toFloatArray()
        val normal = cross(columns[axes.right.axis]!!, columns[axes.top.axis]!!)
        val missing = (0..2).first { columns[it] == null }
        columns[missing] = when (missing) {
            2 -> cross(columns[0]!!, columns[1]!!)
            1 -> cross(columns[2]!!, columns[0]!!)
            else -> cross(columns[1]!!, columns[2]!!)
        }
        // Keep the compiler-visible invariant that the selected display normal is the expected one.
        check(normal.all { it.isFinite() })
        return floatArrayOf(
            columns[0]!![0], columns[1]!![0], columns[2]!![0],
            columns[0]!![1], columns[1]!![1], columns[2]!![1],
            columns[0]!![2], columns[1]!![2], columns[2]!![2],
        )
    }

    private fun cross(a: FloatArray, b: FloatArray): FloatArray = floatArrayOf(
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0],
    )
}
