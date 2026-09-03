## [Unreleased]

### Cycle 08 — 视觉统一与动效打磨（2026-09-03）

#### 新增
- 应用图标：adaptive（金环+四隅刻度+朱砂/素金磁针 vector）+ monochrome 主题层 + 传统圆形 PNG×5 密度
- 罗盘专属设计 Token（天池水色/太极象牙/玄墨）；首页标语/卦名/段落标题/定印衬线化（碑刻感）
#### 验证
- 大字体 1.3× 无截断；系统"减少动画"下正常；Lint 警告 27→16（图标类清零）
## [Unreleased]

### Cycle 07 — 卦例与本地数据（2026-09-03）

#### 新增
- Room 持久化（room 2.6.1 + KSP）：卦例表/DAO/schema 导出；仓储接口不变换 Room 实现
- 卦例记录页：日期（今天/近7天）+八场景+收藏筛选；强杀重启数据不丢
- 解卦页卦例管理区：收藏/备注/删除（含确认）
- 全链路 IO 线程化（produceState/rememberCoroutineScope，无主线程 DB 访问）
#### 重要修复
- Room+KSP MissingType：@Database 文件不得混入引用领域类型的顶层函数（拆分 CaseMappers）
- 协程误用 4 处重构（阻塞 DAO/事件回调 LaunchedEffect）
#### 验证
- 持久化 E2E：保存→强杀→重启→历史在；收藏跨重启持久；删除后空态正确；logcat 0 crash
## [Unreleased]

### Cycle 06 — 宅居测局（2026-09-03）

#### 新增
- 宅居测局页：八场景逐项测量（场景预选→定盘→自动起卦直达）、状态与进度、新开测局
- 整宅摘要（纯函数）：按场景去重保留最新、向方五行分布、明示"不做飞星等综合断语"
- Router.replace 语义（修复返回键重复起卦缺陷）；测局会话跨导航保持
- 4 个新单测（累计 76 全绿）
## [Unreleased]

### Cycle 05 — 解卦与原典（2026-09-03）

#### 新增
- 固定八段解卦页（测量结果/卦象结果/原典依据/象义解析/空间解读/宜忌注意/AI 白话/规则版本）
- 原典仓储接口 + fixture 数据（6 卦卦辞象辞，verified=false 显著标注；爻辞不录；未收录卦显式降级提示）
- AI 接口抽象（AiInterpreter + 结构化请求构造[§10.1 字段全覆盖]）+ 离线降级实现（不出空白页）
- 确定性解读引擎（象义=结构事实/空间=五行特质+场景建议/宜忌通则；不做生克吉凶推断，列待决策 D-10）
- 11 个新单测（累计 72 全绿）
## [Unreleased]

### Cycle 04 — 定盘与起卦（2026-09-03）

#### 新增
- 定盘交互：稳定度良好且无磁扰/倾斜才可定盘；朱砂"定"印动画 + 触觉反馈；重新测量
- 可替换规则引擎：FixtureDirectionRule（上卦=向卦[正式]；下卦/动爻临时口径，三重显著标记，D-01~D-05 拍板后替换）
- 场景选择（方案 §9.5 八场景+单项测量）、六爻自下而上生成动画、动爻朱砂高亮+阴阳翻转、本卦→变卦过渡、算法依据卡
- 卦例数据层：DivinationCase/CaseRepository（内存实现，Cycle 07 换 Room）/DivinationService；起卦模式页（B/C 待决策占位）
- 10 个新单测（累计 61 全绿）
#### 验证
- 模拟器 E2E：定盘→场景→起卦→井卦三爻动变坎（结构验算正确）；logcat 0 crash
## [Unreleased]

### Cycle 03 — 动态罗盘 UI（2026-09-03）

#### 新增
- 多层动态罗盘（Canvas）：刻度环/角度数字/八方/二十四山（当前山金弧+亮字高亮）/八卦/五行弧/天池太极/磁针（按稳定度微摆）/顶部朱砂向首指针；最短路径旋盘动画（跨界不绕圈）；减少动画适配
- 首页四入口枢纽 + 手写路由（无导航库依赖）
- 罗盘页：读数/坐向/稳定度/精度/磁扰/姿态/8字校准提示；无磁力计 LIMITED 降级横幅（不伪造方向）；竖屏锁定
- 传感器竖持姿态 remap（AXIS_X/AXIS_Z）
#### 修复
- drawArc 角度基准 90° 偏移（高亮弧/五行弧错位，像素取证确认修复）
- 正常竖持误报告倾斜（姿态基准）、指针压字、卦符过小
## [Unreleased]

### Cycle 02 — 电子罗盘引擎（2026-09-03）

#### 新增
- `:core:compass` 纯 Kotlin 模块：环形数学（归一化/最短路径/环形均值标准差）、EMA 平滑（跨界不绕圈）、稳定度三档、稳定期毛刺抑制、磁场干扰监测、倾斜标记
- `:app` CompassController：Rotation Vector 优先 + 磁力计/加速度计回退、磁力计常开供磁扰检测、start/stop 成对注销、StateFlow 输出；能力判定 FULL/LIMITED（无磁力计不伪造方向）
- 17 个新单测（累计 51 全绿）
# Changelog

本文件记录每个 PDCA Cycle 的对外可见变化。格式参考 Keep a Changelog，版本号随发版周期演进。

## [Unreleased]

### Cycle 01 — 术数数据与演算核心（2026-09-03）

#### 新增
- `:core:yijing` 纯 Kotlin JVM 模块（无 Android/UI/AI 依赖，可独立 JVM 测试）：
  - 八卦模型（先天卦数、爻列、后天方位/角度、五行、象义、亲属）
  - 二十四山（方案 §3.2 原序 + 索引/边界公式）、后天八卦 45° 扇区映射、坐向换算
  - 六十四卦结构表（King Wen 序，8×8 上下卦编码；卦名/卦序待人工复核）
  - 动爻翻转与变卦推导（384 组合测试全覆盖 + 往返一致）
  - 起卦模式 A/B/C 接口（公式待决策 D-01～D-05，未实现）
- 33 个 JVM 单元测试（7 套），全绿
- `DOCS/YIJING_RULES.md`（规则 rules-v0.1：唯一事实源 + 数据核定状态）

#### 修复
- 六十四卦数据：45 萃误录为下兑上坤 → 更正为下坤上兑（被 8×8 覆盖测试逮出）
- Lint 基线建立：lintDebug 0 error（PropertyEscape 对 local.properties 的 Windows 误报已按文件级理由禁用；余 6 条非阻断警告记录为技术债）

#### 验证
- assembleDebug + :app 单测 + :core:yijing 33 测试 + lintDebug 全绿；emulator-5554 重装重启回归通过

### Cycle 00 — 项目基线审计（2026-09-03）

#### 新增
- Android 工程骨架：`:app` 单模块（Kotlin 2.1.20 / AGP 8.12.0 / Gradle 9.3.1 / Compose BOM 2025.08.01 / minSdk 24 / targetSdk 36 / `com.shinefs.app`）
- 基线首页（玄黑壳）：设计 Token 初稿 `ShineColors`、四大入口静态占位（标注建设周期）
- JVM 测试通道（JUnit4）与 `BaselineSmokeTest`
- Gradle wrapper 9.3.1
- 文档体系：`DOCS/ARCHITECTURE.md`、`DOCS/PDCA_LOG.md`、`DOCS/TEST_MATRIX.md`、`DOCS/基线审计报告_Cycle00.md`、`CHANGELOG.md`

#### 验证
- `assembleDebug` 构建通过；单测 1/1 通过；emulator-5554 安装、启动、运行无 crash；首页截图存档 `DOCS/assets/cycle00_home.png`

#### 修复
- 模板 JVM-target 不一致（Java 1.8 vs Kotlin 17）→ 统一 JVM 17
- 模板依赖版本对齐本机 Gradle 缓存（AGP 8.7.3→8.12.0，Kotlin 2.0.21→2.1.20，BOM 2024.12.01→2025.08.01）
