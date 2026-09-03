package com.shinefs.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DivinationServiceTest {

    private fun service() = DivinationService(InMemoryCaseRepository())

    private fun reading(azimuth: Float = 182.4f, ts: Long = 1_772_000_000_000L) = LockedReading(
        azimuth = azimuth,
        facingMountain = "午",
        sittingMountain = "子",
        facingTrigram = "离",
        facingElement = "火",
        timestamp = ts,
        stability = "良好",
        accuracy = "高",
    )

    @Test
    fun `起卦产出完整卦例并入库`() {
        val s = service()
        val case = s.castWithDirection(reading(), Scenes.byId("front_door"))
        assertEquals("front_door", case.sceneId)
        assertEquals("大门", case.sceneName)
        assertEquals("午", case.facingMountain)
        assertTrue(case.originalHexagramOrder in 1..64)
        assertTrue(case.changedHexagramOrder in 1..64)
        assertTrue(case.changingLine in 1..6)
        assertEquals(DivinationCase.RULES_VERSION, case.rulesVersion)
        assertTrue(case.ruleId.startsWith("fixture"))
    }

    @Test
    fun `同读数同时刻重复起卦结果一致-可重复性`() {
        val repo = InMemoryCaseRepository()
        val s = DivinationService(repo)
        val a = s.castWithDirection(reading(), Scenes.generic)
        val b = s.castWithDirection(reading(), Scenes.generic)
        assertEquals(a.originalHexagramOrder, b.originalHexagramOrder)
        assertEquals(a.changingLine, b.changingLine)
        assertEquals(a.changedHexagramOrder, b.changedHexagramOrder)
    }

    @Test
    fun `卦例本卦与变卦满足确定性映射`() {
        val s = service()
        val case = s.castWithDirection(reading(azimuth = 0f), Scenes.generic)
        val core = com.shinefs.core.yijing.data.Hexagrams
        val original = core.byKingWenOrder(case.originalHexagramOrder)
        val changed = core.byKingWenOrder(case.changedHexagramOrder)
        // 变卦 = 本卦翻转动爻
        assertEquals(
            changed,
            com.shinefs.core.yijing.rules.HexagramOps.withChangingLine(original, case.changingLine),
        )
    }

    @Test
    fun `规则说明包含临时口径警示`() {
        val text = service().ruleExplain()
        assertTrue(text.contains("临时"))
        assertTrue(text.contains("上卦"))
    }

    @Test
    fun `仓储按宅局分组`() {
        val repo = InMemoryCaseRepository()
        val s = DivinationService(repo)
        s.castWithDirection(reading(), Scenes.byId("living_room"), houseAuditId = "audit-1")
        s.castWithDirection(reading(azimuth = 45f), Scenes.byId("front_door"), houseAuditId = "audit-1")
        s.castWithDirection(reading(azimuth = 90f), Scenes.generic)
        assertEquals(2, repo.byHouseAudit("audit-1").size)
        assertEquals(3, repo.all().size)
    }
}
