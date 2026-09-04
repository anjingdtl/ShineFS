package com.shinefs.app.data

import com.shinefs.core.calendar.CivilTime
import com.shinefs.core.calendar.model.CivilDateTime
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** V2 起卦编排测试（正式核心链，0 Fixture / 0 AI）。 */
class DivinationServiceV2Test {

    private val beijing = TimeZone.getTimeZone("Asia/Shanghai")

    private fun service(repo: CaseRepository = InMemoryCaseRepository()) =
        DivinationServiceV2(repo, timeZone = beijing)

    private val fixedMillis =
        CivilTime.toEpochMillis(CivilDateTime(2026, 9, 4, 16, 0), beijing)

    @Test
    fun `纯时间起卦全字段留存`() {
        val repo = InMemoryCaseRepository()
        val svc = DivinationServiceV2(repo, timeZone = beijing)
        val case = svc.castTime(scene = Scenes.generic, atMillis = fixedMillis)

        // 2026-09-04 16:00 东八区：丙午年七月廿三 申时（TableChineseCalendarProvider 真实历表）
        assertEquals("午", case.yearBranch)
        assertEquals(7, case.yearBranchNumber)
        assertEquals(7, case.lunarMonthNumber)
        assertEquals(23, case.lunarDayNumber)
        assertEquals(9, case.hourBranchNumber)
        assertEquals("申", case.hourBranch)
        assertEquals("meihua-time-v1", case.ruleId)
        assertEquals("rules-v2.0", case.rulesVersion)
        assertEquals("interpret-v1", case.interpretationVersion)
        assertEquals("zhouyi-corpus-v1", case.classicCorpusVersion)
        assertEquals("calendar-table-v1", case.calendarVersion)
        assertEquals(DivinationCase.CAST_MODE_TIME, case.castMode)
        assertNull(case.azimuth)
        assertNotNull(case.nuclearHexagramName)
        assertNotNull(case.tiTrigram)
        assertNotNull(case.elementRelation)
        assertTrue(case.calculationTrace!!.contains("37 = 7+7+23"))
        assertTrue(case.reportText!!.contains("一、时空数据"))
        assertTrue(case.reportText!!.contains("九、规则来源与版本"))
        assertTrue(!case.legacyFixture)
        assertEquals(1, repo.all().size)
    }

    @Test
    fun `同毫秒同输出（确定性 + id 除外）`() {
        val svc = service()
        val a = svc.castTime(scene = Scenes.generic, atMillis = fixedMillis)
        val b = svc.castTime(scene = Scenes.generic, atMillis = fixedMillis)
        assertNotEquals(a.id, b.id) // id 仅记录主键
        assertEquals(a.copy(id = "x"), b.copy(id = "x"))
        assertEquals(a.reportText, b.reportText)
        assertEquals(a.calculationTrace, b.calculationTrace)
    }

    @Test
    fun `时空合参 - 空间不改时间卦`() {
        val reading = LockedReading(
            azimuth = 182.4f, facingMountain = "午", sittingMountain = "子",
            facingTrigram = "离", facingElement = "火",
            timestamp = fixedMillis, stability = "良好", accuracy = "高",
        )
        val svc = service()
        val timeCase = svc.castTime(scene = Scenes.generic, atMillis = fixedMillis)
        val spaceCase = svc.castTimeSpace(reading = reading, scene = Scenes.generic)

        // 时间卦四数与卦象完全一致
        assertEquals(timeCase.originalHexagramOrder, spaceCase.originalHexagramOrder)
        assertEquals(timeCase.changingLine, spaceCase.changingLine)
        assertEquals(timeCase.changedHexagramOrder, spaceCase.changedHexagramOrder)
        // 空间字段留存
        assertEquals("午", spaceCase.facingMountain)
        assertEquals("子", spaceCase.sittingMountain)
        assertEquals("离", spaceCase.facingTrigram)
        assertEquals("MAGNETIC", spaceCase.northReference)
        assertEquals(DivinationCase.CAST_MODE_TIME_SPACE, spaceCase.castMode)
        assertTrue(spaceCase.reportText!!.contains("七、方位与方应"))
    }

    @Test
    fun `离线复算与原记录一致`() {
        val svc = service()
        val case = svc.castTime(scene = Scenes.generic, atMillis = fixedMillis)
        val recompute = svc.recomputeTrace(case)
        assertNotNull(recompute)
        assertTrue(recompute!!.endsWith("✓ 与原记录一致"))
    }
}
