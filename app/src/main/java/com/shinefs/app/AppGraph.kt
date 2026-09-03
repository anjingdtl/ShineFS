package com.shinefs.app

import com.shinefs.app.ai.AiInterpreter
import com.shinefs.app.ai.OfflineAiInterpreter
import com.shinefs.app.data.CaseRepository
import com.shinefs.app.data.DivinationService
import com.shinefs.app.data.InMemoryCaseRepository
import com.shinefs.app.interpret.RuleBasedInterpreter
import com.shinefs.core.yijing.text.ClassicTextRepository
import com.shinefs.core.yijing.text.FixtureClassicTexts

/** 极简服务定位（V1 规模下足够；Cycle 07 仓储换 Room 实现时仅改此处）。 */
object AppGraph {
    val caseRepository: CaseRepository = InMemoryCaseRepository()
    val divinationService: DivinationService = DivinationService(caseRepository)
    val classicTexts: ClassicTextRepository = FixtureClassicTexts()
    val ruleInterpreter = RuleBasedInterpreter()
    val aiInterpreter: AiInterpreter = OfflineAiInterpreter()
}
