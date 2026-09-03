package com.shinefs.app.data

import com.shinefs.app.data.db.DivinationCaseDao
import com.shinefs.app.data.db.toDomain
import com.shinefs.app.data.db.toEntity

/**
 * Room 仓储实现（Cycle 07 起替换内存实现）。
 * DAO 为阻塞式：所有调用必须在 IO 线程（UI 层统一 withContext(Dispatchers.IO)）。
 */
class RoomCaseRepository(private val dao: DivinationCaseDao) : CaseRepository {
    override fun save(case: DivinationCase) = dao.upsert(case.toEntity())
    override fun all(): List<DivinationCase> = dao.all().map { it.toDomain() }
    override fun byId(id: String): DivinationCase? = dao.byId(id)?.toDomain()
    override fun byHouseAudit(auditId: String): List<DivinationCase> =
        dao.byHouseAudit(auditId).map { it.toDomain() }

    override fun update(case: DivinationCase) = dao.upsert(case.toEntity())
    override fun delete(id: String) = dao.delete(id)
}
