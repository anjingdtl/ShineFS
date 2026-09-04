# ShineFS 规则清单（RULE_MANIFEST）

> 版本：manifest-v2.0（Cycle 10A，2026-09-04）
> 每条正式规则的"从哪里来"（V2.0 方案 §19）。代码内以 `RuleManifest` 数据类承载同名字段，
> 本文件是其人读总账；两处必须同步修订。来源编号见 `DOCS/SOURCE_CATALOG.md`。

## 0. 状态取值

| RuleStatus | 含义 |
|---|---|
| VERIFIED | 文献明文 + 金标准古例/测试通过，无附加口径 |
| VERIFIED_WITH_EXPLICIT_ASSUMPTIONS | 文献明文，但存在显式登记的工程假设/口径选择 |
| ENGINEERING_POLICY | 非术数文献规则，是登记在案的工程政策 |
| PENDING | 待决策，禁止进入正式代码 |

## 1. 演算规则

| ruleId | version | system | 来源 | 关键假设 | status |
|---|---|---|---|---|---|
| `xiantian-number-v1` | 1 | YIJING_CLASSIC (B) | S-B01 | 先天数次序为邵氏象数传统，非《周易》经文 | VERIFIED |
| `later-heaven-bagua-v1` | 1 | YIJING_CLASSIC (A) | S-A04、S-C02 | 八宫各 45°，中心角对准卦位正中 | VERIFIED |
| `hexagram-structure-v1` | 1 | YIJING_CLASSIC (A) | S-A01 | 通行本 King Wen 序 1..64；下卦+上卦唯一确定 | VERIFIED |
| `changing-line-v1` | 1 | YIJING_CLASSIC (A) | S-A01（变卦通则） | 动爻 1..6 自下而上；只翻该爻；翻两次回本卦 | VERIFIED |
| `normalize-remainder-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B02 + 古例数值反推 | `((n-1)%k)+1`：余0 取 k（8→坤、6→上爻）；观梅占/牡丹占数值自洽 | VERIFIED_WITH_EXPLICIT_ASSUMPTIONS |
| `meihua-time-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B02、S-E04、S-E05 | ①年支数=支序（子1…亥12），年界=农历正月初一（观梅占古例反推，TD-V2-03）②月/日用农历月数/日数③时辰数按 §5.1 表④闰月 SAME_MONTH_NUMBER⑤日界 CIVIL_MIDNIGHT | VERIFIED_WITH_EXPLICIT_ASSUMPTIONS |
| `meihua-postheaven-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B03 | 物象卦为上卦、方位卦为下卦；物象仅出自版本化类象表；动爻=(物数+方数+时辰数) normalize6 | VERIFIED_WITH_EXPLICIT_ASSUMPTIONS |
| `meihua-classimage-v1` | 1 | YIJING_CLASSIC (A) | S-B04（《说卦传》明文） | 仅收说卦明文类象（老人→乾、少年→艮、牛→坤等）；不扩充现代配卦（TD-V2-07） | VERIFIED |
| `nuclear-hexagram-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B05 | 默认 STANDARD_234_345：下互=2,3,4爻、上互=3,4,5爻；乾坤照常有互（"乾坤无互"旧说为未启用策略，方案 §13） | VERIFIED_WITH_EXPLICIT_ASSUMPTIONS |
| `tiyong-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B05 | 动爻 1–3 → 下卦为用、上卦为体；4–6 → 上卦为用、下卦为体 | VERIFIED |
| `trigram-element-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B06 | 乾兑金、震巽木、坎水、离火、坤艮土 | VERIFIED |
| `element-relation-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B07 | 五对生、五对克、同比和；只判事实关系，不断吉凶 | VERIFIED |
| `seasonal-qi-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B08、S-E03 | 春木、夏火、秋金、冬水（按节气月令）；土旺辰戌丑未月；仅呈现当令五行事实层（TD-V2-05） | VERIFIED_WITH_EXPLICIT_ASSUMPTIONS |
| `spatial-response-v1` | 1 | MEIHUA_YISHU_TRADITION (B) | S-B09、S-C02 | 方位卦为事实层输入，与体卦做五行关系描述；**空间数据不修改时间卦**（方案 §10） | VERIFIED_WITH_EXPLICIT_ASSUMPTIONS |
| `mountains24-v1` | 1 | LUOJING_GEOGRAPHY (C) | S-C01 | 每山15°，index=floor(((az+7.5)%360)/15) | VERIFIED |
| `orientation-v1` | 1 | LUOJING_GEOGRAPHY (C) | S-C03 | facing=azimuth、sitting=+180；北参考默认磁北（TD-V2-08） | VERIFIED_WITH_EXPLICIT_ASSUMPTIONS |

## 2. 历法工程规则

| ruleId | version | 来源 | 关键假设 | status |
|---|---|---|---|---|
| `calendar-table-v1` | 1 | S-E01 | 内置 1900–2100 历表；锚点+第三方独立交叉验证源 `lunar_python` 构建期采样核验；当前项目既有 ICU 对照实现曾出现异常，暂不作为正式 Oracle；版本化+checksum | ENGINEERING_POLICY |
| `ganzhi-day-v1` | 1 | S-E02 | JDN 取模 60，双锚点互验（1900-01-01 甲戌、1949-10-01 甲子） | ENGINEERING_POLICY |
| `ganzhi-year-v1` | 1 | S-E05 | 年干支随农历年（正月初一）轮转 | ENGINEERING_POLICY |
| `shichen-v1` | 1 | S-E04 | 12 时辰整点左闭右开；23:00 属次日子时 | ENGINEERING_POLICY |
| `solar-term-meeus-v1` | 1 | S-E03 | Meeus 太阳视黄经算法；误差容忍 ±2 分钟（仅时令上下文，不入起卦） | ENGINEERING_POLICY |
| `day-boundary-policy` | 1 | 方案 §5.4 | 默认 CIVIL_MIDNIGHT；ZI_HOUR_START_23 为显式高级策略 | ENGINEERING_POLICY |
| `leap-month-policy` | 1 | 方案 §5.3 | SAME_MONTH_NUMBER（闰六月取6）——显式工程政策，非梅花原文 | ENGINEERING_POLICY |

## 3. 金标准古例（回归基线）

| 古例 | 来源 | 期望结果 |
|---|---|---|
| 观梅占 | S-B02：辰年5+十二月12+十七日17=34→兑；34+申时9=43→离；43 mod 6=1 | 泽火革，初爻动，变泽山咸 |
| 牡丹占 | S-B02：巳年6+三月3+十六日16=25→乾；25+卯时4=29→巽；29 mod 6=5 | 天风姤，五爻动，变火风鼎 |
| 老人有忧色（端法） | S-B03：乾1+巽5+卯时4=10；10 mod 6=4 | 天风姤，四爻动 |
| 少年有喜色（端法） | S-B03：艮7+离3+午时7=17；17 mod 6=5 | 山火贲，五爻动 |
| 牛哀鸣（端法） | S-B03：坤8+坎6+午时7=21；21 mod 6=3 | 地水师，三爻动 |

## 4. V1 待决策项收口映射

| V1 项 | V2 处置 |
|---|---|
| D-01/D-04/D-05（求余/动爻/时间基准口径） | 已定：normalize-remainder-v1 + meihua-time-v1（农历+年支+时辰） |
| D-02（时间起卦算法） | 已定：meihua-time-v1 |
| D-03（数字起卦余0约定） | 维持冻结：TD-V2-02 |
| D-06（产品名） | 维持 ShineFS |
| D-07（兼向/分金） | 维持不做：TD-V2-06 |
| D-08（横竖屏） | 维持竖屏 |
| D-09（原典底本） | 已定：S-A01 通行本系统 + S-AE2 电子底本 + S-AE3 代码锚点抽查；独立第二文献源待后续治理 |
| D-10（AI 解读） | 已定：AI 全链路移除（V2.0 方案 §1.2） |

## 5. 变更记录

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-09-04 | manifest-v2.0 | 初版：16 条术数规则 + 7 条历法工程规则 + 5 古例基线 + V1 决策项收口 |
