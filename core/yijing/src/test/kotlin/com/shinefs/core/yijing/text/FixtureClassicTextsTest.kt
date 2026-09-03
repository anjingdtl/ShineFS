package com.shinefs.core.yijing.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FixtureClassicTextsTest {

    private val repo = FixtureClassicTexts()

    @Test
    fun `示例六卦可查且均标记未核定`() {
        listOf(1, 2, 3, 4, 63, 64).forEach { order ->
            val text = repo.byKingWenOrder(order)
            assertNotNull("卦 $order 缺失", text)
            assertFalse(text!!.verified)
            assertEquals("classic-fixture-v0", text.version)
            assertTrue(text.judgment.isNotBlank())
            assertTrue(text.imageText.isNotBlank())
        }
    }

    @Test
    fun `爻辞一律未录入`() {
        listOf(1, 2, 3, 4, 63, 64).forEach { order ->
            assertEquals(emptyList<String>(), repo.byKingWenOrder(order)!!.lineTexts)
        }
    }

    @Test
    fun `未收录卦返回null-由UI显式降级提示`() {
        assertNull(repo.byKingWenOrder(48))
    }

    @Test
    fun `卦名与卦序一致`() {
        assertEquals("乾", repo.byKingWenOrder(1)!!.hexagramName)
        assertEquals("未济", repo.byKingWenOrder(64)!!.hexagramName)
    }
}
