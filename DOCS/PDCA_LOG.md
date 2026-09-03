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

> 状态：**已完成并关闭**（2026-09-03）

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

### Do

- **Changed**:
  - `settings.gradle.kts`：include `:core:yijing`；`build.gradle.kts`（root）：注册 kotlin-jvm 插件；`gradle/libs.versions.toml`：新增 kotlin-jvm 插件项；
  - `app/build.gradle.kts`：新增 `lint { disable += "PropertyEscape" }`（见 Check/Act）；
  - `local.properties`：SDK 路径改为属性文件规范转义写法（本地文件，不入库）。
- **Added**:
  - `:core:yijing` 纯 Kotlin JVM 模块（仅依赖 kotlin-stdlib + junit，**无 Android/UI/AI 依赖**，结构性保证）：
    - `model/Trigram.kt`：八卦枚举（先天卦数=声明序，爻列自下而上，后天方位/角度/五行/象义/亲属/关键词）；
    - `model/Hexagram.kt`：六十四卦模型（King Wen 序 + 卦名 + 上下经卦；六爻与 Unicode 卦符派生；**原典文本字段有意缺席**，待 D-09/Cycle 05）；
    - `rules/Azimuths.kt`：方位角合法域校验（[0,360)、拒 NaN，fail-fast）；
    - `rules/Mountains24.kt`：二十四山（方案 §3.2 原序 + 索引公式 + 边界派生）；
    - `rules/LaterHeavenBagua.kt`：后天八卦 45° 扇区映射（与二十四山双实现交叉验证）；
    - `rules/Orientation.kt`：坐向换算（向/坐/向卦/坐卦/五行）；
    - `rules/HexagramOps.kt`：本卦查询、动爻翻转、变卦推导；
    - `divination/DivinationRules.kt`：起卦结果模型 + 模式 A/B/C 接口（**公式未拍板，无实现**）；
    - `data/Hexagrams.kt`：六十四卦结构表（Agent 录入 + 自动化结构测试，待人工复核）。
  - 测试 7 套 33 用例：八卦属性/互逆、二十四山（24 中心 + 上下边界 + 跨 0° + 方案临界角 + 非法输入）、后天八卦（方案 §12.1 边界 + 山卦领属交叉）、坐向（方案用例 + §10.1 示例 182.4°→向午坐子离火 + 对向互验）、六十四卦（64 序唯一/卦名唯一/8×8 全覆盖/六爻拆分/卦符码位/15 个锚点卦）、**变卦 384 组合全覆盖 + 往返一致 + 既济三爻动变屯（方案 §10.1 示例）**、起卦结果确定性。
  - `DOCS/YIJING_RULES.md`：规则 rules-v0.1 固化（含数据核定状态与待决策清单）。
- **Removed**: 无。
- **Migration**: 无（新增模块，无既有数据）。

### Check

- **Build**: `./gradlew assembleDebug`（:app + :core:yijing）→ **BUILD SUCCESSFUL**。
- **Unit tests**: `:core:yijing:test` 33/33 **PASS**（含 384 变卦）；`:app:testDebugUnitTest` 1/1 PASS。
- **Lint**: `:app:lintDebug` 首跑报 1 error（`local.properties` PropertyEscape）——按建议转义后**仍报**，判定为 AGP Lint 在 Windows 上对已正确转义文件的已知误报（cat -A 核实内容与建议完全一致）；因该文件不入库、不随 APK 发布，仅对该单条检查 disable（注释注明理由）。复跑 → **0 error**，余 6 条非阻断警告（MissingApplicationIcon×1、NewerVersionAvailable×3、GradleDependency×1、OldTargetApi×1）记录为 TD-07。
- **关键算法测试**: 384 变卦 + 边界临界角 + 8×8 全覆盖全部通过；方案 §10.1 示例（既济三爻动→屯、182.4° 向午坐子）双验算通过。
- **UI**: 本轮未改 UI（核心模块未接线至 app），无 UI 回归面。
- **Runtime**: APK 重装 emulator-5554 Success → 重启 → 进程存活（pid 6181）→ 无 crash。
- **Regression**: `:app` 构建/测试/运行不因新模块受影响（全量任务同跑通过）。
- **Problems found**（本轮 Check 抓到并已 Act）:
  1. **六十四卦数据错误**：45 萃误录为"下兑上坤"（与 19 临重复，8×8 覆盖缺"下坤上兑"）→ 修正为下坤上兑，测试转绿——证明"结构性测试兜底录入错误"的必要性，**人工复核要求维持不撤**（TD-08）；
  2. 测试自身笔误一处（15° 期望值误写为丑，实为癸中心角）→ 已修正；
  3. `String.code` 不存在（应为 `symbol[0].code`）→ 编译期修复；
  4. Lint PropertyEscape 误报（见上）。

### Act

- **Fixes**: 上述 4 项全部修复并复检通过（构建+33 测试+Lint 0 error+模拟器回归）。
- **Remaining known issues**: TD-01（Gradle deprecation）、TD-02（设计 Token 占位）、TD-03（desugaring 约束）、TD-06（无 UI/仪器化测试）、TD-07（Lint 6 条非阻断警告，含无应用图标）、TD-08（六十四卦卦名/卦序待人工复核，见 YIJING_RULES §7）。TD-04（Lint 基线）本轮关闭：lintDebug 0 error 已入 Check 流程。
- **Decision**:
  - 起卦公式（D-01～D-05）维持"接口-only"，未猜测任何口径；
  - 原典文本不进 Cycle 01 模型（避免占位文本冒充经典）；
  - `:core:yijing` 不被 `:app` 依赖（接线发生在有真实消费方的 Cycle 03/04，避免无意义依赖）。
- **Next cycle**: Cycle 02 — 电子罗盘引擎（SensorManager、Rotation Vector 优先 + 磁力计/加速度计回退、方位角、环形平滑、稳定检测、精度与磁干扰提示、生命周期管理；验收：0/360° 无跳变、退出注销 listener、无泄漏）。

**验收判定：Cycle 01 达标（规则可脱离 Android 单独测试 ✓ 无 AI 依赖 ✓ 无 UI 依赖 ✓ 384 变卦测试 ✓），Cycle 01 关闭。**
