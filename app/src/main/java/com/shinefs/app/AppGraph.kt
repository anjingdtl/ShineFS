package com.shinefs.app

import android.content.Context
import androidx.room.Room
import com.shinefs.app.data.CaseRepository
import com.shinefs.app.data.DivinationServiceV2
import com.shinefs.app.data.RoomCaseRepository
import com.shinefs.app.data.db.ShineDatabase
import com.shinefs.core.calendar.YijingTimeResolver
import com.shinefs.core.calendar.provider.TableChineseCalendarProvider
import com.shinefs.core.classics.CanonicalCorpus
import com.shinefs.core.classics.ClassicCorpus
import com.shinefs.core.divination.classimage.ClassImageTable
import com.shinefs.core.divination.rule.MeihuaPostHeavenObjectDirectionRuleV1
import com.shinefs.core.divination.rule.MeihuaTimeDivinationRuleV1
import com.shinefs.core.interpretation.AdvisoryComposer
import java.util.TimeZone

/**
 * V2 服务定位（0 AI / 0 生产 Fixture / 0 网络 / 0 随机）：
 * - [divinationService]：正式起卦编排（梅花时间法 / 时空合参）；
 * - [timeResolver]：当前时空上下文（罗盘页时间盘、时间起卦页共用）；
 * - [classicCorpus]：周易原典（版本化 + checksum）；
 * - [composer]：本地九段报告解释器。
 * [init] 必须在 Application/Activity 启动时调用一次（建库 + 迁移 + legacy 标记）。
 * 线程约定：caseRepository 的方法一律在 Dispatchers.IO 调用。
 */
object AppGraph {
    lateinit var caseRepository: CaseRepository
        private set

    val timeZone: TimeZone = TimeZone.getTimeZone("Asia/Shanghai")

    val timeResolver: YijingTimeResolver by lazy {
        YijingTimeResolver(TableChineseCalendarProvider())
    }

    private var serviceV2: DivinationServiceV2? = null

    val divinationService: DivinationServiceV2
        get() = serviceV2 ?: DivinationServiceV2(
            caseRepository, composer = composer, timeZone = timeZone,
            dayBoundaryPolicy = dayBoundaryPolicy(),
        ).also { serviceV2 = it }

    val classicCorpus: ClassicCorpus = CanonicalCorpus

    val composer: AdvisoryComposer by lazy { AdvisoryComposer(classicCorpus) }

    val meihuaTimeRule: MeihuaTimeDivinationRuleV1 by lazy { MeihuaTimeDivinationRuleV1() }

    val postHeavenRule: MeihuaPostHeavenObjectDirectionRuleV1 by lazy {
        MeihuaPostHeavenObjectDirectionRuleV1()
    }

    val classImageTable: ClassImageTable get() = ClassImageTable

    /** 当前宅居测局会话（跨导航存活；随卦例持久化可回溯）。 */
    private var currentHouseAuditId: String? = null

    fun obtainHouseAuditId(): String =
        currentHouseAuditId ?: java.util.UUID.randomUUID().toString().also { currentHouseAuditId = it }

    fun newHouseAuditId(): String =
        java.util.UUID.randomUUID().toString().also { currentHouseAuditId = it }

    private lateinit var database: ShineDatabase
    private lateinit var prefs: android.content.SharedPreferences

    /** 日界策略（设置页可切换；切换后须重新起卦并随例留存规则版本）。 */
    fun dayBoundaryPolicy(): com.shinefs.core.calendar.model.DayBoundaryPolicy {
        if (!::prefs.isInitialized) return com.shinefs.core.calendar.model.DayBoundaryPolicy.CIVIL_MIDNIGHT
        return if (prefs.getString("day_boundary", "CIVIL_MIDNIGHT") == "ZI_HOUR_START_23") {
            com.shinefs.core.calendar.model.DayBoundaryPolicy.ZI_HOUR_START_23
        } else {
            com.shinefs.core.calendar.model.DayBoundaryPolicy.CIVIL_MIDNIGHT
        }
    }

    fun setDayBoundaryPolicy(policy: com.shinefs.core.calendar.model.DayBoundaryPolicy) {
        prefs.edit().putString("day_boundary", policy.name).apply()
        serviceV2 = null // 策略变更后重建服务（后续起卦按新策略，并随例留存）
    }

    fun init(context: Context) {
        if (::caseRepository.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences("shinefs_settings", Context.MODE_PRIVATE)
        database = Room.databaseBuilder(
            context.applicationContext,
            ShineDatabase::class.java,
            "shinefs.db",
        )
            .addMigrations(ShineDatabase.MIGRATION_1_2)
            .build()
        caseRepository = RoomCaseRepository(database.divinationCaseDao())
        // Cycle 10I：V1 Fixture 卦例统一标记 legacy-fixture（幂等；后台线程避免主线程 DB）。
        // 旧例保留可查看（解读页显示 legacy 横幅），不伪装为 V2 正式结果。
        Thread {
            runCatching { database.divinationCaseDao().markLegacyFixtures() }
        }.start()
    }
}
