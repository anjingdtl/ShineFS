package com.shinefs.app

import android.content.Context
import androidx.room.Room
import com.shinefs.app.ai.AiInterpreter
import com.shinefs.app.ai.OfflineAiInterpreter
import com.shinefs.app.data.CaseRepository
import com.shinefs.app.data.DivinationService
import com.shinefs.app.data.RoomCaseRepository
import com.shinefs.app.data.db.ShineDatabase
import com.shinefs.app.interpret.RuleBasedInterpreter
import com.shinefs.core.yijing.text.ClassicTextRepository
import com.shinefs.core.yijing.text.FixtureClassicTexts

/**
 * 极简服务定位。[init] 必须在 Application/Activity 启动时调用一次（建库）。
 * 线程约定：caseRepository 的方法一律在 Dispatchers.IO 调用。
 */
object AppGraph {
    lateinit var caseRepository: CaseRepository
        private set
    val divinationService: DivinationService by lazy { DivinationService(caseRepository) }
    val classicTexts: ClassicTextRepository = FixtureClassicTexts()
    val ruleInterpreter = RuleBasedInterpreter()
    val aiInterpreter: AiInterpreter = OfflineAiInterpreter()

    /** 当前宅居测局会话（跨导航存活；随卦例持久化可回溯）。 */
    private var currentHouseAuditId: String? = null

    fun obtainHouseAuditId(): String =
        currentHouseAuditId ?: java.util.UUID.randomUUID().toString().also { currentHouseAuditId = it }

    fun newHouseAuditId(): String =
        java.util.UUID.randomUUID().toString().also { currentHouseAuditId = it }

    fun init(context: Context) {
        if (::caseRepository.isInitialized) return
        val db = Room.databaseBuilder(
            context.applicationContext,
            ShineDatabase::class.java,
            "shinefs.db",
        ).build()
        caseRepository = RoomCaseRepository(db.divinationCaseDao())
    }
}
