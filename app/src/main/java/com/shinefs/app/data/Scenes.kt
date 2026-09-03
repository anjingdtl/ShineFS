package com.shinefs.app.data

/** 宅居测局场景（产品方案 §9.5）+ 单项测量。 */
data class SceneType(val id: String, val name: String, val guidance: String)

object Scenes {
    val generic = SceneType("generic", "单项测量", "任意方位测量，不归入宅居测局。")
    val house = listOf(
        SceneType("front_door", "大门", "立于宅内正对大门，手机正对门向定盘。"),
        SceneType("living_room", "客厅", "立于客厅中心，朝向主要采光面或坐席主向定盘。"),
        SceneType("master_bedroom", "主卧", "立于卧室门内侧，正对床向定盘。"),
        SceneType("study", "书房", "正对书桌坐向定盘（测坐向，背对桌即坐）。"),
        SceneType("stove", "灶位", "正对灶台朝向定盘。"),
        SceneType("balcony", "阳台", "立于阳台内侧，朝向外景定盘。"),
        SceneType("office", "办公位", "坐于工位，测桌面朝向（向首）。"),
        SceneType("shop_entrance", "商铺入口", "立于店内正对入口定盘。"),
    )

    fun byId(id: String): SceneType = house.firstOrNull { it.id == id } ?: generic
}
