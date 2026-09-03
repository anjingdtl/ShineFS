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
