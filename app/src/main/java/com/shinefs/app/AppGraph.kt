package com.shinefs.app

import com.shinefs.app.data.CaseRepository
import com.shinefs.app.data.DivinationService
import com.shinefs.app.data.InMemoryCaseRepository

/** 极简服务定位（V1 规模下足够；Cycle 07 仓储换 Room 实现时仅改此处）。 */
object AppGraph {
    val caseRepository: CaseRepository = InMemoryCaseRepository()
    val divinationService: DivinationService = DivinationService(caseRepository)
}
