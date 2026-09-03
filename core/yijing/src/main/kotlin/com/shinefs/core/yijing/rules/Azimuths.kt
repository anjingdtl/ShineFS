package com.shinefs.core.yijing.rules

/** 方位角公共校验。合法域 [0°, 360°)，北=0°，顺时针增大。 */
object Azimuths {
    fun requireValid(azimuth: Float) {
        require(!azimuth.isNaN()) { "azimuth must not be NaN" }
        require(azimuth >= 0f && azimuth < 360f) { "azimuth must be in [0,360), got $azimuth" }
    }
}
