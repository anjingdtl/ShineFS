# ShineFS V2.2 发布记录

> 日期：2026-09-04（Asia/Shanghai）
> 版本：`versionCode 4` / `versionName 2.2`
> applicationId：`com.shinefs.app`

## 1. 本次发布内容

- 桌面 adaptive 图标与罗盘天池中央统一使用 `bagua_core` 资产。
- 品牌核心固定为先天八卦摆位：乾上、坤下，顺时针为乾、兑、离、震、坤、巽、坎、艮。
- 罗盘动态外圈继续使用后天八卦业务方位，避免品牌资产与动态方位混用。
- 八个卦使用真实阴阳爻线绘制，中央太极使用完整阴阳鱼结构。

## 2. 构建与门禁

构建命令：

```text
./gradlew.bat test lintDebug :app:assembleDebug :app:assembleRelease --offline
```

Release APK：`app/build/outputs/apk/release/app-release.apk`

SHA-256：`5FAF20B3E6ED853A2EB2A63CC75627984A4E566B60E3FF11200CF58ADEBA8A32`

Release 构建使用工作机既有正式签名环境变量完成签名，APK Signature Scheme v2 校验通过；签名密钥与 APK 均不进入 Git。

## 3. 验收边界

- JVM 测试、Lint、Debug 构建和 Release 构建通过。
- 模拟器已核验桌面图标与罗盘中央图标实际渲染。
- 当前仍只有 `emulator-5554`，物理 Android 真机磁场精度、跨姿态误差和无磁力计降级继续保持待验收状态。
