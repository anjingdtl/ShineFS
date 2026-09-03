# ShineFS 架构说明（ARCHITECTURE）

> 维护规则：每个 PDCA Cycle 结束时同步更新本文件。
> 当前状态：**Cycle 00 基线**（2026-09-03）

## 1. 项目定位

安卓周易风水罗盘 App（代号 ShineFS）。以《周易》卦象体系为根本、手机空间传感器为输入，
八卦/二十四山/六十四卦/确定性起卦规则为演算核心。算法与 AI 解读严格解耦。

产品方案：`DOCS/安卓周易风水罗盘_App_产品方案_V1.0_PDCA建设方案.md`（以下简称"方案"）。

## 2. 技术栈（Cycle 00 基线）

| 项 | 值 | 说明 |
|---|---|---|
| 语言 | Kotlin 2.1.20 | |
| UI | Jetpack Compose + Material3 | Compose BOM 2025.08.01 |
| 构建 | Gradle 9.3.1 wrapper / AGP 8.12.0 | 与本机 Gradle 缓存对齐，可离线构建 |
| JDK | 17（Temurin 17.0.19） | compileOptions/jvmTarget 均为 17 |
| minSdk | 24（Android 7.0） | |
| compileSdk / targetSdk | 36 / 36 | 模拟器 AVD 为 API 37.1，兼容 |
| applicationId | `com.shinefs.app` | |
| 版本 | versionCode 1 / versionName 1.0 | 基线壳 |
| 测试 | JUnit 4.13.2（JVM） | 仪器化测试尚未引入 |

依赖清单见 `gradle/libs.versions.toml`（版本目录单一事实源）。

## 3. 模块结构

```text
ShineFS/
├─ app/                    # Android Application（Compose 壳，包名 com.shinefs.app）
│  └─ src/
│     ├─ main/java/com/shinefs/app/
│     │  ├─ MainActivity.kt          # 基线首页（占位，无业务交互）
│     │  └─ ui/theme/ShineColors.kt  # 设计 Token 初稿（方案 §7.2，HEX 为占位值）
│     └─ test/.../BaselineSmokeTest.kt
├─ gradle/libs.versions.toml
├─ DOCS/                   # 方案与过程文档（本仓库文档根，对应方案中的 docs/）
└─ CHANGELOG.md
```

### 目标结构（按方案 §17 演进，未到期不得提前建设）

```text
├─ app/                    # Compose 壳 + 导航
├─ core/yijing/            # 纯 Kotlin 术数核心（Cycle 01）——无 Android/UI/AI 依赖
├─ core/compass/           # 传感器罗盘引擎（Cycle 02）
└─ feature/*               # 页面模块（Cycle 03+）
```

## 4. 架构原则（摘自方案，具有约束力）

1. **算法与 AI 解耦**：方位/八卦/二十四山/本卦/动爻/变卦由确定性规则引擎计算；AI 仅做白话解释。
2. **UI 与规则解耦**：`core/yijing` 必须可脱离 Android 在 JVM 上测试（纯 Kotlin 模块）。
3. **V1.0 术数边界**：仅用后天八卦、二十四山、六十四卦、确定性起卦；不得混入飞星/奇门/八字等。
4. **经典文本可溯源**：卦辞爻辞必须来自人工核定的版本化数据文件，禁止大模型生成。
5. **视觉规范**：玄黑/古铜金/朱砂传统数术风格；禁止赛博科技指南针风格。

## 5. 数据与状态

- 数据库：**无**（Room 计划于 Cycle 07 引入，用于卦例与规则版本存储）。
- 持久化：**无**（DataStore 计划用于用户设置）。
- 传感器：**无**（SensorManager/Rotation Vector 计划于 Cycle 02 引入）。
- AI 层：**无**（Cycle 05 仅做接口抽象）。

## 6. 构建与验证基线

| 命令 | 结果（Cycle 00） |
|---|---|
| `./gradlew :app:assembleDebug` | BUILD SUCCESSFUL（首次 2m36s 含一次失败修复，修复后 16s） |
| `./gradlew :app:testDebugUnitTest` | PASS（1 test） |
| `adb install` → emulator-5554 | Success |
| 启动 `com.shinefs.app` | 进程存活，logcat 无 crash/ANR |
| 首页截图 | `DOCS/assets/cycle00_home.png`（玄黑底，四入口占位，渲染无异常） |

## 7. 环境事实（Windows 本机）

- SDK：`C:\Users\Administrator\AppData\Local\Android\Sdk`（`local.properties` 已配置；环境变量未设置）
- 模拟器：`emulator-5554`，AVD `Medium_Phone`（1080×2400/420dpi，API 37.1，x86_64，WHPX 加速）
- 参考资产：兄弟项目 TAVO-MINI（React Native）——仅复用其 Gradle wrapper（9.3.1）与依赖缓存，代码无复用。

## 8. 已知技术债与风险

| # | 项 | 状态 |
|---|---|---|
| TD-01 | Gradle 提示存在 deprecated 用法（与 Gradle 10 不兼容） | 非阻断，来源待查（AGP/插件） |
| TD-02 | 设计 Token HEX 为占位值 | Cycle 08 前核定 |
| TD-03 | jvmTarget 17 + minSdk 24：app 内使用 java.time 等 API 需 desugaring | 暂避开；core 模块为纯 JVM 不受影响 |
| TD-04 | Lint 未运行（无 lint 基线） | Cycle 01 Check 补齐 |
| TD-05 | 无 Git 仓库 | Cycle 00 Act 中 init |
| TD-06 | 仪器化测试/UI 测试能力未建立 | Cycle 03+ 按需引入 |
