package com.shinefs.core.yijing.text

/**
 * ⚠️ 临时联调原典数据（fixture，verified=false）。
 *
 * 仅收录 6 个示例卦的卦辞与象辞用于页面联调；爻辞一律未录入。
 * **不得**将本对象内容当作核定原典使用；正式底本与录入流程待决策 D-09。
 * 收录内容以通行本常见表述为准，仍需人工逐字核对后方可转正（verified=true）。
 */
class FixtureClassicTexts : ClassicTextRepository {

    override val version: String = "classic-fixture-v0"

    private val texts: Map<Int, ClassicHexagramText> = listOf(
        ClassicHexagramText(
            kingWenOrder = 1,
            hexagramName = "乾",
            judgment = "乾：元，亨，利，贞。",
            imageText = "象曰：天行健，君子以自强不息。",
            lineTexts = emptyList(),
            version = version,
            verified = false,
        ),
        ClassicHexagramText(
            kingWenOrder = 2,
            hexagramName = "坤",
            judgment = "坤：元亨，利牝马之贞。君子有攸往，先迷后得主，利。西南得朋，东北丧朋。安贞吉。",
            imageText = "象曰：地势坤，君子以厚德载物。",
            lineTexts = emptyList(),
            version = version,
            verified = false,
        ),
        ClassicHexagramText(
            kingWenOrder = 3,
            hexagramName = "屯",
            judgment = "屯：元亨，利贞。勿用有攸往，利建侯。",
            imageText = "象曰：云雷，屯；君子以经纶。",
            lineTexts = emptyList(),
            version = version,
            verified = false,
        ),
        ClassicHexagramText(
            kingWenOrder = 4,
            hexagramName = "蒙",
            judgment = "蒙：亨。匪我求童蒙，童蒙求我。初筮告，再三渎，渎则不告。利贞。",
            imageText = "象曰：山下出泉，蒙；君子以果行育德。",
            lineTexts = emptyList(),
            version = version,
            verified = false,
        ),
        ClassicHexagramText(
            kingWenOrder = 63,
            hexagramName = "既济",
            judgment = "既济：亨小，利贞。初吉，终乱。",
            imageText = "象曰：水在火上，既济；君子以思患而豫防之。",
            lineTexts = emptyList(),
            version = version,
            verified = false,
        ),
        ClassicHexagramText(
            kingWenOrder = 64,
            hexagramName = "未济",
            judgment = "未济：亨。小狐汔济，濡其尾，无攸利。",
            imageText = "象曰：火在水上，未济；君子以慎辨物居方。",
            lineTexts = emptyList(),
            version = version,
            verified = false,
        ),
    ).associateBy { it.kingWenOrder }

    override fun byKingWenOrder(order: Int): ClassicHexagramText? = texts[order]
}
