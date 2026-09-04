# ShineFS V2.1 发布与验收报告

> 日期：2026-09-04（Asia/Shanghai）
> 版本：`versionCode 3` / `versionName 2.1`
> applicationId：`com.shinefs.app`
> 最终判定：**CONDITIONAL PASS**

## 1. 结论

V2.1 已完成 Cycle 11A～11H 的代码、测试、迁移、文档和 CI 收口。设备时区、真实定盘快照、HoldPose 四态识别、平放/竖持双姿态算法、Display Rotation 补偿、起卦前动态持握引导、快照/历史测量留痕、原典 verification status 和 GitHub Actions 门禁均已接入。

自动化测试、Debug/Lint/Release variant 构建、设备端历法冒烟以及模拟器飞行模式下的时间起卦/罗盘时空合参闭环通过。当前工作机只连接 `emulator-5554`，没有物理 Android 真机，因此真实磁场硬件、四种 Display Rotation 实测、平放/竖持硬件误差和无磁力计设备降级不能宣称完成；按验收规则保留为 **CONDITIONAL PASS**。

## 2. Cycle 提交与推送

| Cycle | 提交 | 主题 |
|---|---|---|
| 11A | `73252d9` | 设备时区与 UI/核心/历史时间统一 |
| 11B | `81e1004` | 真实 `LockedCompassSnapshot` 与 Room v4 |
| 11C | `46ff0ed` | HoldPose、防抖、双姿态和 Display Rotation |
| 11D | `b5ecabb` | 起卦前动态持握引导与 readiness 自动通过 |
| 11E | `f214ff5` | 原典 `CorpusVerificationStatus` 与措辞收口 |
| 11F | `fba898c` | GitHub Actions CI 与 Release 签名边界 |
| 11G | `fe4b5a2` | 模拟器飞行模式 E2E 与设备边界记录 |
| 11H | 本报告所属最终发布提交（以 `git log` 为准） | 版本、历史测量留痕、仓库卫生与最终验收 |

上述 Cycle 提交均在 `main` 上按 Cycle 完成推送；11H 提交完成后以远端 `origin/main` 和 Actions 运行结果为最终依据。

## 3. 已交付能力

- 生产时区使用 `TimeZone.getDefault()`；同一次点击/定盘保存 epoch、zoneId、UTC offset、本地日期时间和可追溯 instant。
- `DivinationServiceV2` 的生产时空路径只接收定盘瞬间复制的真实 `LockedCompassSnapshot`，不重新构造 `CompassEngine` 或虚假 `CompassState`；旧入口仅保留显式 legacy 兼容适配。
- `HoldPoseDetector` 识别 `FLAT`、`UPRIGHT`、`TRANSITION`、`INVALID`，包含进入/退出迟滞、稳定等待和快速大幅姿态变化抑制。
- Flat/Upright resolver 使用统一的真实手机顶部水平投影，结合 Display Rotation 0/90/180/270°；JVM 属性测试覆盖 24 个方向，双姿态误差 ≤5°。
- 起卦前 readiness 同时检查姿态、稳定度、磁场干扰/强度和方向/磁场精度；引导会提示平放、竖持、保持稳定、远离干扰或校准，满足条件后自动通过。
- 定盘快照和历史记录保留姿态、置信度、姿态稳定时长、pitch/roll、方向精度、磁场精度、磁场强度、磁扰、稳定标准差、Display Rotation、北向和锁定时间；历史详情页可直接查看。
- 电子底本状态明确为 `ELECTRONIC_STRUCTURE_VERIFIED`；未完成独立第二来源全量校勘时不显示“已核定原典”。
- `.github/workflows/android.yml` 对 main push/PR 和手动触发执行 JVM test、lintDebug、Debug/Release variant 构建；CI Release 只允许 unsigned 构建验证，本地正式发布仍要求环境签名。

## 4. 自动化、构建与 CI 证据

以下数字来自最终版本号和最终历史详情代码的实际输出；`test` 同时执行 Debug/Release 两个 App variant，因此同时列出唯一用例数和 variant 执行数，避免重复计数。

| 门禁 | 命令/范围 | 结果 |
|---|---|---|
| JVM 全量单测 | `./gradlew test`（`:app` + `:core:*`） | 167 个唯一用例；Debug/Release variant 合计 181 次执行；0 failure/0 error/0 skipped |
| Lint | `./gradlew lintDebug` | BUILD SUCCESSFUL；0 error |
| Debug | `./gradlew :app:assembleDebug` | BUILD SUCCESSFUL；最终 APK 已安装到模拟器 |
| Release variant | `CI=true ./gradlew :app:assembleRelease`；本地 legacy 环境签名路径 | BUILD SUCCESSFUL；`com.shinefs.app` / code 3 / name 2.1；APK v2 签名验证通过 |
| 设备端 | `./gradlew :app:connectedDebugAndroidTest` | 3/3；`CalendarDeviceSmokeTest` failures=0/errors=0/skipped=0 |
| GitHub Actions | `.github/workflows/android.yml`，最新 `main` SHA | 11H 推送后核对最新运行并回填 URL |

CI 的 unsigned Release APK 只证明 variant 可编译，不能直接作为对外发布包；正式包必须通过 `SHINEFS_RELEASE_*` 或兼容旧变量提供的 keystore 签名路径。

## 5. 模拟器飞行模式 E2E

环境：`emulator-5554` / `Medium_Phone` / API 37.1 / x86_64；`airplane_mode_on=1`；App 无网络权限，测试不依赖联网。

- 路径 A：首页 → 传统时间起卦 → 卦象 → 九段解读；时间上下文和“电子底本已校验”可见。
- 路径 B：首页 → 风水罗盘 → readiness“姿态正确，已自动通过” → 定盘 → 场景 → 卦象 → 时空报告；报告保留锁定方位和锁定时刻。
- Room 空间历史行已实查 `holdPose`、置信度、姿态稳定时长、Display Rotation、pitch/roll、方向/磁场精度、磁场强度和干扰字段；纯时间行的空间字段为空。
- 设备端历法测试已执行 3/3；最终 UI dump 验证 `shinefs_measurement_metadata`、姿态/角度/双精度/磁场状态和“姿态正确，已自动通过”；末次 logcat 未发现 `FATAL EXCEPTION` 或 `ANR in`。
- 最终 Room 行实查：`snapshotCapturedAt == timestamp`；空间行 `UPRIGHT`、置信度约 89%、姿态稳定约 61772ms、Display Rotation 0、pitch 85.276°、roll 0.020°、方向/磁场精度 HIGH、磁场约 48.760µT、干扰 0、`zoneId=GMT`、`utcOffsetMinutes=0`，本地时间与 UI 定盘时间一致。

模拟器的 Goldfish 虚拟传感器只能证明接线与闭环可运行，不能替代物理磁力计证据。

## 6. 物理真机边界与遗留项

当前 `adb devices` 只有 `emulator-5554`，未发现物理 Android 设备。以下项目已在 [`REAL_DEVICE_TEST.md`](REAL_DEVICE_TEST.md) 固化步骤和记录表，连接设备后继续执行：

- 0/90/180/270° Display Rotation 下的手机顶部定义；
- 同一方向平放/竖持方位最短环形误差 ≤5°；
- 正常磁场范围、磁铁/金属干扰暂停与恢复、8 字校准前后精度；
- 无磁力计设备的 LIMITED 降级与“不显示伪造方位”；
- 多机型生命周期、省电、冷启动和真实磁场绝对误差。

此外保留 TD-01 Gradle deprecated 警告、设计 Token 后续核定和历史文档中的早期技术债；它们不改变本 Cycle 的代码门禁结果。未遇到文献实质冲突、Android 坐标体系无法统一或无法解释的跨设备系统性方位偏差。

## 7. 11H 最终 Re-Check 记录

- 代码门禁在最终措辞修正（“规则核对状态”与原典 `CorpusVerificationStatus` 分离）后重新通过。
- 仓库扫描无 `__pycache__` 或 `*.pyc`；APK、测试日志、签名文件保持 ignored，不进入提交。
- 物理 Android 真机仍未连接，因此最终判定保持 CONDITIONAL PASS，而不是宣称完整 PASS。

## 8. 相关文档与产物

- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`COMPASS_HOLD_POSE.md`](COMPASS_HOLD_POSE.md)
- [`REAL_DEVICE_TEST.md`](REAL_DEVICE_TEST.md)
- [`TEST_MATRIX.md`](TEST_MATRIX.md)
- [`PDCA_LOG.md`](PDCA_LOG.md)
- [`YIJING_RULES.md`](YIJING_RULES.md)
- [`CHANGELOG.md`](../CHANGELOG.md)
- [`android.yml`](../.github/workflows/android.yml)

构建目录、测试日志、签名文件和 APK 均不纳入 Git；仓库内不保留 `__pycache__` 或 `*.pyc`。
