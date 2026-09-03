package com.shinefs.app.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

/** 卦例表：字段与领域模型 DivinationCase 一一对应（规则/解释版本随例留存）。 */
@Entity(tableName = "divination_cases")
data class DivinationCaseEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val sceneId: String,
    val sceneName: String,
    val azimuth: Float?,
    val facingMountain: String?,
    val sittingMountain: String?,
    val facingTrigram: String?,
    val facingElement: String?,
    val stability: String?,
    val ruleId: String,
    val ruleDisplayName: String,
    val rulesVersion: String,
    val interpretationVersion: String,
    val upperTrigram: String,
    val lowerTrigram: String,
    val originalHexagramOrder: Int,
    val originalHexagramName: String,
    val changingLine: Int,
    val changedHexagramOrder: Int,
    val changedHexagramName: String,
    val houseAuditId: String?,
    val favorite: Boolean,
    val note: String?,
)

@Dao
interface DivinationCaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: DivinationCaseEntity)

    @Query("SELECT * FROM divination_cases ORDER BY timestamp DESC")
    fun all(): List<DivinationCaseEntity>

    @Query("SELECT * FROM divination_cases WHERE id = :id LIMIT 1")
    fun byId(id: String): DivinationCaseEntity?

    @Query("SELECT * FROM divination_cases WHERE houseAuditId = :auditId ORDER BY timestamp ASC")
    fun byHouseAudit(auditId: String): List<DivinationCaseEntity>

    @Query("DELETE FROM divination_cases WHERE id = :id")
    fun delete(id: String)
}

@Database(entities = [DivinationCaseEntity::class], version = 1, exportSchema = true)
abstract class ShineDatabase : RoomDatabase() {
    abstract fun divinationCaseDao(): DivinationCaseDao
}
