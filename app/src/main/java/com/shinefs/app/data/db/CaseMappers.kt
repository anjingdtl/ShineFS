package com.shinefs.app.data.db

import com.shinefs.app.data.DivinationCase

fun DivinationCase.toEntity() = DivinationCaseEntity(
    id = id, timestamp = timestamp, sceneId = sceneId, sceneName = sceneName,
    azimuth = azimuth, facingMountain = facingMountain, sittingMountain = sittingMountain,
    facingTrigram = facingTrigram, facingElement = facingElement, stability = stability,
    ruleId = ruleId, ruleDisplayName = ruleDisplayName,
    rulesVersion = rulesVersion, interpretationVersion = interpretationVersion,
    upperTrigram = upperTrigram, lowerTrigram = lowerTrigram,
    originalHexagramOrder = originalHexagramOrder, originalHexagramName = originalHexagramName,
    changingLine = changingLine, changedHexagramOrder = changedHexagramOrder,
    changedHexagramName = changedHexagramName, houseAuditId = houseAuditId,
    favorite = favorite, note = note,
)

fun DivinationCaseEntity.toDomain() = DivinationCase(
    id = id, timestamp = timestamp, sceneId = sceneId, sceneName = sceneName,
    azimuth = azimuth, facingMountain = facingMountain, sittingMountain = sittingMountain,
    facingTrigram = facingTrigram, facingElement = facingElement, stability = stability,
    ruleId = ruleId, ruleDisplayName = ruleDisplayName,
    rulesVersion = rulesVersion, interpretationVersion = interpretationVersion,
    upperTrigram = upperTrigram, lowerTrigram = lowerTrigram,
    originalHexagramOrder = originalHexagramOrder, originalHexagramName = originalHexagramName,
    changingLine = changingLine, changedHexagramOrder = changedHexagramOrder,
    changedHexagramName = changedHexagramName, houseAuditId = houseAuditId,
    favorite = favorite, note = note,
)
