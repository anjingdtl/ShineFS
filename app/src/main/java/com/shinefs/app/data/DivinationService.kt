package com.shinefs.app.data

import com.shinefs.core.yijing.divination.DirectionCastInput
import com.shinefs.core.yijing.divination.DirectionDivinationRule
import com.shinefs.core.yijing.divination.FixtureDirectionRule
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** 卦例仓储接口：V1 先用内存实现，Cycle 07 换 Room 实现并保持接口不变。 */
interface CaseRepository {
    fun save(case: DivinationCase)
    fun all(): List<DivinationCase>
    fun byId(id: String): DivinationCase?
    fun byHouseAudit(auditId: String): List<DivinationCase>
    fun update(case: DivinationCase)
    fun delete(id: String)
}

class InMemoryCaseRepository : CaseRepository {
    private val store = ConcurrentHashMap<String, DivinationCase>()
    override fun save(case: DivinationCase) {
        store[case.id] = case
    }

    override fun all(): List<DivinationCase> = store.values.sortedByDescending { it.timestamp }
    override fun byId(id: String): DivinationCase? = store[id]
    override fun byHouseAudit(auditId: String): List<DivinationCase> =
        store.values.filter { it.houseAuditId == auditId }.sortedBy { it.timestamp }

    override fun update(case: DivinationCase) {
        store[case.id] = case
    }

    override fun delete(id: String) {
        store.remove(id)
    }
}

/** 定盘锁定的罗盘读数（分层边界：传感器层产物，起卦输入）。 */
data class LockedReading(
    val azimuth: Float,
    val facingMountain: String,
    val sittingMountain: String,
    val facingTrigram: String,
    val facingElement: String,
    val timestamp: Long,
    val stability: String,
    val accuracy: String,
)

/**
 * 起卦编排：规则引擎可替换（D-01~D-05 未拍板期间使用明确标记的临时规则）。
 * 同一输入（方位+时刻）必得同一输出——确定性由 core:yijing 保证。
 */
class DivinationService(
    private val repository: CaseRepository,
    private val directionRule: DirectionDivinationRule = FixtureDirectionRule(),
) {
    fun castWithDirection(
        reading: LockedReading,
        scene: SceneType,
        houseAuditId: String? = null,
    ): DivinationCase {
        val outcome = directionRule.cast(
            DirectionCastInput(azimuth = reading.azimuth, epochMillis = reading.timestamp),
        )
        val original = outcome.originalHexagram
        val changed = outcome.changedHexagram
        val case = DivinationCase(
            id = UUID.randomUUID().toString(),
            timestamp = reading.timestamp,
            sceneId = scene.id,
            sceneName = scene.name,
            azimuth = reading.azimuth,
            facingMountain = reading.facingMountain,
            sittingMountain = reading.sittingMountain,
            facingTrigram = reading.facingTrigram,
            facingElement = reading.facingElement,
            stability = reading.stability,
            ruleId = outcome.ruleId,
            ruleDisplayName = directionRule.displayName,
            rulesVersion = DivinationCase.RULES_VERSION,
            interpretationVersion = DivinationCase.INTERPRETATION_VERSION,
            upperTrigram = outcome.upperTrigram.chineseName,
            lowerTrigram = outcome.lowerTrigram.chineseName,
            originalHexagramOrder = original.kingWenOrder,
            originalHexagramName = original.chineseName,
            changingLine = outcome.changingLine,
            changedHexagramOrder = changed.kingWenOrder,
            changedHexagramName = changed.chineseName,
            houseAuditId = houseAuditId,
        )
        repository.save(case)
        return case
    }

    fun ruleExplain(): String = buildString {
        appendLine("规则标识：${directionRule.ruleId}（${directionRule.displayName}）")
        appendLine("规则版本：${DivinationCase.RULES_VERSION}")
        appendLine("上卦（正式）：向首所属后天八卦（产品方案 §4.2 模式 A）")
        appendLine("下卦（临时口径）：公历 年+月+日+时 之和 mod 8，余 0 记 8，取先天卦数")
        appendLine("动爻（临时口径）：公历 年+月+日+时+分 之和 mod 6，余 0 记 6")
        appendLine("变卦：仅翻转动爻后重新拆卦映射（确定性，测试覆盖 64×6）")
        appendLine("⚠️ 临时口径仅供联调，正式取数口径待决策 D-01/D-04/D-05（见 DOCS/YIJING_RULES.md）")
    }
}
