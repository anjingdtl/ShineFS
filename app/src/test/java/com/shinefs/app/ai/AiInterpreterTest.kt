package com.shinefs.app.ai

import com.shinefs.app.data.DivinationCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiInterpreterTest {

    private fun case() = DivinationCase(
        id = "t", timestamp = 0, sceneId = "front_door", sceneName = "住宅大门",
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
    fun `未配置AI返回NOT_CONFIGURED且无正文`() = runBlocking {
        val result = OfflineAiInterpreter().interpret(case())
        assertEquals(AiStatus.NOT_CONFIGURED, result.status)
        assertNull(result.plainText)
    }

    /** 产品方案 §10.1 的结构化输入字段全覆盖。 */
    @Test
    fun `结构化请求包含方案10_1全部字段`() {
        val json = buildStructuredRequest(case())
        listOf(
            "\"scene\": \"住宅大门\"", "\"azimuth\": 182.4", "\"facingMountain\": \"午\"",
            "\"sittingMountain\": \"子\"", "\"trigram\": \"离\"", "\"element\": \"火\"",
            "\"originalHexagram\": \"既济\"", "\"changingLine\": 3",
            "\"changedHexagram\": \"屯\"", "\"rulesVersion\"", "\"interpretationVersion\"",
        ).forEach { fragment ->
            assertTrue("缺少字段片段 $fragment", json.contains(fragment))
        }
    }
}
