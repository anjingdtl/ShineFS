package com.shinefs.app.data

import java.util.concurrent.ConcurrentHashMap

/** 卦例仓储接口。 */
interface CaseRepository {
    fun save(case: DivinationCase)
    fun all(): List<DivinationCase>
    fun byId(id: String): DivinationCase?
    fun byHouseAudit(auditId: String): List<DivinationCase>
    fun update(case: DivinationCase)
    fun delete(id: String)
}

/** 内存实现（JVM 测试与开发用；生产用 Room 实现）。 */
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
