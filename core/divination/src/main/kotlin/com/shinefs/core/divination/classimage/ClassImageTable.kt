package com.shinefs.core.divination.classimage

import com.shinefs.core.divination.manifest.RuleManifest
import com.shinefs.core.divination.manifest.RuleStatus
import com.shinefs.core.divination.manifest.SourceRef
import com.shinefs.core.divination.manifest.RuleSystem
import com.shinefs.core.yijing.model.Trigram

/**
 * 八卦类象表（`meihua-classimage-v1`，A 级，S-B04）。
 *
 * 仅收《说卦传》明文条目；禁止无文献依据的现代场景配卦（TD-V2-07）。
 * 说卦第九章（人象/动物象）：乾为父/为马、坤为母/为牛、震为长男/为龙、
 * 巽为长女/为鸡、坎为中男/为豕、离为中女/为雉、艮为少男/为狗、兑为少女/为羊。
 * "老人配乾"承《梅花易数》端法古例（老人有忧色占以乾为老父取象），一并登记。
 */
data class ClassImage(
    val id: String,
    val label: String,
    val trigram: Trigram,
    val shuoguaCitation: String,
)

object ClassImageTable {

    const val VERSION = "meihua-classimage-v1"

    val all: List<ClassImage> = listOf(
        ClassImage("qian-father", "父亲", Trigram.QIAN, "《说卦》：乾为父"),
        ClassImage("qian-elder", "老人", Trigram.QIAN, "《梅花易数》老人有忧色占：以老人属乾（老父）为上卦"),
        ClassImage("qian-horse", "马", Trigram.QIAN, "《说卦》：乾为马"),
        ClassImage("kun-mother", "母亲", Trigram.KUN, "《说卦》：坤为母"),
        ClassImage("kun-cow", "牛", Trigram.KUN, "《说卦》：坤为牛"),
        ClassImage("zhen-eldest-son", "长男", Trigram.ZHEN, "《说卦》：震为长男"),
        ClassImage("zhen-dragon", "龙", Trigram.ZHEN, "《说卦》：震为龙"),
        ClassImage("xun-eldest-daughter", "长女", Trigram.XUN, "《说卦》：巽为长女"),
        ClassImage("xun-rooster", "鸡", Trigram.XUN, "《说卦》：巽为鸡"),
        ClassImage("kan-middle-son", "中男", Trigram.KAN, "《说卦》：坎为中男"),
        ClassImage("kan-pig", "豕", Trigram.KAN, "《说卦》：坎为豕"),
        ClassImage("li-middle-daughter", "中女", Trigram.LI, "《说卦》：离为中女"),
        ClassImage("li-pheasant", "雉", Trigram.LI, "《说卦》：离为雉"),
        ClassImage("gen-youngest-son", "少男", Trigram.GEN, "《说卦》：艮为少男"),
        ClassImage("gen-teenager", "少年", Trigram.GEN, "《梅花易数》少年有喜色占：以少年属艮（少男）为上卦"),
        ClassImage("gen-dog", "狗", Trigram.GEN, "《说卦》：艮为狗"),
        ClassImage("dui-youngest-daughter", "少女", Trigram.DUI, "《说卦》：兑为少女"),
        ClassImage("dui-sheep", "羊", Trigram.DUI, "《说卦》：兑为羊"),
    )

    fun byId(id: String): ClassImage =
        all.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("unknown class image id: $id")

    val manifest: RuleManifest = RuleManifest(
        ruleId = "meihua-classimage-v1",
        version = "1",
        system = RuleSystem.YIJING_CLASSIC,
        sourceRefs = listOf(
            SourceRef("S-B04", "《说卦传》八卦象义"),
            SourceRef("S-B03", "《梅花易数》端法后天起卦古例"),
        ),
        assumptions = listOf("仅收说卦明文与梅花古例条目，不扩充现代配卦（TD-V2-07）"),
        status = RuleStatus.VERIFIED,
    )
}
