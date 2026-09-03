package com.shinefs.app.interpret

import com.shinefs.app.data.DivinationCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedInterpreterTest {

    private val interpreter = RuleBasedInterpreter()

    private fun case(sceneId: String = "front_door") = DivinationCase(
        id = "t", timestamp = 0, sceneId = sceneId, sceneName = "大门",
        azimuth = 182.4f, facingMountain = "午", sittingMountain = "子",
        facingTrigram = "离", facingElement = "火", stability = "良好",
        ruleId = "fixture-direction", ruleDisplayName = "x",
        rulesVersion = DivinationCase.RULES_VERSION,
        interpretationVersion = DivinationCase.INTERPRETATION_VERSION,
        upperTrigram = "坎", lowerTrigram = "离",
        originalHexagramOrder = 63, originalHexagramName = "既济",
        changingLine = 3, changedHexagramOrder = 3, changedHexagramName = "屯",
    )

    @Test
    fun `象义解析包含卦象结构事实`() {
        val text = interpreter.symbolism(case())
        assertTrue(text.contains("既济"))
        assertTrue(text.contains("下卦离"))
        assertTrue(text.contains("上卦坎"))
        assertTrue(text.contains("三爻"))
        assertTrue(text.contains("屯"))
    }

    @Test
    fun `空间解读包含方位五行与场景建议-不断吉凶`() {
        val text = interpreter.spatial(case())
        assertTrue(text.contains("向午"))
        assertTrue(text.contains("坐子"))
        assertTrue(text.contains("火"))
        assertTrue(text.contains("大门"))
        assertTrue(text.contains("不做飞星"))
        assertTrue(!text.contains("大凶"))
        assertTrue(!text.contains("大吉"))
    }

    @Test
    fun `八场景均有观察建议`() {
        val scenes = com.shinefs.app.data.Scenes.house.map { it.id }
        scenes.forEach { id ->
            val text = interpreter.spatial(case(id))
            assertTrue("场景 $id 无建议", text.contains("观察建议"))
        }
    }

    @Test
    fun `宜忌含免责声明`() {
        val text = interpreter.advisories()
        assertTrue(text.contains("不构成"))
    }

    @Test
    fun `爻名映射`() {
        assertEquals("初爻", interpreter.lineName(1))
        assertEquals("上爻", interpreter.lineName(6))
    }
}
