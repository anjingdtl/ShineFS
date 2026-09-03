package com.shinefs.app.interpret

import com.shinefs.app.data.DivinationCase
import com.shinefs.app.data.Scenes
import com.shinefs.core.yijing.data.Hexagrams
import com.shinefs.core.yijing.model.Trigram

/**
 * 确定性象义/空间解读引擎：全部文案由规则数据（core:yijing）与场景表模板拼装，
 * 不含随机性、不调用 AI、不做流派吉凶断言（五行生克推断列为待决策 D-10，未实现）。
 */
class RuleBasedInterpreter {

    /** 象义解析：只陈述卦象结构事实（上下卦、象、五行、亲属、爻动事实）。 */
    fun symbolism(case: DivinationCase): String {
        val original = Hexagrams.byKingWenOrder(case.originalHexagramOrder)
        val changed = Hexagrams.byKingWenOrder(case.changedHexagramOrder)
        val upper = original.upperTrigram
        val lower = original.lowerTrigram
        return buildString {
            appendLine("本卦《${original.chineseName}》（第${original.kingWenOrder}卦 ${original.symbol}）由下卦${lower.chineseName}与上卦${upper.chineseName}组成。")
            appendLine("下卦${lower.chineseName}，象${lower.natureImage}，五行属${lower.element}，家风${lower.familyRole}；上卦${upper.chineseName}，象${upper.natureImage}，五行属${upper.element}，家风${upper.familyRole}。")
            appendLine("第${case.changingLine}爻（${lineName(case.changingLine)}）发动，阴阳互转，本卦《${original.chineseName}》过渡为变卦《${changed.chineseName}》（第${changed.kingWenOrder}卦）。")
            append("爻动表示事态在此位次出现变化契机；结合所占场景「${case.sceneName}」与方位读数综合观察。")
        }
    }

    /** 空间解读：方位五行特质 + 场景观察建议（中性表述，不断吉凶）。 */
    fun spatial(case: DivinationCase): String {
        val element = case.facingElement ?: "—"
        val quality = elementQuality(element)
        val sceneAdvice = sceneAdvice(case.sceneId)
        return buildString {
            appendLine("定盘方位：向${case.facingMountain}（${case.facingTrigram}卦，五行属$element）坐${case.sittingMountain}。")
            appendLine("向方五行特质：$quality")
            appendLine("场景「${case.sceneName}」观察建议：$sceneAdvice")
            append("以上为方位与场景的结构化描述；V1.0 不做飞星、兼向等流派推断（详见规则来源与版本）。")
        }
    }

    /** 宜忌与注意：通用环境建议与免责说明，不构成决策依据。 */
    fun advisories(): String = buildString {
        appendLine("宜：保持该场景整洁通明，动线顺畅；对应五行特质之物象可作陈设参考。")
        appendLine("忌：向首方向避免长期堆压杂物、正对尖锐冲射之物；磁场干扰未排除时不宜定盘。")
        append("注意：本节为传统数术的通则性提示，仅供参考，不构成医疗、投资、法律等专业建议。")
    }

    fun lineName(line: Int): String = when (line) {
        1 -> "初爻"; 2 -> "二爻"; 3 -> "三爻"; 4 -> "四爻"; 5 -> "五爻"; 6 -> "上爻"; else -> "第${line}爻"
    }

    private fun elementQuality(element: String): String = when (element) {
        "金" -> "收敛、肃降、主决断；象秋，宜简洁有序。"
        "木" -> "生发、舒展、主进取；象春，宜生机通畅。"
        "水" -> "润下、流转、主智慧；象冬，宜静定藏养。"
        "火" -> "炎上、光明、主礼；象夏，宜明亮而忌躁。"
        "土" -> "承载、中正、主信；象四季，宜安稳厚实。"
        else -> "五行信息缺失。"
    }

    private fun sceneAdvice(sceneId: String): String = when (sceneId) {
        Scenes.house[0].id -> "大门为宅之纳气口，注意门槛内外整洁、开门见序。"
        Scenes.house[1].id -> "客厅为聚气会客之所，主位宜实、采光宜匀。"
        Scenes.house[2].id -> "主卧以安稳为要，床头宜有靠、避免正对冲射。"
        Scenes.house[3].id -> "书房主文昌，坐向后宜有依靠，桌面宜清。"
        Scenes.house[4].id -> "灶位主养，忌正对水口与门冲，保持洁净。"
        Scenes.house[5].id -> "阳台为宅之明堂外延，宜开阔明亮、忌堆积。"
        Scenes.house[6].id -> "办公位宜坐实向虚，背后有靠，前方开阔。"
        Scenes.house[7].id -> "商铺入口主纳客，宜敞亮迎人、动线分明。"
        else -> "通用观察：测点宜稳固，读数宜在无磁扰环境复核。"
    }
}
