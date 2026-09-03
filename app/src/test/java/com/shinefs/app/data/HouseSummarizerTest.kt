package com.shinefs.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseSummarizerTest {

    private fun case(
        id: String,
        auditId: String?,
        sceneId: String,
        element: String,
        hex: String = "井",
    ) = DivinationCase(
        id = id, timestamp = 0, sceneId = sceneId, sceneName = Scenes.byId(sceneId).name,
        azimuth = 0f, facingMountain = "子", sittingMountain = "午",
        facingTrigram = "坎", facingElement = element, stability = "良好",
        ruleId = "fixture-direction", ruleDisplayName = "x",
        rulesVersion = DivinationCase.RULES_VERSION,
        interpretationVersion = DivinationCase.INTERPRETATION_VERSION,
        upperTrigram = "坎", lowerTrigram = "巽",
        originalHexagramOrder = 48, originalHexagramName = hex,
        changingLine = 3, changedHexagramOrder = 29, changedHexagramName = "坎",
        houseAuditId = auditId,
    )

    @Test
    fun `空测局零测量`() {
        val s = HouseSummarizer.summarize("a1", emptyList())
        assertEquals(0, s.measuredCount)
        assertEquals(8, s.totalCount)
        assertTrue(s.entries.isEmpty())
    }

    @Test
    fun `只统计本测局且按场景去重-保留最新`() {
        val cases = listOf(
            case("1", "a1", "front_door", "火"),
            case("2", "a2", "living_room", "金"), // 他局，不计入
            case("3", "a1", "front_door", "水"), // 同场景重复，保留最新
            case("4", "a1", "stove", "木"),
        )
        val s = HouseSummarizer.summarize("a1", cases)
        assertEquals(2, s.measuredCount)
        assertEquals("水", s.entries.first { it.sceneId == "front_door" }.element)
        assertEquals(mapOf("水" to 1, "木" to 1), s.elementCounts)
    }

    @Test
    fun `摘要文本含场景坐向卦象与边界声明`() {
        val s = HouseSummarizer.summarize("a1", listOf(case("1", "a1", "front_door", "火", "既济")))
        val text = HouseSummarizer.summaryText(s)
        assertTrue(text.contains("1/8"))
        assertTrue(text.contains("大门"))
        assertTrue(text.contains("向子坐午"))
        assertTrue(text.contains("《既济》"))
        assertTrue(text.contains("火×1"))
        assertTrue(text.contains("不做飞星"))
    }

    @Test
    fun `满测达成完成态`() {
        val cases = Scenes.house.mapIndexed { i, sc -> case("$i", "a1", sc.id, "金") }
        val s = HouseSummarizer.summarize("a1", cases)
        assertEquals(8, s.measuredCount)
        assertTrue(s.complete)
    }
}
