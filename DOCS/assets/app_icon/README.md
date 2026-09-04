# ShineFS 八卦视觉资产

## 唯一口径

`shinefs_app_icon.svg` 是评审和导出的源稿；Android 运行时使用 `app/src/main/res/drawable/bagua_core.xml`，桌面 adaptive 图标和罗盘天池中央均引用同一份矢量资产。品牌资产采用先天图位：从图面上方顺时针为乾、兑、离、震、坤、巽、坎、艮；`bagua_monochrome.xml` 只负责系统单色主题，但沿用完全相同的卦位和爻线几何。

罗盘外圈不再使用字体里的 Unicode 卦符，而是通过 `BaguaAsset.kt` 读取核心 `Trigram.lines` 绘制真实阴阳爻线，并继续使用后天图位：从正北顺时针为坎、艮、震、巽、离、坤、兑、乾。这样品牌标识与测向业务各自固定，不会把两套卦位混成一张图。

## 固定规范

- 品牌图标采用先天八卦；从图面上方开始顺时针：乾、兑、离、震、坤、巽、坎、艮（乾上、坤下）。
- 罗盘业务外圈采用后天八卦；从正北开始顺时针：坎、艮、震、巽、离、坤、兑、乾。
- 每一卦为三爻，数据自下而上；阳爻为整线，阴爻为中断线。
- 玄黑/天池墨色为底，古铜金为轮廓，亮金为卦爻，象牙白与玄墨组成中央太极。
- 外圈和天池核心均保持圆心对齐；适配图标的有效图形留在 108 视口的安全区内。
- 旧版“八方重复同一组爻线”的实现不再作为资产来源。

## 资源关系

```text
shinefs_app_icon.svg
        └── bagua_core.xml
              ├── ic_launcher_foreground.xml → 桌面 adaptive 图标
              └── CompassDial.kt              → 罗盘天池中央
```

低于 Android 8 的五档 `mipmap-*` PNG 也按同一几何重新栅格化；方形和圆形 fallback 保持同稿，避免系统回退时出现两套图标。可用 `tools/render_bagua_assets.ps1` 重建这些 PNG。
