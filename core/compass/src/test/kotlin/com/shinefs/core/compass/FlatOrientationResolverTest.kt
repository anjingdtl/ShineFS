package com.shinefs.core.compass

import com.shinefs.core.compass.orientation.FlatOrientationResolver
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlatOrientationResolverTest {
    @Test
    fun `平放取手机顶部水平投影并补偿显示旋转`() {
        val resolver = FlatOrientationResolver()
        val sample = resolver.resolve(yawMatrix(182.4f), 0)
        assertNotNull(sample)
        assertEquals(182.4f, sample!!.azimuthDeg, 0.01f)
        assertEquals(1f, sample.screenNormalVerticalComponent, 0.01f)
        assertEquals(0f, sample.pitchDeg, 0.01f)

        val rotated = resolver.resolve(identity(), 1)
        assertEquals(90f, rotated!!.azimuthDeg, 0.01f)
    }

    @Test
    fun `平放方向角跨零仍在合法范围`() {
        val sample = FlatOrientationResolver().resolve(yawMatrix(359.5f), 0)
        assertTrue(sample!!.azimuthDeg >= 0f && sample.azimuthDeg < 360f)
        assertEquals(359.5f, sample.azimuthDeg, 0.01f)
    }

    private fun identity() = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f,
    )

    private fun yawMatrix(degrees: Float): FloatArray {
        val radians = Math.toRadians(degrees.toDouble())
        val c = cos(radians).toFloat()
        val s = sin(radians).toFloat()
        return floatArrayOf(
            c, s, 0f,
            -s, c, 0f,
            0f, 0f, 1f,
        )
    }
}
