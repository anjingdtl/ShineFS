package com.shinefs.app.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassCapabilityTest {

    @Test
    fun `有RotationVector即为FULL`() {
        assertEquals(CompassCapabilityLevel.FULL, CompassCapability.of(true, false, false).level)
    }

    @Test
    fun `磁力计加加速度计回退组合为FULL`() {
        assertEquals(CompassCapabilityLevel.FULL, CompassCapability.of(false, true, true).level)
    }

    @Test
    fun `无磁力计只能LIMITED-不得伪造方向`() {
        assertEquals(CompassCapabilityLevel.LIMITED, CompassCapability.of(false, false, true).level)
        assertEquals(CompassCapabilityLevel.LIMITED, CompassCapability.of(false, false, false).level)
    }
}
