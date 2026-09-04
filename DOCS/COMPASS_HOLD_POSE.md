# ShineFS V2.1 罗盘持握姿态与坐标契约

本文档是 Cycle 11C 的实现契约，约束 `core:compass` 与 Android 传感器接线层的姿态判断、方位解析和定盘门禁。它不改变《周易》起卦公式，也不依赖网络、GPS 或品牌黑名单。

## 1. 事实定义

- **平放**：手机屏幕法向量接近世界竖直轴；手机顶部的水平投影定义为“向”。
- **竖持**：手机屏幕法向量接近水平轴；仍以手机顶部的水平投影定义为“向”。
- **过渡**：正在从一种持握姿态切换到另一种姿态，或尚未达到稳定等待时长。
- **无效**：角度/重力/屏幕法向量不可用，或在很短时间内发生大幅姿态跳变。

姿态是定盘前的输入质量事实，不是对方位结果的修正项。平放和竖持不得通过增加固定 90° 或 180° 偏移来“看起来一致”。

## 2. HoldPoseDetector

`HoldPoseDetector` 先使用 rotation matrix 得到的屏幕法向量和顶部/右侧姿态角；无法得到法向量时回退到 `hypot(pitch, roll)`。重力模长不在 `[6, 14] m/s²`、输入非有限、屏幕向下，或发生快速大幅变化时输出 `INVALID`。

当前默认门限：

| 状态 | 进入 | 退出/转移 | 说明 |
|---|---:|---:|---|
| 平放 | 倾角 ≤20° | 倾角 >30° | 以屏幕法向量接近竖直为优先 |
| 竖持 | 倾角 ≥70° | 倾角 <60° | 以屏幕法向量接近水平为优先 |
| 过渡 | 20°–70° 或候选未稳定 | — | 候选姿态连续保持 800ms 后提交 |
| 无效 | 数据无效/屏幕朝下/剧烈跳变 | 数据恢复后重新候选 | 250ms 内倾角变化 ≥45° 立即打断候选 |

已有姿态采用退出阈值，形成迟滞，避免在边界附近反复抖动。定盘 readiness 还要求姿态已提交并持续满足稳定、磁场和传感器精度门禁。

## 3. 坐标体系与 Display Rotation

自然传感器坐标约定为：`+X=设备右侧`、`+Y=手机顶部`、`+Z=屏幕朝外`。Android rotation matrix 为 3×3 row-major，列向量表示自然设备轴在世界坐标中的方向；世界水平面使用 East/North 分量。

Display Rotation 只改变显示坐标的 right/top 轴选取，不改变物理设备的“顶部=向”定义：

| `Surface.ROTATION_*` | 显示 right（自然轴） | 显示 top（自然轴） |
|---:|---|---|
| 0° | `+X` | `+Y` |
| 90° | `−Y` | `+X` |
| 180° | `−X` | `−Y` |
| 270° | `+Y` | `−X` |

`OrientationMath` 按该映射取 display top 的世界向量，将其投影到水平面后用 `atan2(topEast, topNorth)` 求方位；pitch/roll 和屏幕法向量只用于姿态及质量判断。平放与竖持 resolver 共享这条物理方向定义，仅由 `HoldPoseDetector` 决定是否允许进入相应姿态门禁。

## 4. 定盘快照

点击定盘时，`CompassController.captureSnapshot()` 只复制当前 StateFlow 中已经存在的传感器状态，产生不可变 `LockedCompassSnapshot`。快照包括：

- 捕获 instant、raw/smoothed azimuth、Display Rotation；
- HoldPose、置信度、姿态稳定时长、pitch/roll；
- 稳定性、稳定标准差、朝向与磁力计精度；
- 磁场强度、磁扰状态、北向参考；
- 坐山/向山、方位卦、采样数和突跳抑制事实。

`YijingSpaceContextFactory.fromLockedCompassSnapshot()` 直接读取这些字段。生产定盘链不得新建 `CompassEngine`、重复喂角度或人工构造 `CompassState`；旧的 `fromLegacyReading` 只服务兼容测试/旧导航输入。

## 5. 验证与真机记录

JVM 属性测试使用多方向、四种 Display Rotation 分别构造同一物理顶部方向的平放/竖持 rotation matrix，并断言最短环形误差 ≤5°。真机验证需在保持同一手机顶部指向的条件下，分别平放和竖持采样；每种姿态在 readiness 自动通过后记录 snapshot 的姿态、pitch/roll、精度、磁场和误差。磁铁、金属桌面、无线充电器等干扰源应单独验证为提示并禁止定盘。

真机证据与未完成项统一记录在 [`REAL_DEVICE_TEST.md`](REAL_DEVICE_TEST.md)；在没有实际设备证据前，不能把模拟器结果写成真机通过。
