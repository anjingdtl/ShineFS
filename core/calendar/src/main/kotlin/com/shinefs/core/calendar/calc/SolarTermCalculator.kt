package com.shinefs.core.calendar.calc

import com.shinefs.core.calendar.model.SolarTerm
import com.shinefs.core.calendar.model.SolarTermInfo
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

/**
 * 二十四节气计算（`solar-term-meeus-v1`，E 级，S-E03）。
 *
 * 算法：Jean Meeus《Astronomical Algorithms》(2nd ed.)
 * - §25 太阳几何平黄经 / 中心差 → 真黄经；章动+光行差改正 → 视黄经；
 * - ΔT 采用 Espenak & Meeus (2006) 分段多项式（NASA Eclipse 网站公布式）；
 * - 节气瞬时 = 视黄经到达 15° 倍数的时刻，Newton 迭代（导数 ≈ 0.9856°/天）。
 *
 * 精度声明：时刻误差典型 < ±2 分钟；节气仅入时令/旺衰上下文与展示，**不入起卦公式**
 * （V2.0 方案 §5.5），该精度满足日级展示与月建判定。
 */
object SolarTermCalculator {

    /** 儒略日（UT）→ 毫秒（epoch 1970-01-01T00:00 UT）。 */
    private const val MILLIS_PER_DAY = 86400000.0
    private const val JULIAN_EPOCH_MILLIS = -2440587.5 * MILLIS_PER_DAY

    fun jdUtToEpochMillis(jdUt: Double): Long = ((jdUt * MILLIS_PER_DAY) + JULIAN_EPOCH_MILLIS).toLong()

    fun epochMillisToJdUt(epochMillis: Long): Double =
        (epochMillis - JULIAN_EPOCH_MILLIS) / MILLIS_PER_DAY

    /** 某时刻所处节气（按该时刻太阳视黄经判定）。 */
    fun termAt(epochMillis: Long): SolarTerm {
        val jdTt = epochMillisToJdUt(epochMillis) + deltaTSeconds(epochMillis) / 86400.0
        return SolarTerm.ofApparentLongitude(apparentSolarLongitude(jdTt))
    }

    /** 某时刻所处节气 + 该节气开始瞬时。 */
    fun termInfoAt(epochMillis: Long): SolarTermInfo {
        val term = termAt(epochMillis)
        return SolarTermInfo(term = term, startEpochMillis = termStartEpochMillis(term, epochMillis))
    }

    /** 计算节气 [term] 的开始瞬时；[nearMillis] 为搜索参考时刻（取该节气附近时刻收敛更快）。 */
    fun termStartEpochMillis(term: SolarTerm, nearMillis: Long): Long {
        val target = term.targetLongitude.toDouble()
        var jdUt = epochMillisToJdUt(nearMillis)
        // 初值：按平黄经线性估算到目标附近（保证迭代落在正确的一年内）
        jdUt += normalizeDeg180(target - apparentSolarLongitude(jdUt + deltaTSeconds(nearMillis) / 86400.0)) / 0.9856
        var jd = jdUt + deltaTSeconds(jdUtToEpochMillis(jdUt)) / 86400.0
        for (i in 0 until 8) {
            val diff = normalizeDeg180(target - apparentSolarLongitude(jd))
            if (abs(diff) < 1e-7) break
            jd += diff / 0.98565 // 太阳平均角速度（度/日），含中心差影响足够收敛
        }
        val jdResult = jd - deltaTSeconds(jdUtToEpochMillis(jd)) / 86400.0
        return jdUtToEpochMillis(jdResult)
    }

    /** Meeus §25：太阳视黄经（度）。jd 为 TT 时标儒略日。 */
    fun apparentSolarLongitude(jdTt: Double): Double {
        val t = (jdTt - 2451545.0) / 36525.0
        val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val centigrade = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(rad(m)) +
            (0.019993 - 0.000101 * t) * sin(rad(2 * m)) +
            0.000289 * sin(rad(3 * m))
        val trueLong = l0 + centigrade
        val omega = 125.04 - 1934.136 * t
        val apparent = trueLong - 0.00569 - 0.00478 * sin(rad(omega))
        return normalizeDeg360(apparent)
    }

    /** Espenak & Meeus 2006 分段 ΔT 多项式（秒）。epochMillis 为 UT。 */
    fun deltaTSeconds(epochMillis: Long): Double {
        val year = decimalYear(epochMillis)
        val t: Double
        return when {
            year < 1920 -> {
                t = year - 1900
                -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t * t * t - 0.000197 * t * t * t * t
            }
            year < 1941 -> {
                t = year - 1920
                21.20 + 0.84493 * t - 0.076100 * t * t + 0.0020936 * t * t * t
            }
            year < 1961 -> {
                t = year - 1950
                29.07 + 0.407 * t - t * t / 233 + t * t * t / 2547
            }
            year < 1986 -> {
                t = year - 1975
                45.45 + 1.067 * t - t * t / 260 - t * t * t / 718
            }
            year < 2005 -> {
                t = year - 2000
                63.86 + 0.3345 * t - 0.060374 * t * t + 0.0017275 * t * t * t +
                    0.000651814 * t * t * t * t + 0.00002373599 * t * t * t * t * t
            }
            year < 2050 -> {
                t = year - 2000
                62.92 + 0.32217 * t + 0.005589 * t * t
            }
            year < 2150 -> {
                t = (year - 1820) / 100
                -20 + 32 * t * t - 0.5628 * (2150 - year)
            }
            else -> {
                t = (year - 1820) / 100
                -20 + 32 * t * t
            }
        }
    }

    private fun decimalYear(epochMillis: Long): Double {
        val jd = epochMillisToJdUt(epochMillis)
        val z = floor(jd + 0.5)
        val f = jd + 0.5 - z
        var a = z
        if (z >= 2299161) {
            val alpha = floor((z - 1867216.25) / 36524.25)
            a = z + 1 + alpha - floor(alpha / 4)
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25)
        val d = floor(365.25 * c)
        val e = floor((b - d) / 30.6001)
        val day = b - d - floor(30.6001 * e) + f
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return year + (floor(month) - 0.5) / 12.0
    }

    private fun normalizeDeg360(deg: Double): Double {
        val wrapped = deg % 360.0
        return if (wrapped < 0) wrapped + 360.0 else wrapped
    }

    /** 归一化到 [-180, 180) 的角差。 */
    private fun normalizeDeg180(deg: Double): Double {
        var d = deg % 360.0
        if (d >= 180.0) d -= 360.0
        if (d < -180.0) d += 360.0
        return d
    }

    private fun rad(deg: Double): Double = Math.toRadians(deg)
}
