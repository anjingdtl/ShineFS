# ShineFS V2.0 发布验收记录

> 应用名称：周易风水罗盘
> applicationId：`com.shinefs.app`
> versionCode：`2`
> versionName：`2.0`
> 验收日期：2026-09-04

## 发布结论

**条件通过（可发布）**。

核心演算、离线运行、中文展示、Debug/Release 构建、签名复用、模拟器闭环和设备端历表冒烟均通过。罗盘真实磁场准确度、磁扰阈值和无磁力计真机降级仍需按 `DOCS/REAL_DEVICE_TEST.md` 在真实设备上补测，因此罗盘硬件项保持“条件通过”，不冒充已完成真机验收。

## 本次收尾优化

- 开屏更换为玄黑、金铜罗盘与黑白太极视觉，主图源文件：`DOCS/assets/splash_v2/shinefs_splash_hero.png`，Android 资源：`app/src/main/res/drawable-nodpi/splash_hero.jpg`。
- 图标中心统一为黑白太极；同步 adaptive、圆形、单色主题层和五档密度位图。
- 全面清理用户可见的英文、规则编号、版本串、校验串和工程字段：首页、罗盘、时间起卦、场景选择、卦象、九段解读、卦例管理、宅居测局、规则与典籍、六十四卦列表及详情均改为自然中文。
- 历史卦例增加兼容显示转换：旧记录仍可查看，但不把内部规则编号、时区标识或数据校验串带到用户界面。
- 内部包名、数据库字段、测试语义标识和规则枚举保持不变，避免破坏存量数据、自动化验收和程序兼容。

## 构建与签名

| 项目 | 结果 |
|---|---|
| Debug 构建 | `clean test lint assembleDebug` 通过 |
| JVM 单元测试 | 148/148 通过 |
| 设备端测试 | 3/3 通过（Medium_Phone，API 37） |
| 连通测试 | `connectedDebugAndroidTest` 通过 |
| Release 构建 | `:app:assembleRelease` 通过 |
| 包元数据 | `com.shinefs.app` / `versionCode 2` / `versionName 2.0` |
| 签名方案 | APK v2 已验证 |
| Release 包 SHA-256 | `b0bc4739556f07d72a78c70ef836690b0357dbfcacb9cfa453f6e7f8728c2ef9` |
| 证书 SHA-256 | `017b3fbed4001083f2f70a0c51e8e463322df66b095e1c3a476fdd0d86dc2a0a` |
| 最终 APK | `release/ShineFS-v2.0-release.apk` |

签名证书与 TAVO-MINI 官方发布证书一致。签名口令仅通过本机环境变量提供，未写入仓库、文档或命令输出。

## 模拟器验收

环境：`emulator-5554` / `Medium_Phone` / API 37，飞行模式开启，系统动画关闭以便稳定取证。

- 首次启动进入开屏，随后进入首页，进程存活，无应用级 FATAL/ANR。
- 六入口及相关页面展示文字扫描无英文展示内容；仅保留中文、数字、卦符和必要传统符号。
- 传统时间起卦：时间盘、换日说明、起卦、卦象、九段解读、离线复核均通过。
- 时空链路：罗盘读数、定盘、场景选择、起卦入口通过；模拟器实际提供虚拟磁力计/旋转向量，展示为完整罗盘状态。
- 卦例管理：收藏、历史筛选、删除确认通过；备注输入控件存在，键盘输入能力仍建议真机复核。
- 宅居测局：八个场景、进度、摘要和说明均为中文，页面可正常打开。

截图证据位于 `DOCS/assets/release_v2/`：

`release_v20_01_launcher.png`、`release_v20_02_home.png`、`release_v20_03_timecast.png`、`release_v20_04_hexagram.png`、`release_v20_05_report.png`、`release_v20_06_classics.png`、`release_v20_07_rules.png`、`release_v20_08_history.png`、`release_v20_09_airplane.png`、`release_v20_10_sensor_limited.png`。

其中 `release_v20_10_sensor_limited.png` 按验收文件名留存，但本次模拟器实际是完整虚拟传感器状态，不代表“有限模式”已经在无磁力计设备上验证。

## 发布后仍需补测

1. 真实 Android 手机的正北/正东/正南/正西、跨 0/360°、快慢旋转和多圈旋转。
2. 真机磁场干扰、画 8 字校正、倾斜姿态、稳定阈值和磁扰阈值。
3. 无磁力计设备的有限模式横幅及不伪造方位读数。
4. 原典更大范围的独立文献抽查，以及后天端法正式入口等冻结项。
