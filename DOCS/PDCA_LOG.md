# ShineFS PDCA 建设日志（PDCA_LOG）

> 每轮 Cycle 必须按 Plan → Do → Check → Act 完整记录。
> 规则：Check 不通过必须回到 Act 修正后重新 Check；未达验收标准不得进入下一 Cycle。

---

## Cycle 00 — 项目基线审计（2026-09-03）

### Plan

- **Goal**: 全面理解 `F:\ClaudeWorkSpace\projects\ShineFS` 现状，建立可复现的编译/测试/运行基线，形成《基线审计报告》。
- **Current state**:（扫描结论）仓库初始仅含 1 份方案文档 `DOCS/安卓周易风水罗盘_App_产品方案_V1.0_PDCA建设方案.md`；**无 Android 工程、无 Gradle、无源码、无资源、无测试、无 Git 仓库**。兄弟项目 TAVO-MINI 为 React Native，无可复用业务代码（仅 Gradle wrapper 9.3.1 与依赖缓存可复用）。本机模拟器初始未运行。
- **Scope**:
  1. 扫描仓库结构/Git 状态/工具链（SDK、JDK、Gradle 缓存、模拟器）；
  2. 搭建最小 Android 工程骨架（Compose、版本与本机缓存对齐）；
  3. 建立编译基线（assembleDebug）与测试基线（JUnit 通道）；
  4. 模拟器安装、启动、截图取证、logcat 检查；
  5. 产出 `DOCS/ARCHITECTURE.md`、`DOCS/TEST_MATRIX.md`、`DOCS/基线审计报告_Cycle00.md`、`CHANGELOG.md`；
  6. 梳理待决策项清单。
- **Out of scope**: 一切业务功能（罗盘/传感器/术数引擎/数据库/AI）、多模块拆分、视觉体系实现、Lint 基线（留待 Cycle 01 Check）。
- **Files**: 新增 `settings.gradle.kts`、`build.gradle.kts`、`gradle.properties`、`local.properties`、`gradle/libs.versions.toml`、`gradle/wrapper/*`、`gradlew(.bat)`、`app/**`；新增 `DOCS/ARCHITECTURE.md`、`DOCS/TEST_MATRIX.md`、`DOCS/基线审计报告_Cycle00.md`、`DOCS/PDCA_LOG.md`、`CHANGELOG.md`。
- **Risks**:
  - 模板版本组合（AGP 8.7.3/Kotlin 2.0.21）与本机 Gradle 9.3.1 缓存不匹配 → 需对齐版本（已预案：AGP 8.12.0/Kotlin 2.1.20）；
  - 空仓库无既有约束，骨架选择将影响后续所有 Cycle → 采用方案 §17 目标结构的"先单模块、按 Cycle 逐步拆分"策略，避免过度工程；
  - 模拟器由本轮自行启动（初始未运行），与全局记忆"已启动"状态不符，已核实后启动。
- **Acceptance criteria**:
  1. 方案 §14 Cycle 00 Check 九问全部有据可查地回答；
  2. `assembleDebug` 成功且 APK 可安装、可启动、无 crash，截图存档；
  3. JVM 单元测试通道可用且有测试通过；
  4. 三份文档 + CHANGELOG + 审计报告落盘；
  5. 待决策项（含术数规则缺口）成清单，不擅自拍板。

### Do

- **Changed**:
  - `gradle/libs.versions.toml`：模板版本（AGP 8.7.3/Kotlin 2.0.21/BOM 2024.12.01）→ 本机缓存验证组合（AGP 8.12.0/Kotlin 2.1.20/BOM 2025.08.01/core-ktx 1.16.0/activity-compose 1.10.1）；
  - `app/build.gradle.kts`：补 `compileOptions` + `kotlin.compilerOptions`（JVM 17/17），修复 JVM-target 不一致编译失败；补 `testImplementation(libs.junit)`；
  - `app/src/main/res/values/styles.xml`：浅色主题 → 玄黑深色主题（窗口/状态栏/导航栏 #0C0C0F）；
  - `MainActivity.kt`：模板 Hello/Counter → ShineFS 基线首页（玄黑底、金色标语"观方辨位 · 依易起卦"、四入口静态占位并标注建设周期、语义标记供 UI 自动化）。
- **Added**:
  - Gradle wrapper 9.3.1（复用 TAVO-MINI 的 wrapper 文件，distribution 本机已缓存）；
  - `app/src/main/java/com/shinefs/app/ui/theme/ShineColors.kt`：方案 §7.2 设计 Token 初稿（HEX 为占位值，Cycle 08 前核定）；
  - `app/src/test/java/com/shinefs/app/BaselineSmokeTest.kt`：JVM 测试通道基线（1 test，业务测试进入 core 模块后可移除）；
  - `DOCS/ARCHITECTURE.md`、`DOCS/TEST_MATRIX.md`、`DOCS/基线审计报告_Cycle00.md`、`CHANGELOG.md`、本文件；
  - `DOCS/assets/cycle00_home.png`（运行取证截图）。
- **Removed**: 无。
- **Migration**: 无（空仓库起步，无历史数据）。

### Check

- **Build**: `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL**（首次失败于 JVM-target 不一致，Act 修复后通过；修复后增量 16s）。
- **Unit tests**: `:app:testDebugUnitTest` → **PASS**（1/1）。
- **Lint**: 未运行（TD-04，非本轮范围，Cycle 01 Check 补齐）。
- **关键算法测试**: 不适用（无业务算法）。
- **UI**: 首页截图目检通过——玄黑背景、金色标语、四入口占位、底部基线标识；无文字重叠/截断/乱码（截图：`DOCS/assets/cycle00_home.png`）。
- **Runtime**: emulator-5554（Medium_Phone, API 37.1）安装 Success → 启动成功 → 进程存活（pid 5348）→ logcat 无 FATAL/ANR/AndroidRuntime（仅一条系统级 `android.xr` flag 噪音，与本应用无关）。
- **Regression**: 不适用（首个基线）。
- **Problems found**:
  1. 模板缺 compileOptions → 编译失败（已修复）；
  2. Gradle deprecation 警告（不兼容 Gradle 10，TD-01 记录）；
  3. 模拟器未按预期处于运行状态（已核实并自行启动）。

**方案 §14 Cycle 00 九问核对**：

| 问题 | 回答 |
|---|---|
| 当前是否 Compose | 是（BOM 2025.08.01 + Material3 + Kotlin Compose 插件） |
| 最低 Android 版本 | minSdk 24（Android 7.0） |
| targetSdk | 36 |
| 当前架构 | 单模块 Compose 壳（`:app`），目标架构按方案 §17 分阶段演进（core/yijing → core/compass → feature/*） |
| 已有页面 | 1 个：基线首页（占位，无业务交互） |
| 是否有数据库 | 无（Room 计划 Cycle 07） |
| 是否有传感器代码 | 无（计划 Cycle 02） |
| 是否有测试 | 有：JVM 单测通道 + 1 个 smoke test；无仪器化/UI 测试 |
| 是否可正常安装运行 | 是（emulator-5554 验证：安装/启动/存活/无 crash） |

### Act

- **Fixes**:
  1. JVM-target 不一致 → `compileOptions`+`compilerOptions` 统一 JVM 17（重新 Check：构建通过）；
  2. 版本目录对齐本机缓存组合，规避离线构建风险；
  3. Git 仓库初始化并提交基线（TD-05 关闭）。
- **Remaining known issues**: TD-01（Gradle deprecation，非阻断）、TD-02（设计 Token 占位）、TD-03（desugaring 约束）、TD-04（Lint 基线缺失）、TD-06（无 UI/仪器化测试）。详见 `DOCS/ARCHITECTURE.md` §8。
- **Decision**: 本轮**不**创建 `core/yijing` 等空模块（避免无内容的模块化）；自 Cycle 01 起按需创建。仓库文档根沿用既有 `DOCS/`（大写），对应方案中 `docs/`。
- **Next cycle**: Cycle 01 — 术数数据与演算核心（见下方 Plan）。

---

## Cycle 01 — 术数数据与演算核心（YijingCore）

> 状态：**Plan 已立，Do 进行中**（本轮接续执行）

### Plan

- **Goal**: 建立纯 Kotlin、与 Android/UI/AI 完全解耦的 `:core:yijing` 模块：八卦、后天八卦方位、二十四山、六十四卦结构、动爻、变卦、起卦规则接口，并以自动化测试固化（含 64×6=384 变卦全覆盖）。
- **Current state**: Cycle 00 基线（单模块 `:app` 壳，无任何业务代码）。
- **Scope**:
  1. `:core:yijing` 纯 Kotlin JVM 模块（kotlin("jvm")，无 Android 依赖）；
  2. 数据模型：`Trigram`（八卦）、`Mountain`/二十四山、`Hexagram`（六十四卦结构）——按方案 §5 字段裁剪（原典文本字段 Cycle 05 再补）；
  3. 方位映射：azimuth→二十四山（含边界策略）、azimuth→后天八卦、坐向换算；
  4. 卦象演算：上下卦→六十四卦查询、动爻翻转→变卦；
  5. 起卦规则**接口**（模式 A/B/C），具体时间换算公式未定 → 仅接口 + 待决策项；
  6. 测试：八卦 8 项、二十四山 24×边界、坐向 3 例、64 卦结构完整性、384 变卦、往返一致性；
  7. `DOCS/YIJING_RULES.md`：固化已定义规则 + 待决策项清单。
- **Out of scope**: 卦辞/爻辞原典数据（Cycle 05）、起卦公式拍板（待决策）、罗盘传感器（Cycle 02）、UI、数据库、AI。
- **Files**: 新增 `core/yijing/**`、`settings.gradle.kts`（include）、`DOCS/YIJING_RULES.md`；更新 `DOCS/ARCHITECTURE.md`、本文件、`CHANGELOG.md`。
- **Risks**:
  - 六十四卦上下卦结构数据若录入有误将污染全局 → 用结构化测试（8×8 全覆盖、上下卦拆分与六爻一致、King Wen 序唯一性）兜底，并将数据文件标记"待人工核定"；
  - 二十四山数组顺序易错 → 按方案 §3.2 中心角表逐项断言；
  - 新模块引入可能触发依赖下载 → 纯 JVM 模块仅依赖 kotlin-stdlib（已缓存）+ junit（已缓存）。
- **Acceptance criteria**:
  1. `:core:yijing` 无任何 Android/UI/AI 依赖（build.gradle 可证）；
  2. 全部单测通过（含 384 变卦用例）；
  3. `:app` 构建不受影响（回归）；
  4. 二十四山边界（0/7.49/7.5/352.5/359.99 等）有测试且通过；
  5. 未定义的术数公式只留接口与待决策记录，无猜测实现。

（Do / Check / Act 于本轮实施后回填）
