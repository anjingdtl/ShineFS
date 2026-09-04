package com.shinefs.core.classics

import com.shinefs.core.classics.data.CanonicalTextsData
import java.security.MessageDigest

/** 原典核验等级；只有双来源逐条校勘完成后才允许进入 DUAL_SOURCE_FULL。 */
enum class CorpusVerificationStatus(val label: String, val evidence: String) {
    ELECTRONIC_STRUCTURE_VERIFIED(
        label = "电子底本已校验",
        evidence = "结构完整性、checksum、电子底本核对与代码锚点抽查已完成；独立第二来源全量校勘未完成",
    ),
    DUAL_SOURCE_PARTIAL(
        label = "双来源部分校勘",
        evidence = "第二独立来源已完成部分逐卦/逐爻比对，异文已登记",
    ),
    DUAL_SOURCE_FULL(
        label = "双来源全量校勘",
        evidence = "第二独立来源已完成逐卦/逐爻比对，异文已登记",
    ),
}

/**
 * 周易原典单爻文本（V2.0 方案 §17）：[line] 1..6 自下而上；[smallImage] 为该爻小象。
 */
data class CanonicalLineText(
    val line: Int,
    val text: String,
    val smallImage: String?,
)

/**
 * 周易原典单卦文本（V2.0 方案 §17）。
 *
 * 数据治理（DOCS/YIJING_RULES.md §10）：
 * - [sourceId]/[sourceEdition] 登记电子底本来源（S-AE2 维基文库通行本系统，构建管线见 edition/）；
 * - [verified] == true 仅表示通过结构完整性校验、checksum、电子底本核对与代码锚点抽查，
 *   不宣称存在独立古籍第二文本源；异文仍以 [textualVariants] 透明保留；
 * - [checksum] 为该卦全部字段的 SHA-256（[CanonicalCorpus.computeChecksum] 复算口径）；
 * - 禁止由 LLM 生成原典并标记已核定；[specialUseText] 仅乾（用九）/坤（用六）非空。
 */
data class CanonicalHexagramText(
    val kingWenOrder: Int,
    val name: String,
    val judgment: String,
    val tuan: String?,
    val greatImage: String?,
    val lines: List<CanonicalLineText>,
    val specialUseText: String?,
    val specialUseSmallImage: String?,
    val textualVariants: List<String>,
    val sourceEdition: String,
    val sourceId: String,
    val verified: Boolean,
    val checksum: String,
)

/**
 * 原典仓储（V2.0 方案 §17）：版本化、checksum 可复算；Room 只存用户卦例，不与原典混库。
 */
interface ClassicCorpus {
    val version: String
    val edition: String
    val verificationStatus: CorpusVerificationStatus
    val corpusChecksum: String
    val all: List<CanonicalHexagramText>

    fun byKingWenOrder(order: Int): CanonicalHexagramText?
}

object CanonicalCorpus : ClassicCorpus {
    override val version: String get() = CanonicalTextsData.VERSION
    override val edition: String get() = CanonicalTextsData.EDITION
    override val verificationStatus: CorpusVerificationStatus =
        CorpusVerificationStatus.ELECTRONIC_STRUCTURE_VERIFIED
    override val corpusChecksum: String get() = CanonicalTextsData.CORPUS_CHECKSUM
    override val all: List<CanonicalHexagramText> get() = CanonicalTextsData.all

    override fun byKingWenOrder(order: Int): CanonicalHexagramText? =
        all.firstOrNull { it.kingWenOrder == order }

    /** 与构建管线一致的逐卦 SHA-256 复算口径。 */
    fun computeChecksum(e: CanonicalHexagramText): String {
        val canon = buildString {
            append(e.kingWenOrder).append('|').append(e.name).append('|').append(e.judgment).append('|')
            append(e.tuan ?: "").append('|').append(e.greatImage ?: "").append('|')
            e.lines.forEach { append(it.line).append(':').append(it.text).append(':').append(it.smallImage ?: "").append('|') }
            append(e.specialUseText ?: "").append('|').append(e.specialUseSmallImage ?: "")
        }
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(canon.toByteArray(Charsets.UTF_8))
            .joinToString("") { String.format(java.util.Locale.ROOT, "%02x", it) }
    }
}
