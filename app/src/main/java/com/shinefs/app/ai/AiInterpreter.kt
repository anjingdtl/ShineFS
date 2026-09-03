package com.shinefs.app.ai

import com.shinefs.app.data.DivinationCase

/**
 * AI 解读接口抽象（产品方案 §10）。
 *
 * 职责边界：AI 只做白话化与内容组织；输入为结构化卦例，输出不得改写
 * 测量值/本卦/动爻/变卦，不得自创经典文本。默认实现为离线降级
 * （NOT_CONFIGURED），App 在无 AI 时仍展示完整确定性结果。
 */
enum class AiStatus { OK, NOT_CONFIGURED, FAILED }

data class AiInterpretResult(
    val status: AiStatus,
    val plainText: String?,
    val model: String?,
)

interface AiInterpreter {
    val name: String
    suspend fun interpret(case: DivinationCase): AiInterpretResult
}

/** 结构化请求预览（调试/未来接入远端 AI 用），字段对应产品方案 §10.1。 */
fun buildStructuredRequest(case: DivinationCase): String = buildString {
    append("{\n")
    append("  \"scene\": \"${case.sceneName}\",\n")
    append("  \"azimuth\": ${case.azimuth},\n")
    append("  \"facingMountain\": \"${case.facingMountain}\",\n")
    append("  \"sittingMountain\": \"${case.sittingMountain}\",\n")
    append("  \"trigram\": \"${case.facingTrigram}\",\n")
    append("  \"element\": \"${case.facingElement}\",\n")
    append("  \"originalHexagram\": \"${case.originalHexagramName}\",\n")
    append("  \"changingLine\": ${case.changingLine},\n")
    append("  \"changedHexagram\": \"${case.changedHexagramName}\",\n")
    append("  \"rulesVersion\": \"${case.rulesVersion}\",\n")
    append("  \"interpretationVersion\": \"${case.interpretationVersion}\"\n")
    append("}")
}

/** 默认离线实现：未配置远端 AI 时的降级。 */
class OfflineAiInterpreter : AiInterpreter {
    override val name: String = "离线（未配置 AI）"
    override suspend fun interpret(case: DivinationCase): AiInterpretResult =
        AiInterpretResult(
            status = AiStatus.NOT_CONFIGURED,
            plainText = null,
            model = null,
        )
}
