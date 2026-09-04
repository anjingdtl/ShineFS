package com.shinefs.app.data.db

import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** 卦例表：字段与领域模型 DivinationCase 一一对应（V2.0 方案 §28 全字段留存）。 */
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
    // ---- V2 新增（带默认值以支持 v1→v2 迁移）----
    @ColumnInfo(defaultValue = "TIME") val castMode: String,
    @ColumnInfo(defaultValue = "Asia/Shanghai") val zoneId: String,
    @ColumnInfo(defaultValue = "0") val utcOffsetMinutes: Int,
    @ColumnInfo(defaultValue = "") val localDateTime: String,
    @ColumnInfo(defaultValue = "") val calendarVersion: String,
    @ColumnInfo(defaultValue = "") val ruleVersion: String,
    @ColumnInfo(defaultValue = "") val classicCorpusVersion: String,
    @ColumnInfo(defaultValue = "CIVIL_MIDNIGHT") val dayBoundaryPolicy: String,
    @ColumnInfo(defaultValue = "SAME_MONTH_NUMBER") val leapMonthPolicy: String,
    @ColumnInfo(defaultValue = "MAGNETIC") val northReference: String,
    val rawAzimuth: Float?,
    val smoothedAzimuth: Float?,
    @ColumnInfo(defaultValue = "0") val lunarYear: Int,
    @ColumnInfo(defaultValue = "0") val lunarMonth: Int,
    @ColumnInfo(defaultValue = "0") val lunarDay: Int,
    @ColumnInfo(defaultValue = "0") val leapMonthFlag: Boolean,
    @ColumnInfo(defaultValue = "") val yearBranch: String,
    @ColumnInfo(defaultValue = "") val hourBranch: String,
    @ColumnInfo(defaultValue = "0") val yearBranchNumber: Int,
    @ColumnInfo(defaultValue = "0") val lunarMonthNumber: Int,
    @ColumnInfo(defaultValue = "0") val lunarDayNumber: Int,
    @ColumnInfo(defaultValue = "0") val hourBranchNumber: Int,
    @ColumnInfo(defaultValue = "0") val nuclearHexagramOrder: Int,
    @ColumnInfo(defaultValue = "") val nuclearHexagramName: String,
    @ColumnInfo(defaultValue = "") val tiTrigram: String,
    @ColumnInfo(defaultValue = "") val yongTrigram: String,
    @ColumnInfo(defaultValue = "") val elementRelation: String,
    @ColumnInfo(defaultValue = "") val seasonalQi: String,
    @ColumnInfo(defaultValue = "") val solarTerm: String,
    val calculationTrace: String?,
    val reportText: String?,
    @ColumnInfo(defaultValue = "0") val legacyFixture: Boolean,
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

    @Query("UPDATE divination_cases SET legacyFixture = 1 WHERE legacyFixture = 0 AND rulesVersion != 'rules-v2.0'")
    fun markLegacyFixtures()
}

@Database(entities = [DivinationCaseEntity::class], version = 3, exportSchema = true)
abstract class ShineDatabase : RoomDatabase() {
    abstract fun divinationCaseDao(): DivinationCaseDao

    companion object {
        /** v1 → v2：新增 V2 全字段（带默认值）；旧记录由 10I 标记 legacyFixture。 */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN castMode TEXT NOT NULL DEFAULT 'TIME'")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN zoneId TEXT NOT NULL DEFAULT 'Asia/Shanghai'")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN calendarVersion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN ruleVersion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN classicCorpusVersion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN dayBoundaryPolicy TEXT NOT NULL DEFAULT 'CIVIL_MIDNIGHT'")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN leapMonthPolicy TEXT NOT NULL DEFAULT 'SAME_MONTH_NUMBER'")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN northReference TEXT NOT NULL DEFAULT 'MAGNETIC'")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN rawAzimuth REAL")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN smoothedAzimuth REAL")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN lunarYear INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN lunarMonth INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN lunarDay INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN leapMonthFlag INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN yearBranch TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN hourBranch TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN yearBranchNumber INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN lunarMonthNumber INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN lunarDayNumber INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN hourBranchNumber INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN nuclearHexagramOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN nuclearHexagramName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN tiTrigram TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN yongTrigram TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN elementRelation TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN seasonalQi TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN solarTerm TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN calculationTrace TEXT")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN reportText TEXT")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN legacyFixture INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v2 → v3：保存设备时区下的 offset 与本地日期时间，便于历史复算核对。 */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN utcOffsetMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE divination_cases ADD COLUMN localDateTime TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
