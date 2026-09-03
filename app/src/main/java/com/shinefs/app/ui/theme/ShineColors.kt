package com.shinefs.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ShineFS 设计 Token 初稿（方案 §7.2）。
 * HEX 为 Cycle 00 占位值，正式值在视觉统一周期（Cycle 08）前核定；
 * 页面禁止绕过本对象硬编码颜色。
 */
object ShineColors {
    val BackgroundDeep = Color(0xFF0C0C0F)   // 玄黑
    val BackgroundRaised = Color(0xFF16161A) // 墨黑
    val GoldPrimary = Color(0xFFB8955A)      // 古铜金（占位）
    val GoldMuted = Color(0xFF7E6A44)        // 暗金（占位）
    val Cinnabar = Color(0xFFB03A2E)         // 朱砂（占位）
    val JadeAccent = Color(0xFF5F8F7A)       // 青玉（占位）
    val TextPrimary = Color(0xFFF2EDE3)      // 象牙白
    val TextSecondary = Color(0xFF9A8F7A)    // 灰金
    val Divider = Color(0xFF3A342A)          // 暗铜
}
