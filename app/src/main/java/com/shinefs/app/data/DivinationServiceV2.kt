package com.shinefs.app.data

import com.shinefs.core.calendar.YijingTimeResolver
import com.shinefs.core.calendar.model.DayBoundaryPolicy
import com.shinefs.core.calendar.provider.TableChineseCalendarProvider
import com.shinefs.core.classics.CanonicalCorpus
import com.shinefs.core.divination.context.YijingMomentContext
import com.shinefs.core.divination.context.YijingSpaceContextFactory
import com.shinefs.core.divination.result.DivinationResult
import com.shinefs.core.divination.rule.MeihuaTimeDivinationRuleV1
import com.shinefs.core.divination.rule.TimeCastWithSpatialResponse
import com.shinefs.core.interpretation.AdvisoryComposer
import com.shinefs.core.compass.snapshot.LockedCompassSnapshot
import java.util.TimeZone
import java.util.UUID

/** 定盘锁定的罗盘读数（V2：含精度分离标签）。 */
data class LockedReading(
    val azimuth: Float,
    val facingMountain: String,
    val sittingMountain: String,
    val facingTrigram: String,
    val facingElement: String,
    val timestamp: Long,
    val stability: String,
    val accuracy: String,
    val magneticAccuracy: String = "",
    /** 定盘时设备时区；空间快照与时间演算共用该 zone。 */
    val zoneId: String? = null,
    val localDateTime: String? = null,
    val utcOffsetMinutes: Int? = null,
    /** V2.1 定盘瞬间复制的真实状态；没有该字段的旧调用方只走显式兼容路径。 */
    val snapshot: LockedCompassSnapshot? = null,
)

/**
 * V2 起卦编排（V2.0 方案 §38 主流程）：
 * 正式核心链 = TableChineseCalendarProvider + YijingTimeResolver + MeihuaTime/TimeCastWithSpatialResponse
 * + AdvisoryComposer。**0 生产 Fixture、0 AI、0 网络、0 随机**（id 用 UUID 仅作记录主键，不入演算）。
 */
class DivinationServiceV2(
    private val repository: CaseRepository,
    private val composer: AdvisoryComposer = AdvisoryComposer(),
    /** 测试或历史复算可注入固定时区；生产默认每次读取设备当前时区。 */
    private val timeZone: TimeZone? = null,
    private val dayBoundaryPolicy: DayBoundaryPolicy = DayBoundaryPolicy.CIVIL_MIDNIGHT,
    private val timeZoneProvider: () -> TimeZone = { TimeZone.getDefault() },
) {
    private val timeResolver = YijingTimeResolver(TableChineseCalendarProvider())
    private val spaceRule = TimeCastWithSpatialResponse()
    private val timeRule = MeihuaTimeDivinationRuleV1()

    /** 纯时间起卦（模式 A）。 */
    fun castTime(
        scene: SceneType,
        houseAuditId: String? = null,
        atMillis: Long = System.currentTimeMillis(),
    ): DivinationCase {
        val time = timeResolver.resolve(atMillis, currentTimeZone(), dayBoundaryPolicy)
        val result = timeRule.cast(time)
        return save(result, scene, houseAuditId, DivinationCase.CAST_MODE_TIME, null)
    }

    /** 时空合参起卦（模式 C：时间卦 + 罗盘方应，空间不修改时间卦）。 */
    fun castTimeSpace(
        reading: LockedReading,
        scene: SceneType,
        houseAuditId: String? = null,
    ): DivinationCase {
        val time = timeResolver.resolve(
            reading.timestamp,
            reading.zoneId?.let(TimeZone::getTimeZone) ?: currentTimeZone(),
            dayBoundaryPolicy,
        )
        val space = reading.snapshot?.let(YijingSpaceContextFactory::fromLockedCompassSnapshot)
            ?: YijingSpaceContextFactory.fromLegacyReading(
                rawAzimuth = reading.azimuth,
                smoothedAzimuth = reading.azimuth,
                facingMountain = reading.facingMountain,
                sittingMountain = reading.sittingMountain,
                facingTrigram = reading.facingTrigram,
                stable = reading.stability == "良好",
                orientationAccuracy = reading.accuracy,
                magneticAccuracy = reading.magneticAccuracy,
            )
            ?: error("时空起卦缺少有效的定盘读数")
        val result = spaceRule.cast(YijingMomentContext(time, space, null, reading.timestamp))
        return save(result, scene, houseAuditId, DivinationCase.CAST_MODE_TIME_SPACE, reading)
    }

    private fun save(
        result: DivinationResult,
        scene: SceneType,
        houseAuditId: String?,
        castMode: String,
        reading: LockedReading?,
    ): DivinationCase {
        val report = composer.compose(result)
        val t = result.timeContext
        val sp = result.spaceContext
        val case = DivinationCase(
            id = UUID.randomUUID().toString(),
            timestamp = t.epochMillis,
            sceneId = scene.id,
            sceneName = scene.name,
            azimuth = sp?.smoothedAzimuth ?: reading?.azimuth,
            facingMountain = sp?.facingMountain ?: reading?.facingMountain,
            sittingMountain = sp?.sittingMountain ?: reading?.sittingMountain,
            facingTrigram = sp?.directionTrigram?.chineseName ?: reading?.facingTrigram,
            facingElement = sp?.directionTrigram?.element ?: reading?.facingElement,
            stability = if (sp?.stable == true) "良好" else reading?.stability,
            ruleId = result.rule.ruleId,
            ruleDisplayName = displayRuleName(result.rule.ruleId),
            rulesVersion = DivinationCase.RULES_VERSION,
            interpretationVersion = DivinationCase.INTERPRETATION_VERSION,
            upperTrigram = result.upperTrigram.chineseName,
            lowerTrigram = result.lowerTrigram.chineseName,
            originalHexagramOrder = result.original.kingWenOrder,
            originalHexagramName = result.original.chineseName,
            changingLine = result.changingLine,
            changedHexagramOrder = result.changed.kingWenOrder,
            changedHexagramName = result.changed.chineseName,
            castMode = castMode,
            zoneId = t.zoneId,
            utcOffsetMinutes = t.utcOffsetMinutes,
            localDateTime = t.localDateTime,
            calendarVersion = t.calendarVersion,
            ruleVersion = result.rule.version,
            classicCorpusVersion = CanonicalCorpus.version,
            dayBoundaryPolicy = t.dayBoundaryPolicy.name,
            leapMonthPolicy = t.leapMonthPolicy.name,
            northReference = sp?.northReference?.name,
            rawAzimuth = sp?.rawAzimuth,
            smoothedAzimuth = sp?.smoothedAzimuth,
            snapshotCapturedAt = reading?.snapshot?.capturedAt,
            holdPose = sp?.holdPose?.name ?: reading?.snapshot?.holdPose?.name,
            holdPoseConfidence = sp?.holdPoseConfidence,
            poseStableMillis = sp?.poseStableMillis,
            displayRotation = reading?.snapshot?.displayRotation,
            pitchDeg = sp?.pitchDeg,
            rollDeg = sp?.rollDeg,
            stabilityStdDeg = sp?.stabilityStdDeg,
            orientationAccuracy = sp?.sensorAccuracy?.orientationAccuracy?.name,
            magneticAccuracy = sp?.sensorAccuracy?.magneticAccuracy?.name,
            magneticMagnitudeUt = sp?.magneticMagnitudeUt,
            magneticInterference = sp?.magneticInterference,
            lunarYear = t.lunarYear,
            lunarMonth = t.lunarMonth,
            lunarDay = t.lunarDay,
            leapMonthFlag = t.leapMonth,
            yearBranch = t.yearBranch.chinese,
            hourBranch = t.hourBranch.chinese,
            yearBranchNumber = t.yearBranchNumber,
            lunarMonthNumber = t.lunarMonthNumber,
            lunarDayNumber = t.lunarDayNumber,
            hourBranchNumber = t.hourBranchNumber,
            nuclearHexagramOrder = result.nuclear?.hexagram?.kingWenOrder,
            nuclearHexagramName = result.nuclear?.hexagram?.chineseName,
            tiTrigram = result.tiYong?.ti?.chineseName,
            yongTrigram = result.tiYong?.yong?.chineseName,
            elementRelation = result.elementRelation?.name,
            seasonalQi = result.seasonalQi?.let { "${it.season.chinese}·${it.dominantElement.chinese}" },
            solarTerm = t.solarTerm?.term?.chinese,
            calculationTrace = result.trace.render(),
            reportText = report.render(),
            legacyFixture = false,
            houseAuditId = houseAuditId,
        )
        repository.save(case)
        return case
    }

    private fun currentTimeZone(): TimeZone = timeZone ?: timeZoneProvider()

    /** 按原起卦四数离线复算（方案 §28：旧卦可按原规则版本复算）。 */
    fun recomputeTrace(case: DivinationCase): String? {
        val y = case.yearBranchNumber ?: return null
        val m = case.lunarMonthNumber ?: return null
        val d = case.lunarDayNumber ?: return null
        val h = case.hourBranchNumber ?: return null
        val base = y + m + d
        val upper = com.shinefs.core.yijing.model.Trigram.fromXiantianNumber(((base - 1) % 8) + 1)
        val lower = com.shinefs.core.yijing.model.Trigram.fromXiantianNumber(((base + h - 1) % 8) + 1)
        val line = ((base + h - 1) % 6) + 1
        val original = com.shinefs.core.yijing.rules.HexagramOps.fromTrigrams(lower, upper)
        val changed = com.shinefs.core.yijing.rules.HexagramOps.withChangingLine(original, line)
        val expect = "核对结果：${original.chineseName}之${changed.chineseName}（第${line}爻动）"
        val match = original.chineseName == case.originalHexagramName &&
            changed.chineseName == case.changedHexagramName && line == case.changingLine
        return expect + if (match) " ✓ 与原记录一致" else " ✗ 与原记录不一致（请重新核对起卦依据）"
    }

    companion object {
        fun displayRuleName(ruleId: String): String = when (ruleId) {
            "meihua-time-v1" -> "梅花易数 · 年月日时起卦"
            "time-cast-with-spatial-response-v1" -> "时空合参 · 时间卦＋罗盘方应"
            "meihua-postheaven-v1" -> "梅花易数 · 后天端法（物象方位）"
            else -> "传统起卦方法"
        }
    }
}
