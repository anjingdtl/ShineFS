package com.shinefs.core.compass

import com.shinefs.core.compass.orientation.UprightOrientationResolver
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UprightOrientationResolverTest {
    @Test
    fun `竖持不把屏幕朝向误当成手机顶部`() {
        val sample = UprightOrientationResolver().resolve(uprightMatrix(182.4f), 0)
        assertNotNull(sample)
        assertEquals(182.4f, sample!!.azimuthDeg, 0.01f)
        assertEquals(0f, sample.screenNormalVerticalComponent, 0.01f)
        assertTrue(kotlin.math.abs(sample.rollDeg) > 89f)
    }

    @Test
    fun `竖持四种显示旋转与平放同口径`() {
        val resolver = UprightOrientationResolver()
        assertEquals(0f, resolver.resolve(identity(), 0)!!.azimuthDeg, 0.01f)
        assertEquals(90f, resolver.resolve(identity(), 1)!!.azimuthDeg, 0.01f)
        assertEquals(180f, resolver.resolve(identity(), 2)!!.azimuthDeg, 0.01f)
        assertEquals(270f, resolver.resolve(identity(), 3)!!.azimuthDeg, 0.01f)
    }

    private fun identity() = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f,
    )

    private fun uprightMatrix(degrees: Float): FloatArray {
        val radians = Math.toRadians(degrees.toDouble())
        val c = cos(radians).toFloat()
        val s = sin(radians).toFloat()
        // right = world up；top = heading；screen normal = right × top（水平）。
        return floatArrayOf(
            0f, s, -c,
            0f, c, s,
            1f, 0f, 0f,
        )
    }
}
