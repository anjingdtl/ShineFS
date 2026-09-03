package com.shinefs.core.compass

/**
 * 磁场环境监测：依据磁力计合磁感应强度判断环境异常。
 *
 * 地球磁场典型范围约 25–65 µT。默认阈值 [10, 100] µT 之外判为异常
 * （靠近金属/音箱/磁吸壳/汽车等）。**阈值需真实设备标定后修订**
 * （DOCS/REAL_DEVICE_TEST.md 磁干扰项）。
 */
class MagneticMonitor(
    private val minNormalUt: Float = 10f,
    private val maxNormalUt: Float = 100f,
) {
    var magnitudeUt: Float? = null
        private set

    val anomaly: Boolean
        get() = magnitudeUt?.let { it < minNormalUt || it > maxNormalUt } ?: false

    fun onMagnitude(ut: Float) {
        magnitudeUt = ut
    }

    fun reset() {
        magnitudeUt = null
    }
}
