package com.shinefs.app.ui.compass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.shinefs.app.ui.theme.ShineColors
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.compass.CircularMath
import com.shinefs.core.yijing.model.Trigram
import com.shinefs.core.yijing.rules.LaterHeavenBagua
import com.shinefs.core.yijing.rules.Mountains24
import kotlin.math.cos
import kotlin.math.sin

/**
 * 动态罗盘（传统样式，产品方案 §8）。
 *
 * 交互模式：盘转针定——盘面随方位角旋转，顶部固定朱砂向首指针；
 * 天池磁针印在盘面上（针尖恒指盘面北位，与实盘行为一致），未稳定时微摆。
 *
 * 层次（外→内）：刻度环 / 角度数字 / 方位环（八方）/ 二十四山环（当前山高亮）/
 * 八卦环 / 五行弧 / 天池（太极 + 磁针）。
 */
@Composable
fun CompassDial(
    azimuth: Float?,
    stability: StabilityLevel,
    locked: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    // 盘面旋转角：目标 ≡ -azimuth (mod 360)，按最短路径累积，避免 359→0 反向绕整圈
    var targetRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(azimuth) {
        if (azimuth != null) {
            targetRotation += CircularMath.shortestDiff(targetRotation, -azimuth)
        }
    }
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = if (reducedMotion) {
            tween(0)
        } else {
            spring(dampingRatio = 0.9f, stiffness = 1400f)
        },
        label = "dialRotation",
    )

    // 磁针微摆：不稳定时摆动、稳定后衰减；减少动画时静止
    val swayAmplitude = when {
        locked -> 0f
        stability == StabilityLevel.GOOD -> 0.8f
        stability == StabilityLevel.FAIR -> 3f
        else -> 6f
    }
    val sway: Float = if (reducedMotion || swayAmplitude == 0f) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "needleSway")
        transition.animateFloat(
            initialValue = -swayAmplitude,
            targetValue = swayAmplitude,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "sway",
        ).value
    }

    val facingMountain = azimuth?.let { Mountains24.mountainAt(it) }

    Box(modifier = modifier.fillMaxWidth().aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val c = center
            val r = size.minDimension / 2f

            drawCircle(ShineColors.BackgroundDeep, radius = r)
            drawCircle(ShineColors.BackgroundRaised, radius = r * 0.965f)
            drawCircle(ShineColors.GoldMuted, radius = r * 0.965f, style = Stroke(1.5f))
            drawCircle(ShineColors.Divider, radius = r * 0.90f, style = Stroke(1f))

            rotate(animatedRotation, pivot = c) {
                drawTickRing(c, r)
                drawDegreeNumerals(c, r, textMeasurer)
                drawDirectionRing(c, r, textMeasurer)
                drawMountainRing(c, r, textMeasurer, facingMountain)
                drawTrigramRing(c, r, textMeasurer)
                drawElementRing(c, r)
                drawTianchi(c, r, sway)
            }

            drawFacingPointer(c, r)
        }
    }
}

private fun DrawScope.drawTickRing(c: Offset, r: Float) {
    val ringR = r * 0.955f
    for (deg in 0 until 360 step 2) {
        val major = deg % 30 == 0
        val medium = deg % 10 == 0
        val len = when {
            major -> r * 0.045f
            medium -> r * 0.028f
            else -> r * 0.014f
        }
        val color = when {
            major -> ShineColors.GoldPrimary
            medium -> ShineColors.GoldMuted
            else -> ShineColors.Divider
        }
        val a = Math.toRadians(deg.toDouble())
        val dir = Offset(cos(a).toFloat(), sin(a).toFloat())
        drawLine(
            color = color,
            start = c + dir * (ringR - len),
            end = c + dir * ringR,
            strokeWidth = if (major) 2.5f else 1.2f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawDegreeNumerals(c: Offset, r: Float, tm: androidx.compose.ui.text.TextMeasurer) {
    val radius = r * 0.845f
    for (deg in 0 until 360 step 30) {
        val label = deg.toString()
        val layout = tm.measure(label, TextStyle(color = ShineColors.TextSecondary, fontSize = 9.sp))
        rotate(deg.toFloat(), pivot = c) {
            drawText(
                layout,
                topLeft = Offset(c.x - layout.size.width / 2f, c.y - radius - layout.size.height / 2f),
            )
        }
    }
}

private val DIRECTIONS = listOf(
    0f to "北", 45f to "东北", 90f to "东", 135f to "东南",
    180f to "南", 225f to "西南", 270f to "西", 315f to "西北",
)

private fun DrawScope.drawDirectionRing(c: Offset, r: Float, tm: androidx.compose.ui.text.TextMeasurer) {
    val radius = r * 0.775f
    DIRECTIONS.forEach { (deg, label) ->
        val isNorth = deg == 0f
        val layout = tm.measure(
            label,
            TextStyle(
                color = if (isNorth) ShineColors.CinnabarBright else ShineColors.TextSecondary,
                fontSize = if (label.length == 1) 15.sp else 10.sp,
            ),
        )
        rotate(deg, pivot = c) {
            drawText(
                layout,
                topLeft = Offset(c.x - layout.size.width / 2f, c.y - radius - layout.size.height / 2f),
            )
        }
    }
}

private fun mountainColor(mountainIndex: Int): Color = when (LaterHeavenBagua.trigramAt(Mountains24.centerAngleOf(mountainIndex))) {
    Trigram.QIAN, Trigram.DUI -> ShineColors.ElementMetal
    Trigram.ZHEN, Trigram.XUN -> ShineColors.ElementWood
    Trigram.KAN -> ShineColors.ElementWater
    Trigram.LI -> ShineColors.ElementFire
    Trigram.GEN, Trigram.KUN -> ShineColors.ElementEarth
}

private fun DrawScope.drawMountainRing(c: Offset, r: Float, tm: androidx.compose.ui.text.TextMeasurer, facingMountain: String?) {
    val radius = r * 0.665f
    // 当前山扇区高亮（金弧）。drawArc 的 0° 在正右方（东），北基准角度需 -90 对齐。
    if (facingMountain != null) {
        val idx = Mountains24.names.indexOf(facingMountain)
        val centerDeg = Mountains24.centerAngleOf(idx)
        val arcR = r * 0.71f
        drawArc(
            color = ShineColors.GoldBright.copy(alpha = 0.5f),
            startAngle = centerDeg - 7.5f - 90f,
            sweepAngle = 15f,
            useCenter = false,
            topLeft = Offset(c.x - arcR, c.y - arcR),
            size = androidx.compose.ui.geometry.Size(arcR * 2, arcR * 2),
            style = Stroke(width = r * 0.10f, cap = StrokeCap.Butt),
        )
    }
    Mountains24.names.forEachIndexed { idx, mountain ->
        val isFacing = mountain == facingMountain
        val layout = tm.measure(
            mountain,
            TextStyle(
                color = if (isFacing) ShineColors.GoldBright else ShineColors.TextPrimary,
                fontSize = if (isFacing) 19.sp else 14.sp,
            ),
        )
        rotate(Mountains24.centerAngleOf(idx), pivot = c) {
            drawText(
                layout,
                topLeft = Offset(c.x - layout.size.width / 2f, c.y - radius - layout.size.height / 2f),
            )
        }
    }
}

private fun DrawScope.drawTrigramRing(c: Offset, r: Float, tm: androidx.compose.ui.text.TextMeasurer) {
    val radius = r * 0.545f
    Trigram.entries.forEach { trigram ->
        val layout = tm.measure(
            trigram.symbol,
            TextStyle(color = ShineColors.GoldPrimary, fontSize = 17.sp),
        )
        rotate(trigram.directionAngle, pivot = c) {
            drawText(
                layout,
                topLeft = Offset(c.x - layout.size.width / 2f, c.y - radius - layout.size.height / 2f),
            )
        }
    }
}

/** 五行层：随二十四山领卦着色的暗弧（克制，仅细描）。 */
private fun DrawScope.drawElementRing(c: Offset, r: Float) {
    val ringR = r * 0.455f
    Mountains24.names.indices.forEach { idx ->
        val centerDeg = Mountains24.centerAngleOf(idx)
        drawArc(
            color = mountainColor(idx).copy(alpha = 0.95f),
            startAngle = centerDeg - 7.2f - 90f,
            sweepAngle = 14.4f,
            useCenter = false,
            topLeft = Offset(c.x - ringR, c.y - ringR),
            size = androidx.compose.ui.geometry.Size(ringR * 2, ringR * 2),
            style = Stroke(width = r * 0.026f),
        )
    }
    drawCircle(ShineColors.GoldMuted, radius = ringR, center = c, style = Stroke(1f))
}

/** 天池：玄黑水色圆 + 太极 + 磁针（印于盘面，针尖指盘面北）。swayDeg 为微摆角。 */
private fun DrawScope.drawTianchi(c: Offset, r: Float, swayDeg: Float) {
    val poolR = r * 0.40f
    drawCircle(Color(0xFF11151C), radius = poolR, center = c)
    drawCircle(ShineColors.GoldMuted, radius = poolR, center = c, style = Stroke(1.2f))

    // 太极（小幅居上，磁针叠于其上）
    val taijiR = poolR * 0.52f
    val taijiCenter = Offset(c.x, c.y - poolR * 0.22f)
    drawTaiji(taijiCenter, taijiR)

    // 磁针：北半朱砂（菱形细针），南半素金
    val needleLen = poolR * 0.92f
    val northW = poolR * 0.10f
    withTransform({ rotate(swayDeg, pivot = c) }) {
        val north = Path().apply {
            moveTo(c.x, c.y - needleLen)
            lineTo(c.x + northW, c.y)
            lineTo(c.x - northW, c.y)
            close()
        }
        drawPath(north, ShineColors.CinnabarBright)
        val south = Path().apply {
            moveTo(c.x, c.y + needleLen)
            lineTo(c.x + northW * 0.7f, c.y)
            lineTo(c.x - northW * 0.7f, c.y)
            close()
        }
        drawPath(south, ShineColors.GoldBright)
        drawCircle(ShineColors.BackgroundDeep, radius = northW * 0.45f, center = c)
        drawCircle(ShineColors.GoldMuted, radius = northW * 0.45f, center = c, style = Stroke(1f))
    }
}

private fun DrawScope.drawTaiji(center: Offset, radius: Float) {
    val ivory = Color(0xFFE8E2D4)
    val black = Color(0xFF15161A)
    drawCircle(ivory, radius = radius, center = center)
    // 右半黑
    drawArc(
        color = black,
        startAngle = -90f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
    )
    // 上小圆：上半黑（黑入白）
    val smallR = radius / 2f
    val topC = Offset(center.x, center.y - smallR)
    drawArc(
        color = black,
        startAngle = 180f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(topC.x - smallR, topC.y - smallR),
        size = androidx.compose.ui.geometry.Size(smallR * 2, smallR * 2),
    )
    // 下小圆：下半白（白入黑）
    val bottomC = Offset(center.x, center.y + smallR)
    drawArc(
        color = ivory,
        startAngle = 0f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(bottomC.x - smallR, bottomC.y - smallR),
        size = androidx.compose.ui.geometry.Size(smallR * 2, smallR * 2),
    )
    drawCircle(ivory, radius = smallR * 0.28f, center = topC)
    drawCircle(black, radius = smallR * 0.28f, center = bottomC)
    drawCircle(ShineColors.GoldMuted, radius = radius, center = center, style = Stroke(1f))
}

/** 顶部固定向首指针（朱砂，指向盘心方向，不随盘旋转；位于山环外侧不压字）。 */
private fun DrawScope.drawFacingPointer(c: Offset, r: Float) {
    val tipY = r * 0.78f
    val baseY = r * 0.95f
    val p = Path().apply {
        moveTo(c.x, c.y - tipY)
        lineTo(c.x + r * 0.045f, c.y - baseY)
        lineTo(c.x - r * 0.045f, c.y - baseY)
        close()
    }
    drawPath(p, ShineColors.CinnabarBright)
    drawPath(p, ShineColors.Cinnabar, style = Stroke(1f))
}
