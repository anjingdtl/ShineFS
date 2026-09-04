# ShineFS V2.0 完全离线周易时空演算 App 完整建设方案

> 项目名称：ShineFS  
> 本地仓库：`E:\AiWorkSpace\ShineFS`  
> 平台：Android  
> 产品定位：完全离线运行的周易时空演算工具  
> 版本目标：V2.0  
> 建设方式：PDCA 持续闭环 + 每 Cycle 独立 Git Commit  
> 核心原则：**100% 本地确定性演算、0 AI 依赖、0 网络依赖、0 随机规则、所有术数规则可追溯、可复算、可测试、可版本化。**

---

# 0. V2.0 总体目标

ShineFS V2.0 不再以“电子罗盘 UI”为中心，而以**周易 / 易数确定性演算核心**为第一核心。

产品最终必须能够在无网络、无 AI、飞行模式、无任何云端服务和第三方 API 的情况下，独立完成：

```text
传统历法换算
↓
十二时辰识别
↓
电子罗盘定盘
↓
二十四山 / 后天八卦 / 坐向
↓
选择起卦体系
↓
确定性起卦
↓
本卦
↓
互卦
↓
体用
↓
动爻
↓
变卦
↓
五行关系
↓
时令 / 卦气
↓
空间方应
↓
周易原典
↓
本地规则释义
↓
保存卦例与复算
```

V2.0 的正式产品定义：

> **ShineFS 是一款完全离线运行的周易时空演算工具，以《周易》经传为卦爻正典，以梅花易数传统为确定性起卦方法，以中国传统历法提供年月日时数据，以手机电子罗盘提供空间方位与方应数据，并由本地规则引擎完成本卦、互卦、体用、动爻、变卦及白话释义。**

---

# 1. V2.0 最高原则

## 1.1 100% 完全离线

正式版不得依赖 OpenAI、ChatGPT、Claude、Gemini、GLM、任意 LLM、任意云端解卦服务、联网历法 API 或联网规则服务。

V2.0 核心功能必须在飞行模式下完整工作。正式 Android Manifest 原则上不申请：

```text
android.permission.INTERNET
android.permission.ACCESS_NETWORK_STATE
```

除非未来有明确独立扩展，但不得成为核心依赖。

## 1.2 AI 从正式架构中移除

现有 `AiInterpreter`、`OfflineAiInterpreter` 不再属于正式 V2.0 架构。

处理方式：

- 从生产流程中删除；
- 不再显示“AI 白话解读”；
- 原栏目改为“本地白话释义 / 规则释义 / 象义解读”；
- 解释全部由 `InterpretationCore` 基于确定性规则生成。

## 1.3 演算与解释彻底分层

```text
事实层
↓
演算层
↓
规则关系层
↓
解释层
```

任何 UI 或解释逻辑都不得修改时间上下文、罗盘角度、二十四山、上下卦、本卦、动爻、互卦、变卦、体用和五行关系。

## 1.4 不混术数体系

必须明确区分：

### 《周易》经传

负责六十四卦、卦名、卦序、卦辞、爻辞、彖、象、八卦基础象义。

### 梅花易数传统

负责年月日时起卦、物象起卦、后天端法、体用、互卦、五行生克、时令旺衰、方应。

### 罗经 / 风水体系

负责二十四山、坐向、罗盘测向、空间方位。

禁止把后世术数公式写成“《周易》原文规定”。

---

# 2. 文献与规则分级

## 2.1 A 级：周易正典

正式原典库优先依据：

1. 《周易》
2. 《说卦传》
3. 《系辞传》
4. 《彖传》
5. 《象传》
6. 王弼、韩康伯注 / 孔颖达疏《周易正义》
7. 《阮元校刻十三经注疏》本《周易正义》

用途：64卦、64卦辞、384爻辞、64彖传、64大象、384小象、乾用九、坤用六、八卦象义。

任何正式原典数据必须带：

```text
sourceId
edition
verified
checksum
```

## 2.2 B 级：宋代象数传统

用于先天数：

```text
乾1
兑2
离3
震4
巽5
坎6
艮7
坤8
```

代码名称统一为 `XIANTIAN_NUMBER`。

## 2.3 B 级：《梅花易数》传统

正式用于年月日时起卦、物象起卦、后天端法、动爻、互卦、体用、五行生克、旺衰、方应。

产品及源码表述统一建议为：

> “梅花易数传统 / 邵氏象数传统”

不要写成“《周易》唯一正统起卦法”。

## 2.4 C 级：罗经 / 地理术数

用于二十四山、坐向、空间定位和罗盘显示。

```text
壬 子 癸
丑 艮 寅
甲 卯 乙
辰 巽 巳
丙 午 丁
未 坤 申
庚 酉 辛
戌 乾 亥
```

每山 15°。禁止未经明确文献规则，将“山”直接换成数字参与梅花起卦。

---

# 3. 总体架构

```text
                         ShineFS Offline Core
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
  CalendarCore               CompassCore              ClassicCorpus
  传统历法 / 时辰             电子罗盘 / 二十四山        周易原典
        │                         │                         │
        └──────────────┬──────────┘                         │
                       │                                    │
               YijingMomentContext                          │
                 时间 + 空间                                │
                       │                                    │
                  DivinationCore ────────────────────────────┘
                       │
           ┌───────────┼───────────┐
           │           │           │
         本卦         互卦         体用
           │           │           │
           └───────────┼───────────┘
                       │
                     动爻
                       │
                     变卦
                       │
              五行 / 时令 / 方应
                       │
              InterpretationCore
                本地规则解释器
                       │
                 Offline Report
```

---

# 4. 推荐模块结构

```text
ShineFS/
├─ app/
│  ├─ ui/
│  ├─ nav/
│  ├─ data/
│  └─ report/
├─ core/
│  ├─ yijing/
│  │  ├─ model/
│  │  ├─ hexagram/
│  │  ├─ nuclear/
│  │  ├─ tiyong/
│  │  └─ element/
│  ├─ calendar/
│  │  ├─ ChineseCalendarProvider
│  │  ├─ Shichen
│  │  ├─ HeavenlyStem
│  │  ├─ EarthlyBranch
│  │  ├─ Ganzhi
│  │  ├─ SolarTerm
│  │  └─ policies/
│  ├─ compass/
│  │  ├─ sensor/
│  │  ├─ engine/
│  │  ├─ mountain24/
│  │  └─ orientation/
│  ├─ divination/
│  │  ├─ MeihuaTimeRule
│  │  ├─ MeihuaPostHeavenRule
│  │  ├─ DivinationResult
│  │  ├─ RuleManifest
│  │  └─ CalculationTrace
│  ├─ classics/
│  │  ├─ ZhouyiCorpus
│  │  ├─ SourceRef
│  │  └─ TextVariant
│  └─ interpretation/
│     ├─ HexagramInterpreter
│     ├─ LineInterpreter
│     ├─ TiYongInterpreter
│     ├─ ElementInterpreter
│     ├─ SeasonalInterpreter
│     └─ SpatialResponseInterpreter
└─ DOCS/
   ├─ PRODUCT_V2.md
   ├─ ARCHITECTURE.md
   ├─ YIJING_RULES.md
   ├─ SOURCE_CATALOG.md
   ├─ RULE_MANIFEST.md
   ├─ PDCA_LOG.md
   ├─ TEST_MATRIX.md
   ├─ REAL_DEVICE_TEST.md
   └─ CHANGELOG.md
```

若现有架构已经成熟，不强制一次性大重构，允许按 Cycle 小步迁移。

---

# 5. CalendarCore：时间演算一级核心

时间不再只是普通 UI 字段，而是正式演算输入。

建议：

```kotlin
data class YijingTimeContext(
    val instant: Instant,
    val zoneId: ZoneId,
    val localDateTime: LocalDateTime,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val leapMonth: Boolean,
    val yearStem: HeavenlyStem?,
    val yearBranch: EarthlyBranch,
    val monthBranch: EarthlyBranch?,
    val dayGanzhi: Ganzhi?,
    val hourBranch: EarthlyBranch,
    val yearBranchNumber: Int,
    val lunarMonthNumber: Int,
    val lunarDayNumber: Int,
    val hourBranchNumber: Int,
    val solarTerm: SolarTerm?,
    val calendarVersion: String,
    val dayBoundaryPolicy: DayBoundaryPolicy,
    val leapMonthPolicy: LeapMonthPolicy
)
```

## 5.1 十二时辰

| 时辰 | 民用时间 | 时数 |
|---|---|---:|
| 子 | 23:00–00:59 | 1 |
| 丑 | 01:00–02:59 | 2 |
| 寅 | 03:00–04:59 | 3 |
| 卯 | 05:00–06:59 | 4 |
| 辰 | 07:00–08:59 | 5 |
| 巳 | 09:00–10:59 | 6 |
| 午 | 11:00–12:59 | 7 |
| 未 | 13:00–14:59 | 8 |
| 申 | 15:00–16:59 | 9 |
| 酉 | 17:00–18:59 | 10 |
| 戌 | 19:00–20:59 | 11 |
| 亥 | 21:00–22:59 | 12 |

正式算法禁止直接使用 `hourOfDay` 参与梅花起卦，必须先换算成时辰数。

## 5.2 中国传统历法

Android minSdk 24 可封装 `android.icu.util.ChineseCalendar`，但业务层不得直接访问 Android ICU。

```kotlin
interface ChineseCalendarProvider {
    fun resolve(instant: Instant, zoneId: ZoneId): ChineseDate
}
```

JVM 测试允许注入 FakeProvider。

## 5.3 闰月政策

```kotlin
enum class LeapMonthPolicy {
    SAME_MONTH_NUMBER
}
```

默认闰六月仍取月数 6，但必须在规则来源中注明这是显式工程政策，而非伪装成梅花原文。

## 5.4 子时换日

```kotlin
enum class DayBoundaryPolicy {
    CIVIL_MIDNIGHT,
    ZI_HOUR_START_23
}
```

V2.0 默认 `CIVIL_MIDNIGHT`。高级模式可保留 23:00 换日，但切换后必须重新演算并记录规则版本。

## 5.5 节气

节气进入 `YijingTimeContext`，但不直接进入 V2.0 时间起卦公式。用于时令、旺衰和后续月令扩展。农历月数与节气月令必须分字段。

---

# 6. CompassCore：空间一级核心

## 6.1 原始数据

必须保留：

```text
rawAzimuth
smoothedAzimuth
sensorAccuracy
magneticMagnitude
magneticInterference
stability
pitch
roll
northReference
```

## 6.2 二十四山

```text
index = floor(((azimuth + 7.5) % 360) / 15)
```

中心角：

```text
子0 癸15 丑30 艮45 寅60 甲75
卯90 乙105 辰120 巽135 巳150 丙165
午180 丁195 未210 坤225 申240 庚255
酉270 辛285 戌300 乾315 亥330 壬345
```

## 6.3 后天八卦

```text
坎北 艮东北 震东 巽东南
离南 坤西南 兑西 乾西北
```

山领属：

```text
坎：壬子癸
艮：丑艮寅
震：甲卯乙
巽：辰巽巳
离：丙午丁
坤：未坤申
兑：庚酉辛
乾：戌乾亥
```

## 6.4 坐向

```text
facing = azimuth
sitting = (azimuth + 180) % 360
```

UI 必须明确手机顶部 / 约定测量方向代表“向”。

## 6.5 磁北 / 真北

```kotlin
enum class NorthReference {
    MAGNETIC,
    TRUE
}
```

V2.0 默认 `MAGNETIC`。如果以后支持真北，必须显式标注并保存 northReference。

## 6.6 当前罗盘专项整改

必须修复：

1. 多圈连续旋转时 `shortestDiff` 与累计角度组合产生的负余数 / 大角度回跳风险；
2. Rotation Vector 与 Magnetic Field 的 accuracy 不得互相覆盖；
3. 新增连续顺时针 5 圈、连续逆时针 5 圈、正反交替自动化测试。

建议：

```kotlin
data class SensorAccuracyState(
    val orientationAccuracy: SensorAccuracy,
    val magneticAccuracy: SensorAccuracy
)
```

---

# 7. YijingMomentContext

整个系统统一使用：

```kotlin
data class YijingMomentContext(
    val time: YijingTimeContext,
    val space: YijingSpaceContext?,
    val event: DivinationEvent?,
    val capturedAt: Instant
)
```

```kotlin
data class YijingSpaceContext(
    val rawAzimuth: Float?,
    val smoothedAzimuth: Float?,
    val northReference: NorthReference,
    val facingMountain: Mountain24?,
    val sittingMountain: Mountain24?,
    val directionTrigram: Trigram?,
    val sensorAccuracy: SensorAccuracyState?,
    val stable: Boolean,
    val magneticInterference: Boolean
)
```

---

# 8. 正式起卦模式 A：梅花年月日时

正式类：`MeihuaTimeDivinationRuleV1`

输入：年支数、农历月数、农历日数、时辰数。

年支数：

```text
子1 丑2 寅3 卯4 辰5 巳6
午7 未8 申9 酉10 戌11 亥12
```

八卦先天数：

```text
乾1 兑2 离3 震4
巽5 坎6 艮7 坤8
```

公式：

```text
base = 年支数 + 农历月数 + 农历日数
上卦数 = base 除8取余
下卦数 = (base + 时辰数) 除8取余
动爻   = (base + 时辰数) 除6取余
```

统一余数函数：

```kotlin
fun normalize8(n: Int): Int = ((n - 1) % 8) + 1
fun normalize6(n: Int): Int = ((n - 1) % 6) + 1
```

---

# 9. 正式起卦模式 B：梅花后天端法

正式类：`MeihuaPostHeavenObjectDirectionRuleV1`

规则：

```text
物象 → 上卦
方位 → 下卦
物卦先天数 + 方位卦先天数 + 时辰数 → 动爻
```

例如：

```text
老人 → 乾1
巽方 → 巽5
卯时 → 4
1+5+4=10
10除6余4
```

得天风姤，四爻动。

物象必须来自版本化类象表，不允许 AI 或自由文案擅自配卦。

---

# 10. 正式起卦模式 C：时间卦 + 罗盘方应

建议作为 ShineFS 默认“时空合参”模式。

```text
年月日时
↓
时间起卦
↓
本卦 / 互卦 / 体用 / 动爻 / 变卦
```

同时：

```text
罗盘
↓
二十四山
↓
后天八卦
↓
坐向
↓
空间方应
```

二者合参，但空间数据**不修改时间卦**。

---

# 11. 暂不默认开放的模式

## 11.1 大衍筮法

以后基于《系辞》独立实现 `ClassicYarrowRule`，不得与梅花混合。

## 11.2 数字起卦

文献冻结后单独实现 `MeihuaNumberRule`。

---

# 12. 本卦、互卦、动爻、变卦

## 12.1 本卦

由下卦 + 上卦唯一确定，保持 8×8 全覆盖。

## 12.2 动爻

1=初爻，6=上爻。

## 12.3 变卦

只翻动爻，继续保留 64×6=384 全量测试。

## 12.4 互卦

```text
下互 = 原卦第2、3、4爻
上互 = 原卦第3、4、5爻
```

```kotlin
data class NuclearHexagram(
    val lower: Trigram,
    val upper: Trigram,
    val hexagram: Hexagram
)
```

---

# 13. 互卦冲突治理

不同文本存在“乾坤无互”等差异，不得私下写死。

```kotlin
enum class NuclearPolicy {
    STANDARD_234_345,
    LEGACY_QIAN_KUN_NO_NUCLEAR
}
```

V2.0 默认 `STANDARD_234_345`，但必须在 RuleManifest 中标明。

---

# 14. 体用

```text
动爻1~3 → 下卦为用，上卦为体
动爻4~6 → 上卦为用，下卦为体
```

```kotlin
data class TiYong(
    val ti: Trigram,
    val yong: Trigram,
    val movingPart: MovingPart
)
```

---

# 15. 五行生克

八卦五行：

```text
乾兑金
震巽木
坎水
离火
坤艮土
```

相生：金生水、水生木、木生火、火生土、土生金。  
相克：金克木、木克土、土克水、水克火、火克金。

```kotlin
enum class ElementRelation {
    TI_GENERATES_YONG,
    YONG_GENERATES_TI,
    TI_CONTROLS_YONG,
    YONG_CONTROLS_TI,
    SAME
}
```

核心只负责判定事实关系，不直接输出绝对吉凶。

---

# 16. 时令 / 卦气

结构化加入：

```text
春：震巽木旺
夏：离火旺
秋：乾兑金旺
冬：坎水旺
辰戌丑未：坤艮土旺
```

建议：

```kotlin
data class SeasonalQiContext(
    val season: Season,
    val dominantElement: Element?,
    val sourceRuleId: String
)
```

农历月数用于起卦，月令 / 时令用于旺衰解释，两者不得混用。

---

# 17. ClassicCorpus：周易原典库

现有 Fixture 必须从生产路径移除。

正式至少包含：

```text
64卦名
64卦辞
64彖
64大象
384爻辞
384小象
乾用九
坤用六
```

```kotlin
data class CanonicalHexagramText(
    val kingWenOrder: Int,
    val name: String,
    val judgment: String,
    val tuan: String?,
    val greatImage: String?,
    val lines: List<CanonicalLineText>,
    val specialUseText: String?,
    val sourceEdition: String,
    val sourceId: String,
    val verified: Boolean,
    val checksum: String
)
```

```kotlin
data class CanonicalLineText(
    val line: Int,
    val text: String,
    val smallImage: String?
)
```

原典建议使用静态 assets JSON / protobuf；Room 专门保存用户数据，不与原典混库。

---

# 18. 原典核定流程

```text
底本选定
↓
结构化录入
↓
第二来源交叉核验
↓
异文记录
↓
人工核定
↓
verified=true
↓
checksum
```

`verified=false` 的数据不得显示“已核定原典”。

---

# 19. RuleManifest

每一条正式规则必须能回答“从哪里来”。

```kotlin
data class RuleManifest(
    val ruleId: String,
    val version: String,
    val system: RuleSystem,
    val sourceRefs: List<SourceRef>,
    val assumptions: List<String>,
    val status: RuleStatus
)
```

例如：

```text
ruleId: meihua-time-v1
system: MEIHUA_YISHU_TRADITION
source: 《梅花易数》年月日时起例
assumptions: 闰月同月号；民用午夜换日
status: VERIFIED_WITH_EXPLICIT_ASSUMPTIONS
```

---

# 20. CalculationTrace

所有演算必须生成可复算轨迹，例如：

```text
辰年 → 5
十二月 → 12
十七日 → 17
申时 → 9
5+12+17 = 34
34 mod 8 = 2 → 兑
34+9 = 43
43 mod 8 = 3 → 离
43 mod 6 = 1 → 初爻
兑上离下 → 泽火革
初爻变 → 泽山咸
```

Trace 用于 UI、测试、Bug 定位、历史复算和规则迁移。

---

# 21. DivinationResult

```kotlin
data class DivinationResult(
    val rule: RuleManifest,
    val upperTrigram: Trigram,
    val lowerTrigram: Trigram,
    val original: Hexagram,
    val changingLine: Int,
    val changed: Hexagram,
    val nuclear: NuclearHexagram?,
    val tiYong: TiYong?,
    val elementRelation: ElementRelation?,
    val seasonalQi: SeasonalQiContext?,
    val timeContext: YijingTimeContext,
    val spaceContext: YijingSpaceContext?,
    val trace: CalculationTrace
)
```

---

# 22. InterpretationCore：完全本地解释

```text
InterpretationCore
├─ HexagramInterpreter
├─ LineInterpreter
├─ TiYongInterpreter
├─ ElementInterpreter
├─ SeasonalInterpreter
├─ SpatialResponseInterpreter
└─ AdvisoryComposer
```

解释必须由“规则模板 + 结构化变量”生成。

例如：

```text
体卦兑金
用卦离火
火克金
→ 用克体
```

模板：

> 用卦克制体卦，表示外部条件对主体形成较明显影响。

再叠加时令、互卦、变卦、动爻位置和空间方应。

---

# 23. 禁止自由生成

正式产品禁止：

```text
Random
LLM
概率生成
当前毫秒取模
UUID取数
随机吉凶
随机文案
```

相同输入 + 相同规则版本 + 相同历法政策，必须得到完全相同的演算结果和规则解释。

---

# 24. 结果页结构

正式报告固定九段：

```text
一、时空数据
二、起卦过程
三、卦象结果
四、周易原典
五、互卦与体用
六、五行与时令
七、方位与方应
八、本地白话释义
九、规则来源与版本
```

必须显式显示：起卦体系、经典体系、空间体系、历法、日界、北向、规则版本、原典版本。

---

# 25. 古例金标准测试

## 25.1 观梅占

```text
辰年=5
十二月=12
十七日=17
申时=9
5+12+17=34 → 兑
34+9=43 → 离
43除6余1 → 初爻
```

结果：泽火革，初爻动，变卦咸。

## 25.2 牡丹占

```text
巳年=6
三月=3
十六日=16
卯时=4
6+3+16=25 → 乾
25+4=29 → 巽
29除6余5 → 五爻
```

结果：天风姤，五爻动，变卦鼎。

## 25.3 后天端法

至少回放：老人有忧色、少年有喜色、牛哀鸣。

---

# 26. 自动化测试矩阵

## 周易核心

- 8经卦完整；
- 64卦完整；
- King Wen 序唯一；
- 8×8上下卦全覆盖；
- 384爻；
- 384变卦；
- 翻转两次回原卦；
- 卦符正确。

## 互卦

64 卦全覆盖 234 / 345。

## 体用

384 组合全覆盖。

## 时间

至少覆盖：子时、丑时、亥时、22:59、23:00、23:59、00:00、正月、十二月、初一、三十、春节跨年、闰月、时区、日界策略。

## 罗盘

至少覆盖：0/360、359→1、1→359、24山中心、24山边界、坐向、多圈顺逆时针、快速转动、稳定检测、磁干扰、倾斜、无传感器。

## 原典

必须验证：64卦、384爻、用九、用六、sourceId、verified、checksum、数据完整性。

---

# 27. 完全离线验收 Offline Gate

测试步骤：开启飞行模式、关闭 Wi-Fi、关闭移动网络、清除 App 数据、首次启动。

完整执行：

```text
首页
↓
历法
↓
罗盘
↓
定盘
↓
起卦
↓
本卦
↓
互卦
↓
体用
↓
变卦
↓
原典
↓
白话释义
↓
保存历史
↓
杀进程
↓
重新启动
↓
读取历史
```

必须全部正常。

---

# 28. 用户卦例持久化

Room 必须保存至少：

```text
timestamp
zoneId
calendarVersion
ruleId
ruleVersion
classicCorpusVersion
dayBoundaryPolicy
leapMonthPolicy
northReference
rawAzimuth
smoothedAzimuth
lunarYear
lunarMonth
lunarDay
leapMonth
yearBranch
hourBranch
upperTrigram
lowerTrigram
originalHexagram
nuclearHexagram
changingLine
changedHexagram
ti
yong
elementRelation
seasonalQi
calculationTrace
```

旧卦必须可按原规则版本复算。

---

# 29. 当前必须移出生产链的 Fixture

```text
FixtureDirectionRule
FixtureClassicTexts
```

允许迁移到 `src/test/`、`fixtures/` 或 `debug/`，但正式依赖注入不得默认使用 Fixture。

---

# 30. UI 建设原则

UI 不再是最高优先级。先核心、再 UI。

继续保持玄黑、墨色、古铜金、暗金、朱砂、青玉、象牙白的传统数术风格。

罗盘页新增“时间盘”，同时显示农历、年支、时辰、节气。

时空并列显示：

```text
时：丙午年 七月廿三 巳时
空：182.4° · 午山 · 离宫 · 坐子向午
```

---

# 31. 首页与设置页

V2.0 首页建议：

```text
时空起卦
风水罗盘
传统时间起卦
宅居测局
卦例记录
规则与典籍
```

设置页新增：默认起卦体系、日界、闰月策略、互卦策略、北向策略、显示层级。

“规则与典籍”允许用户查看规则版本、原典版本、来源、历法策略和北向策略。

---

# 32. PDCA 执行规范

每个 Cycle 必须：

```text
Plan
↓
Do
↓
Check
↓
Act
↓
Re-Check
↓
Git Commit
↓
Push
↓
下一 Cycle
```

Plan 必须记录：当前状态、目标、不做什么、预计文件、风险、验收标准、测试清单。

Do 原则：小步实现、算法优先、UI分离、每条规则有测试、不得偷偷加临时术数公式。

Check 至少包含：Gradle Build、Unit Test、Lint、算法边界、模拟器、回归、Crash/ANR、文档一致性。

Act 必须记录：问题、原因、修复、复验、遗留。

---

# 33. V2.0 建设 Cycle

## Cycle 10A — 文献与规则冻结

完成 `SOURCE_CATALOG.md`、`RULE_MANIFEST.md`、`YIJING_RULES.md V2`，冻结周易原典底本、梅花时间法、后天端法、体用、互卦、五行、旺衰、二十四山来源分类和规则冲突清单。

验收：没有来源的规则不得进入正式代码。

## Cycle 10B — CalendarCore

完成中国传统历法、农历年月日、闰月、十二时辰、年支、干支基础、节气上下文、日界策略、时间 CalculationTrace。

## Cycle 10C — YijingCore 2.0

复核八卦、64卦、384变卦；新增互卦、体用、五行关系、时令上下文。

## Cycle 10D — DivinationCore

完成：

```text
MeihuaTimeDivinationRuleV1
MeihuaPostHeavenObjectDirectionRuleV1
TimeCastWithSpatialResponse
RuleManifest
CalculationTrace
DivinationResult
```

古例必须全通过。

## Cycle 10E — ClassicCorpus

完成 64卦辞、384爻辞、64彖、64大象、384小象、用九、用六；双源核验、版本化、checksum。

## Cycle 10F — InterpretationCore

完成卦象结构、动爻、体用、五行、旺衰、方应、本地白话报告，完全无 AI。

## Cycle 10G — 罗盘引擎修复与时空融合

完成多圈旋转 Bug、Sensor Accuracy 分离、时间+罗盘同时锁定、YijingMomentContext、空间方应和真机清单升级。

## Cycle 10H — App V2 接入

移除生产 Fixture 和 AI，接入正式核心，更新首页、罗盘、起卦、结果、原典、历史、设置和规则来源。

## Cycle 10I — 数据迁移

V1 Fixture 卦例统一标记 `legacy-fixture`，保留查看但不伪装为 V2 正式结果；V2 新卦一律使用正式规则。

## Cycle 10J — V2 正式验收

必须满足：

```text
0 AI
0 网络请求
0 生产 Fixture
0 随机演算
```

以及 64卦完整、384爻完整、384变卦、64互卦、384体用、古例、历法边界、24山边界、多圈罗盘、CalculationTrace、RuleManifest、原典版本、飞行模式 E2E、Room 历史、Debug、Release、Lint、Crash/ANR 全部通过。

---

# 34. Git 与文档规则

每个 Cycle 独立 commit 并 push 远端。建议提交类型：

```text
feat(calendar)
feat(yijing)
feat(divination)
feat(classics)
feat(interpretation)
fix(compass)
chore(acceptance)
```

强制维护：

```text
DOCS/ARCHITECTURE.md
DOCS/YIJING_RULES.md
DOCS/SOURCE_CATALOG.md
DOCS/RULE_MANIFEST.md
DOCS/PDCA_LOG.md
DOCS/TEST_MATRIX.md
DOCS/REAL_DEVICE_TEST.md
CHANGELOG.md
```

---

# 35. Agent 自主权限与禁区

Agent 可自主处理 Kotlin 包结构、模块拆分、Compose 组件、Room、测试方式、性能、动画、代码质量和非术数核心 UI 细节。

以下不得擅自决定：

1. 文献存在实质异文；
2. 闰月不同取数口径；
3. 子时 23:00 / 00:00 换日争议；
4. 真太阳时是否引入；
5. 乾坤互卦特殊法；
6. 旺衰具体月令口径；
7. 二十四山理气扩展；
8. 无文献依据的现代场景配卦。

遇到这些事项只能记录待决策，不得为了“功能完整”创造公式。

---

# 36. 最终验收红线

## 工程

```text
Debug ✓
Release ✓
Lint 无 error
Unit Test ✓
```

## 演算

```text
64卦 ✓
384爻 ✓
384变卦 ✓
64互卦 ✓
384体用 ✓
古例 ✓
```

## 时间

```text
农历 ✓
闰月 ✓
时辰 ✓
日界 ✓
节气上下文 ✓
```

## 空间

```text
24山 ✓
坐向 ✓
多圈旋转 ✓
稳定度 ✓
磁扰 ✓
```

## 原典

```text
完整 ✓
可追溯 ✓
有版本 ✓
有 checksum ✓
```

## 离线

```text
飞行模式完整 E2E ✓
无 INTERNET 权限 ✓
无网络请求 ✓
无 AI ✓
```

---

# 37. 真实设备验收

模拟器无法替代真磁场、真磁力计、真实方位准确度、真实磁扰、8字校准、连续旋转和不同机型 Sensor Fusion。

继续维护 `DOCS/REAL_DEVICE_TEST.md`。

真机未完成前：罗盘模块只能 CONDITIONAL PASS；周易演算核心本身可独立 PASS。

---

# 38. 最终用户主流程

```text
打开 ShineFS
↓
自动显示当前传统历法 / 时辰
↓
进入时空起卦
↓
进入风水罗盘
↓
定盘
↓
锁定时间 + 空间
↓
选择起卦体系
↓
本地正式起卦
↓
显示 CalculationTrace
↓
本卦
↓
互卦
↓
体用
↓
动爻
↓
变卦
↓
五行 / 时令
↓
空间方应
↓
周易原典
↓
本地白话释义
↓
保存
↓
随时离线复算
```

---

# 39. 建设优先级

## P0

```text
CalendarCore
YijingCore
DivinationCore
ClassicCorpus
```

## P1

```text
InterpretationCore
CompassCore
时空融合
```

## P2

```text
UI
动效
报告体验
```

未来任何工作都不得再次出现“UI 远比演算核心成熟”。

---

# 40. 最终要求

从 V2.0 开始，ShineFS 的产品价值不再是“做得像传统罗盘”，而是：

> **用严格分层、可追溯、可复算的软件工程方法，将传统历法、周易卦爻体系、梅花易数起卦法、电子罗盘空间数据整合为一个完全离线的确定性演算系统。**

任何无法说明来源、无法测试、无法复算的术数结论，不得进入正式核心。
