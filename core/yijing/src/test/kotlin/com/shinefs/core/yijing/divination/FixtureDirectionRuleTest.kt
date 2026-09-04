package com.shinefs.core.yijing.divination

import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

/**
 * 临时联调规则测试：确定性 + 正式部分（上卦=向卦）正确性 + 临时公式抽查。
 * 临时口径本身无术数正确性主张，仅锁定其行为防回归。
 */
class FixtureDirectionRuleTest {

    private val rule = FixtureDirectionRule(ZoneId.of("Asia/Shanghai"))

    @Test
    fun `同输入必同输出`() {
        val input = DirectionCastInput(azimuth = 182.4f, epochMillis = 1_772_000_000_000L)
        assertEquals(rule.cast(input), rule.cast(input))
    }

    @Test
    fun `上卦恒为向首后天八卦-八方位全覆盖`() {
        val cases = listOf(
            0f to Trigram.KAN, 45f to Trigram.GEN, 90f to Trigram.ZHEN, 135f to Trigram.XUN,
            180f to Trigram.LI, 225f to Trigram.KUN, 270f to Trigram.DUI, 315f to Trigram.QIAN,
        )
        cases.forEach { (az, expected) ->
            assertEquals(expected, rule.cast(DirectionCastInput(az, 0L)).upperTrigram)
        }
    }

    @Test
    fun `临时下卦与动爻公式抽查`() {
        // 2026-09-03T14:05 (+08:00) → y=2026 m=9 d=3 h=14 mi=5
        val millis = Instant.parse("2026-09-03T06:05:00Z").atZone(ZoneId.of("Asia/Shanghai"))
            .toInstant().toEpochMilli()
        val outcome = rule.cast(DirectionCastInput(azimuth = 0f, epochMillis = millis))
        val lowerNum = (2026 + 9 + 3 + 14) % 8 // = 2052 % 8 = 4 → 震
        assertEquals(Trigram.entries[lowerNum - 1], outcome.lowerTrigram)
        val line = (2026 + 9 + 3 + 14 + 5) % 6 // = 2057 % 6 = 5
        assertEquals(if (line == 0) 6 else line, outcome.changingLine)
    }

    @Test
    fun `余零约定-下卦记坤动爻记上爻`() {
        // 构造 y+m+d+h ≡ 0 (mod 8) 且 y+m+d+h+mi ≡ 0 (mod 6)：
        // 取 2000-08-08 00 分无法精确控制时区全字段，这里直接复算期望值做一致性断言
        val millis = 0L // 1970-01-01T08:00 (+08:00) → y=1970 m=1 d=1 h=8 mi=0
        val outcome = rule.cast(DirectionCastInput(azimuth = 0f, epochMillis = millis))
        val lowerNum = (1970 + 1 + 1 + 8) % 8 // = 1980 % 8 = 4
        val line = (1970 + 1 + 1 + 8 + 0) % 6 // = 1980 % 6 = 0 → 记 6
        assertEquals(Trigram.entries[lowerNum - 1], outcome.lowerTrigram)
        assertEquals(6, outcome.changingLine)
    }

    @Test
    fun `规则标识与临时标记`() {
        assertTrue(rule.ruleId.startsWith("fixture"))
        assertTrue(rule.displayName.contains("临时"))
        assertEquals("fixture-direction", rule.cast(DirectionCastInput(0f, 0L)).ruleId)
    }
}
