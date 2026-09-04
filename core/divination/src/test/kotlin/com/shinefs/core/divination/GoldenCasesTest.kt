package com.shinefs.core.divination

import com.shinefs.core.calendar.CivilTime
import com.shinefs.core.calendar.YijingTimeResolver
import com.shinefs.core.calendar.model.ChineseDate
import com.shinefs.core.calendar.model.CivilDateTime
import com.shinefs.core.calendar.provider.ChineseCalendarProvider
import com.shinefs.core.divination.classimage.ClassImageTable
import com.shinefs.core.divination.context.YijingMomentContext
import com.shinefs.core.divination.context.YijingSpaceContext
import com.shinefs.core.divination.rule.MeihuaMath
import com.shinefs.core.divination.rule.MeihuaPostHeavenObjectDirectionRuleV1
import com.shinefs.core.divination.rule.MeihuaTimeDivinationRuleV1
import com.shinefs.core.divination.rule.TimeCastWithSpatialResponse
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.yijing.model.ElementRelation
import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

/** 金标准古例回归（DOCS/YIJING_RULES.md §11）。 */
class GoldenCasesTest {

    private val beijing = TimeZone.getTimeZone("Asia/Shanghai")

    private class FixedProvider(private val date: ChineseDate) : ChineseCalendarProvider {
        override val version = "fake-calendar-golden"
        override fun resolve(civil: CivilDateTime): ChineseDate = date
    }

    private fun timeCtx(date: ChineseDate, hour: Int): com.shinefs.core.calendar.model.YijingTimeContext =
        YijingTimeResolver(FixedProvider(date)).resolve(
            CivilTime.toEpochMillis(CivilDateTime(2026, 9, 4, hour, 0), beijing),
            beijing,
        )

    // ---------- 模式 A：梅花年月日时 ----------

    @Test
    fun `观梅占 - 辰年十二月十七日申时 泽火革之初爻 变泽山咸`() {
        val result = MeihuaTimeDivinationRuleV1().cast(timeCtx(ChineseDate(1940, 12, 17, false), 16))
        assertEquals("兑", result.upperTrigram.chineseName)
        assertEquals("离", result.lowerTrigram.chineseName)
        assertEquals("革", result.original.chineseName)
        assertEquals(1, result.changingLine)
        assertEquals("咸", result.changed.chineseName)
        assertEquals("姤", result.nuclear!!.hexagram.chineseName)
        assertEquals("兑", result.tiYong!!.ti.chineseName)
        assertEquals("离", result.tiYong.yong.chineseName)
        assertEquals(ElementRelation.YONG_CONTROLS_TI, result.elementRelation)
        // 轨迹（V2.0 方案 §20 示例格式）
        val trace = result.trace.render()
        assertTrue(trace.contains("34 = 5+12+17"))
        assertTrue(trace.contains("34 除 8 余 2 → 兑"))
        assertTrue(trace.contains("43 除 8 余 3 → 离"))
        assertTrue(trace.contains("43 除 6 余 1"))
        assertTrue(trace.contains("泽火革").not()) // 名称不带前缀，用 chineseName 断言
        assertEquals("meihua-time-v1", result.rule.ruleId)
    }

    @Test
    fun `牡丹占 - 巳年三月十六日卯时 天风姤之五爻 变火风鼎`() {
        val result = MeihuaTimeDivinationRuleV1().cast(timeCtx(ChineseDate(1965, 3, 16, false), 6))
        assertEquals("乾", result.upperTrigram.chineseName)
        assertEquals("巽", result.lowerTrigram.chineseName)
        assertEquals("姤", result.original.chineseName)
        assertEquals(5, result.changingLine)
        assertEquals("鼎", result.changed.chineseName)
        assertTrue(result.trace.render().contains("25 = 6+3+16"))
        assertTrue(result.trace.render().contains("29 除 6 余 5"))
    }

    // ---------- 模式 B：梅花后天端法 ----------

    @Test
    fun `老人有忧色占 - 乾配老人 巽方 卯时 天风姤四爻动`() {
        val elder = ClassImageTable.byId("qian-elder")
        val result = MeihuaPostHeavenObjectDirectionRuleV1().cast(
            objectTrigram = elder.trigram,
            objectLabel = elder.label,
            directionTrigram = Trigram.XUN,
            time = timeCtx(ChineseDate(1949, 12, 15, false), 6), // 己丑日卯时（任意日，端法只用时辰）
        )
        assertEquals("姤", result.original.chineseName)
        assertEquals(4, result.changingLine)
        assertEquals("巽", result.changed.chineseName) // 姤四爻动变纯巽
        assertTrue(result.trace.render().contains("1+5+4 = 10 除 6 余 4"))
    }

    @Test
    fun `少年有喜色占 - 艮配少年 离方 午时 山火贲五爻动`() {
        val teen = ClassImageTable.byId("gen-teenager")
        val result = MeihuaPostHeavenObjectDirectionRuleV1().cast(
            teen.trigram, teen.label, Trigram.LI, timeCtx(ChineseDate(2008, 8, 1, false), 12),
        )
        assertEquals("贲", result.original.chineseName)
        assertEquals(5, result.changingLine)
        assertTrue(result.trace.render().contains("7+3+7 = 17 除 6 余 5"))
    }

    @Test
    fun `牛哀鸣占 - 坤配牛 坎方 午时 地水师三爻动 变地风升`() {
        val cow = ClassImageTable.byId("kun-cow")
        val result = MeihuaPostHeavenObjectDirectionRuleV1().cast(
            cow.trigram, cow.label, Trigram.KAN, timeCtx(ChineseDate(2003, 3, 5, false), 12),
        )
        assertEquals("师", result.original.chineseName)
        assertEquals(3, result.changingLine)
        assertEquals("升", result.changed.chineseName) // 师六三（坎上爻）阴变阳，下互成巽 → 地风升
        assertTrue(result.trace.render().contains("8+6+7 = 21 除 6 余 3"))
    }

    // ---------- 模式 C：时空合参 ----------

    @Test
    fun `时空合参 - 空间不修改时间卦 方应为事实层`() {
        val time = timeCtx(ChineseDate(1940, 12, 17, false), 16)
        val pureTime = MeihuaTimeDivinationRuleV1().cast(time)
        val space = YijingSpaceContext(
            rawAzimuth = 182.4f,
            smoothedAzimuth = 182.4f,
            northReference = NorthReference.MAGNETIC,
            facingMountain = "午",
            sittingMountain = "子",
            directionTrigram = Trigram.LI,
            sensorAccuracy = null,
            stable = true,
            magneticInterference = false,
        )
        val merged = TimeCastWithSpatialResponse().cast(YijingMomentContext(time, space, null, time.epochMillis))

        // 卦象与 A 法完全一致（空间不改卦）
        assertEquals(pureTime.original, merged.original)
        assertEquals(pureTime.changed, merged.changed)
        assertEquals(pureTime.changingLine, merged.changingLine)
        assertEquals(pureTime.nuclear, merged.nuclear)
        assertEquals(pureTime.tiYong, merged.tiYong)

        // 方应：午山离宫，体兑金，离火克金
        assertEquals(Trigram.LI, merged.spatialResponse!!.directionTrigram)
        assertEquals(ElementRelation.YONG_CONTROLS_TI, merged.spatialResponse.relationToTi)
        assertEquals("spatial-response-v1", merged.spatialResponse.sourceRuleId)
        assertTrue(merged.trace.render().contains("向山：午"))
        assertTrue(merged.trace.render().contains("坐山：子"))
    }

    @Test
    fun `时空合参 - 无空间数据照常成卦`() {
        val time = timeCtx(ChineseDate(1940, 12, 17, false), 16)
        val result = TimeCastWithSpatialResponse().cast(YijingMomentContext(time, null, null, time.epochMillis))
        assertEquals("革", result.original.chineseName)
        assertNull(result.spaceContext)
        assertNull(result.spatialResponse)
    }

    // ---------- 确定性与余数归一 ----------

    @Test
    fun `同输入同输出（含轨迹）`() {
        val time = timeCtx(ChineseDate(1940, 12, 17, false), 16)
        val a = MeihuaTimeDivinationRuleV1().cast(time)
        val b = MeihuaTimeDivinationRuleV1().cast(time)
        assertEquals(a, b)
        assertEquals(a.trace.render(), b.trace.render())
    }

    @Test
    fun `余数归一 - 余0取8取6`() {
        assertEquals(8, MeihuaMath.normalize8(8))
        assertEquals(8, MeihuaMath.normalize8(16))
        assertEquals(2, MeihuaMath.normalize8(34))
        assertEquals(3, MeihuaMath.normalize8(43))
        assertEquals(6, MeihuaMath.normalize6(6))
        assertEquals(6, MeihuaMath.normalize6(12))
        assertEquals(1, MeihuaMath.normalize6(43))
        assertEquals(5, MeihuaMath.normalize6(29))
    }

    @Test
    fun `年支数与日干支联动 - 民用午夜日界下 23点半仍本日`() {
        // 22:59 亥时 与 23:30 子时 的时辰数不同，但（CIVIL_MIDNIGHT）农历日相同
        val ctx1 = YijingTimeResolver(FixedProvider(ChineseDate(1940, 12, 17, false)))
            .resolve(CivilTime.toEpochMillis(CivilDateTime(2026, 9, 4, 22, 59), beijing), beijing)
        val ctx2 = YijingTimeResolver(FixedProvider(ChineseDate(1940, 12, 17, false)))
            .resolve(CivilTime.toEpochMillis(CivilDateTime(2026, 9, 4, 23, 30), beijing), beijing)
        assertEquals(12, ctx1.hourBranchNumber) // 亥
        assertEquals(1, ctx2.hourBranchNumber) // 子
        assertEquals(17, ctx1.lunarDayNumber)
        assertEquals(17, ctx2.lunarDayNumber)
    }
}

class ClassImageTableTest {

    @Test
    fun `类象表条目与引用`() {
        assertEquals(18, ClassImageTable.all.size)
        assertEquals(Trigram.QIAN, ClassImageTable.byId("qian-elder").trigram)
        assertEquals(Trigram.GEN, ClassImageTable.byId("gen-teenager").trigram)
        assertEquals(Trigram.KUN, ClassImageTable.byId("kun-cow").trigram)
        assertTrue(ClassImageTable.all.all { it.shuoguaCitation.isNotBlank() })
        assertEquals("meihua-classimage-v1", ClassImageTable.VERSION)
        assertEquals("meihua-classimage-v1", ClassImageTable.manifest.ruleId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `未知类象拒绝`() {
        ClassImageTable.byId("modern-phone")
    }
}
