# Changelog

本文件记录每个 PDCA Cycle 的对外可见变化。格式参考 Keep a Changelog，版本号随发版周期演进。

## [Unreleased]

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
