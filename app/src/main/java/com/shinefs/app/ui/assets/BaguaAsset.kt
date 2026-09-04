package com.shinefs.app.ui.assets

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.shinefs.core.yijing.model.Trigram

/**
 * ShineFS 八卦视觉资产的几何入口。
 *
 * 卦爻必须从 [Trigram.lines]（自下而上）绘制，不使用字体里的 Unicode 卦符：
 * 字体字形会随设备变化，且很难保证阴爻断口、线宽和间距一致。
 * 动态罗盘环使用后天八卦：从正北开始，顺时针一卦一方。
 */
object BaguaAsset {
    /** 品牌标记使用的先天图位：从图面上方开始顺时针。 */
    val xiantianBrandOrder: List<Trigram> = listOf(
        Trigram.QIAN,
        Trigram.DUI,
        Trigram.LI,
        Trigram.ZHEN,
        Trigram.KUN,
        Trigram.XUN,
        Trigram.KAN,
        Trigram.GEN,
    )

    /** 电子罗盘业务层使用的后天图位：正北起顺时针。 */
    val postnatalOrder: List<Trigram> = listOf(
        Trigram.KAN,
        Trigram.GEN,
        Trigram.ZHEN,
        Trigram.XUN,
        Trigram.LI,
        Trigram.KUN,
        Trigram.DUI,
        Trigram.QIAN,
    )
}

/**
 * 绘制一圈真实八卦爻线。
 *
 * [glyphRadius] 是每个卦象中心到圆心的距离；卦象本身始终以正北为基准，
 * 通过整体旋转跟随罗盘盘面。参数按画布像素传入，便于在不同尺寸的罗盘上等比缩放。
 */
fun DrawScope.drawPostnatalBagua(
    center: Offset,
    glyphRadius: Float,
    glyphWidth: Float,
    lineStroke: Float,
    lineGap: Float,
    lineSpacing: Float,
    color: Color,
    rotation: Float = 0f,
) {
    BaguaAsset.postnatalOrder.forEachIndexed { index, trigram ->
        rotate(rotation + index * 45f, pivot = center) {
            drawTrigram(
                center = Offset(center.x, center.y - glyphRadius),
                trigram = trigram,
                width = glyphWidth,
                stroke = lineStroke,
                gap = lineGap,
                spacing = lineSpacing,
                color = color,
            )
        }
    }
}

private fun DrawScope.drawTrigram(
    center: Offset,
    trigram: Trigram,
    width: Float,
    stroke: Float,
    gap: Float,
    spacing: Float,
    color: Color,
) {
    val safeWidth = width.coerceAtLeast(stroke)
    val safeStroke = stroke.coerceAtLeast(0.1f)
    val safeGap = gap.coerceIn(0f, safeWidth * 0.7f)
    val halfWidth = safeWidth / 2f
    val halfGap = safeGap / 2f

    // lines 自下而上存储；画布从上到下，所以这里反转为上、中、下三爻。
    trigram.lines.asReversed().forEachIndexed { row, yang ->
        val y = center.y + (row - 1) * spacing
        if (yang == 1) {
            drawLine(
                color = color,
                start = Offset(center.x - halfWidth, y),
                end = Offset(center.x + halfWidth, y),
                strokeWidth = safeStroke,
                cap = StrokeCap.Butt,
            )
        } else {
            drawLine(
                color = color,
                start = Offset(center.x - halfWidth, y),
                end = Offset(center.x - halfGap, y),
                strokeWidth = safeStroke,
                cap = StrokeCap.Butt,
            )
            drawLine(
                color = color,
                start = Offset(center.x + halfGap, y),
                end = Offset(center.x + halfWidth, y),
                strokeWidth = safeStroke,
                cap = StrokeCap.Butt,
            )
        }
    }
}
