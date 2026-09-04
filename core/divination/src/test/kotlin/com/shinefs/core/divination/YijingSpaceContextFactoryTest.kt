package com.shinefs.core.divination

import com.shinefs.core.compass.CompassEngine
import com.shinefs.core.compass.SensorAccuracy
import com.shinefs.core.divination.context.YijingSpaceContextFactory
import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YijingSpaceContextFactoryTest {

    @Test
    fun `罗盘状态到空间上下文全字段`() {
        val engine = CompassEngine()
        engine.onAccuracy(SensorAccuracy.HIGH, CompassEngine.AccuracySource.ORIENTATION)
        engine.onAccuracy(SensorAccuracy.MEDIUM, CompassEngine.AccuracySource.MAGNETIC)
        repeat(40) { engine.onAzimuth(182.4f, 0f, 0f) }
        val ctx = YijingSpaceContextFactory.fromCompassState(engine.state)

        assertNotNull(ctx)
        assertEquals(182.4f, ctx!!.smoothedAzimuth!!, 0.5f)
        assertEquals("午", ctx.facingMountain)
        assertEquals("子", ctx.sittingMountain)
        assertEquals(Trigram.LI, ctx.directionTrigram)
        assertTrue(ctx.stable)
        assertTrue(!ctx.magneticInterference)
        assertEquals(SensorAccuracy.HIGH, ctx.sensorAccuracy!!.orientationAccuracy)
        assertEquals(SensorAccuracy.MEDIUM, ctx.sensorAccuracy!!.magneticAccuracy)
    }

    @Test
    fun `无定位返回 null 不伪造方向`() {
        assertNull(YijingSpaceContextFactory.fromCompassState(CompassEngine().state))
    }

    @Test
    fun `跨零方位`() {
        val engine = CompassEngine()
        repeat(40) { engine.onAzimuth(359.5f, 0f, 0f) }
        val ctx = YijingSpaceContextFactory.fromCompassState(engine.state)!!
        assertEquals("子", ctx.facingMountain) // 359.5° ∈ [352.5, 360) → 子
        assertEquals("午", ctx.sittingMountain)
        assertEquals(Trigram.KAN, ctx.directionTrigram) // 北
    }
}
