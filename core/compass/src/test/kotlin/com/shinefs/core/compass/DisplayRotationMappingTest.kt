package com.shinefs.core.compass

import com.shinefs.core.compass.orientation.DisplayRotationMapping
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayRotationMappingTest {
    @Test
    fun `四种显示旋转的显示顶部映射`() {
        assertEquals(1, DisplayRotationMapping.axesFor(0).top.axis)
        assertEquals(1, DisplayRotationMapping.axesFor(0).top.sign)
        assertEquals(0, DisplayRotationMapping.axesFor(1).top.axis)
        assertEquals(1, DisplayRotationMapping.axesFor(1).top.sign)
        assertEquals(1, DisplayRotationMapping.axesFor(2).top.axis)
        assertEquals(-1, DisplayRotationMapping.axesFor(2).top.sign)
        assertEquals(0, DisplayRotationMapping.axesFor(3).top.axis)
        assertEquals(-1, DisplayRotationMapping.axesFor(3).top.sign)
        assertEquals(270, DisplayRotationMapping.degreesFor(-1))
    }
}
