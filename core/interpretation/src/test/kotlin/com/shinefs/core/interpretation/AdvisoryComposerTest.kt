package com.shinefs.core.interpretation

import com.shinefs.core.calendar.CivilTime
import com.shinefs.core.calendar.YijingTimeResolver
import com.shinefs.core.calendar.model.ChineseDate
import com.shinefs.core.calendar.model.CivilDateTime
import com.shinefs.core.calendar.provider.ChineseCalendarProvider
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.divination.context.YijingMomentContext
import com.shinefs.core.divination.context.YijingSpaceContext
import com.shinefs.core.divination.rule.MeihuaTimeDivinationRuleV1
import com.shinefs.core.divination.rule.TimeCastWithSpatialResponse
import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class AdvisoryComposerTest {

    private val beijing = TimeZone.getTimeZone("Asia/Shanghai")

    private class FixedProvider(private val date: ChineseDate) : ChineseCalendarProvider {
        override val version = "fake-calendar-interpret"
        override fun resolve(civil: CivilDateTime): ChineseDate = date
    }

    private fun guanmeiResult(withSpace: Boolean): com.shinefs.core.divination.result.DivinationResult {
        val time = YijingTimeResolver(FixedProvider(ChineseDate(1940, 12, 17, false)))
            .resolve(CivilTime.toEpochMillis(CivilDateTime(2026, 9, 4, 16, 0), beijing), beijing)
        val space = if (withSpace) {
            YijingSpaceContext(
                rawAzimuth = 182.4f, smoothedAzimuth = 182.4f,
                northReference = NorthReference.MAGNETIC,
                facingMountain = "午", sittingMountain = "子",
                directionTrigram = Trigram.LI,
                sensorAccuracy = null, stable = true, magneticInterference = false,
            )
        } else null
        return TimeCastWithSpatialResponse().cast(YijingMomentContext(time, space, null, time.epochMillis))
    }

    @Test
    fun `九段结构完整`() {
        val report = AdvisoryComposer().compose(guanmeiResult(false))
        assertEquals(9, report.sections.size)
        assertEquals(
            listOf(
                "一、时空数据", "二、起卦过程", "三、卦象结果", "四、周易原典",
                "五、互卦与体用", "六、五行与时令", "七、方位与方应", "八、本地白话释义", "九、起卦依据与说明",
            ),
            report.sections.map { it.title },
        )
        assertEquals("interpret-v1", report.interpreterVersion)
    }

    @Test
    fun `观梅占报告内容锚点`() {
        val report = AdvisoryComposer().compose(guanmeiResult(true))
        val text = report.render()
        // 时空
        assertTrue(text.contains("辰年腊月十七"))
        assertTrue(text.contains("申时"))
        // 起卦过程（方案 §20 轨迹格式）
        assertTrue(text.contains("34 = 5+12+17"))
        assertTrue(text.contains("43 除 6 余 1"))
        // 卦象
        assertTrue(text.contains("革"))
        assertTrue(text.contains("咸"))
        assertTrue(text.contains("姤"))
        // 原典
        assertTrue(text.contains("巳日乃孚"))
        assertTrue(text.contains("泽中有火"))
        assertTrue(text.contains("电子底本已校验"))
        assertTrue(!text.contains("原典：周易通行本电子底本（已核定）"))
        // 体用五行
        assertTrue(text.contains("体卦兑（金）"))
        assertTrue(text.contains("用卦离（火）"))
        assertTrue(text.contains("用克体"))
        assertTrue(text.contains("外部条件对主体形成较明显压力"))
        // 方应
        assertTrue(text.contains("午"))
        assertTrue(text.contains("坐子"))
        // 展示文字不暴露内部编号或工程术语
        assertTrue(text.contains("起卦方法：梅花易数 · 年月日时起卦"))
        assertTrue(text.contains("周易通行本电子底本"))
        assertTrue(text.contains("不使用智能生成"))
        assertTrue(!Regex("[A-Za-z]{2,}").containsMatchIn(text))
    }

    @Test
    fun `同输入同输出（0 随机）`() {
        val composer = AdvisoryComposer()
        val a = composer.compose(guanmeiResult(true))
        val b = composer.compose(guanmeiResult(true))
        assertEquals(a.render(), b.render())
    }

    @Test
    fun `系辞爻位引文`() {
        val report = AdvisoryComposer().compose(guanmeiResult(false)) // 观梅占初爻动
        assertTrue(report.render().contains("初辞拟之"))
    }
}
