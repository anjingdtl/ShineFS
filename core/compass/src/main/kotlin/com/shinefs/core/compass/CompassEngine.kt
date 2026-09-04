package com.shinefs.core.compass

/**
 * 罗盘引擎：原始方位角 → 归一化 → 突跳抑制 → 环形指数平滑 → 稳定检测。
 *
 * 设计（产品方案 §6.3）：
 * - 平滑走最短角路径，359°→0° 跨界不绕整圈；
 * - 单样本突跳（≥ [glitchThresholdDeg]，且此前已处于良好稳定）判为毛刺并丢弃；
 * - 稳定度 = 最近 [stabilityWindow] 个原始样本的环形标准差分档；
 * - 磁场干扰、倾斜超限独立标记，由上层决定是否暂停定盘。
 *
 * 本类无 Android 依赖、无协程依赖，全部逻辑可在 JVM 单测复现。
 */
class CompassEngine(
    private val alpha: Float = 0.2f,
    private val stabilityWindow: Int = 24,
    private val glitchThresholdDeg: Float = 175f,
    private val goodStdDeg: Float = 0.6f,
    private val fairStdDeg: Float = 2.5f,
    private val tiltLimitDeg: Float = 45f,
    private val magneticMonitor: MagneticMonitor = MagneticMonitor(),
) {
    init {
        require(alpha in 0.05f..1f) { "alpha must be in (0,1], got $alpha" }
        require(stabilityWindow >= 4)
    }

    private var smoothed: Float? = null
    private var prevRaw: Float? = null
    private val window = ArrayDeque<Float>(stabilityWindow)

    var state: CompassState = CompassState()
        private set

    val magnetic: MagneticMonitor get() = magneticMonitor

    /** 输入一帧原始方位角（度）。pitch/roll 可选，用于倾斜判定。返回最新状态。 */
    @Synchronized
    fun onAzimuth(
        rawAzimuthDeg: Float,
        pitchDeg: Float? = null,
        rollDeg: Float? = null,
        tooTiltedOverride: Boolean? = null,
    ): CompassState {
        require(!rawAzimuthDeg.isNaN()) { "azimuth must not be NaN" }
        val raw = CircularMath.normalize(rawAzimuthDeg)

        val prev = prevRaw
        prevRaw = raw
        val wasStable = state.stability == StabilityLevel.GOOD
        val jumpDeg = prev?.let { kotlin.math.abs(CircularMath.shortestDiff(it, raw)) }
        val glitch = jumpDeg != null && jumpDeg >= glitchThresholdDeg && wasStable

        if (!glitch) {
            val s = smoothed
            smoothed = if (s == null) {
                raw
            } else {
                CircularMath.normalize(s + alpha * CircularMath.shortestDiff(s, raw))
            }
            window.addLast(raw)
            while (window.size > stabilityWindow) window.removeFirst()
        }

        val std = if (window.size >= stabilityWindow / 2) {
            CircularMath.circularStdDeg(window.toList())
        } else {
            Float.NaN
        }
        val stability = when {
            std.isNaN() -> StabilityLevel.UNSTABLE
            std <= goodStdDeg -> StabilityLevel.GOOD
            std <= fairStdDeg -> StabilityLevel.FAIR
            else -> StabilityLevel.UNSTABLE
        }
        val calculatedTooTilted = (pitchDeg != null && kotlin.math.abs(pitchDeg) > tiltLimitDeg) ||
            (rollDeg != null && kotlin.math.abs(rollDeg) > tiltLimitDeg)
        val tooTilted = tooTiltedOverride ?: calculatedTooTilted

        state = state.copy(
            samples = state.samples + 1,
            rawAzimuth = raw,
            smoothedAzimuth = smoothed,
            stability = stability,
            stabilityStdDeg = std,
            tooTilted = tooTilted || state.tooTilted && pitchDeg == null && rollDeg == null,
            pitchDeg = pitchDeg ?: state.pitchDeg,
            rollDeg = rollDeg ?: state.rollDeg,
            glitchSuppressed = state.glitchSuppressed + if (glitch) 1 else 0,
        )
        return state
    }

    /** 精度事件源：朝向（Rotation Vector / 回退链）与磁力计各自独立，互不覆盖。 */
    enum class AccuracySource { ORIENTATION, MAGNETIC }

    @Synchronized
    fun onAccuracy(accuracy: SensorAccuracy, source: AccuracySource = AccuracySource.ORIENTATION): CompassState {
        state = when (source) {
            AccuracySource.ORIENTATION -> state.copy(orientationAccuracy = accuracy)
            AccuracySource.MAGNETIC -> state.copy(magneticAccuracy = accuracy)
        }
        return state
    }

    @Synchronized
    fun onMagneticMagnitudeUt(ut: Float): CompassState {
        magneticMonitor.onMagnitude(ut)
        state = state.copy(
            magneticInterference = magneticMonitor.anomaly,
            magneticMagnitudeUt = ut,
        )
        return state
    }

    @Synchronized
    fun reset() {
        smoothed = null
        prevRaw = null
        window.clear()
        magneticMonitor.reset()
        state = CompassState()
    }
}
