# -*- coding: utf-8 -*-
"""ClassicCorpus 构建管线：wikitext → 结构化 → 简体 → Kotlin 数据文件。

输入：edition/raw/*.txt（维基文库《周易》64卦 wikitext 存档）
输出：
  - build/corpus.json（中间结构，供核验）
  - ../src/main/kotlin/com/shinefs/core/classics/data/CanonicalTextsData.kt（正式数据）
流程：解析（经/彖/大象/小象/用爻）→ OpenCC t2s → 结构校验（64卦/386爻/小象计数/名称对表）
      → SHA-256 checksum（逐卦 + 全库）→ 生成 Kotlin。
异文：{{*|...}} 模板抽取为 textualVariants（透明保留，不并入正文）。
注意：本文件含中文，必须以 UTF-8 保存与运行（Windows 控制台显示乱码不影响数据正确性）。
"""
import hashlib
import io
import json
import os
import re

from opencc import OpenCC

HERE = os.path.dirname(os.path.abspath(__file__))
RAW_DIR = os.path.join(HERE, 'raw')
BUILD_DIR = os.path.join(HERE, 'build')
OUT_KT = os.path.join(HERE, '..', 'src', 'main', 'kotlin', 'com', 'shinefs', 'core', 'classics',
                      'data', 'CanonicalTextsData.kt')
os.makedirs(BUILD_DIR, exist_ok=True)

CC = OpenCC('t2s')

# OpenCC 不处理的简体规范化（卦表用字对齐 + 通行简体）
NORMALIZE = {'遯': '遁'}

EDITION = '通行本《周易》（底本电子源：zh.wikisource.org「周易」，OpenCC t2s 转写简体）'
SOURCE_ID = 'S-AE2'
VERSION = 'zhouyi-corpus-v1'

EXPECTED_NAMES = ['乾', '坤', '屯', '蒙', '需', '讼', '师', '比', '小畜', '履', '泰', '否', '同人', '大有',
                  '谦', '豫', '随', '蛊', '临', '观', '噬嗑', '贲', '剥', '复', '无妄', '大畜', '颐', '大过',
                  '坎', '离', '咸', '恒', '遁', '大壮', '晋', '明夷', '家人', '睽', '蹇', '解', '损', '益',
                  '夬', '姤', '萃', '升', '困', '井', '革', '鼎', '震', '艮', '渐', '归妹', '丰', '旅', '巽',
                  '兑', '涣', '节', '中孚', '小过', '既济', '未济']

MARKER_ALT = '初九|初六|九二|六二|九三|六三|九四|六四|九五|六五|上九|上六|用九|用六'
MARKER_TO_LINE = {'初九': 1, '初六': 1, '九二': 2, '六二': 2, '九三': 3, '六三': 3,
                  '九四': 4, '六四': 4, '九五': 5, '六五': 5, '上九': 6, '上六': 6,
                  '用九': 7, '用六': 7}
USE_MARKERS = {'用九', '用六'}


def s(text):
    converted = CC.convert(text)
    for k, v in NORMALIZE.items():
        converted = converted.replace(k, v)
    return converted


def strip_markup(line, variants):
    s0 = line
    s0 = re.sub(r'\{\{\*\|([^}]*)\}\}', lambda m: (variants.append(m.group(1)) or ''), s0)
    s0 = re.sub(r'\[\[(?:File|Image):[^\]]*\]\]', '', s0)
    s0 = re.sub(r'</?span[^>]*>', '', s0)
    s0 = re.sub(r'style="[^"]*"', '', s0)
    s0 = s0.replace('>', '')
    s0 = s0.replace("'''", '')
    s0 = s0.replace('-{', '').replace('}-', '')
    s0 = re.sub(r'^[;*#]+', '', s0)
    s0 = re.sub(r'\{\{[^}]*\}\}', '', s0)
    return s0.strip()


def parse_page(wt):
    variants = []
    section = None
    judgment_parts = []
    line_texts = {}
    line_order = []
    tuan_parts = []
    great_image_parts = []
    small_images = []
    seen_xiang_item = False

    for raw in wt.split('\n'):
        stripped = strip_markup(raw, variants)
        if not stripped:
            continue
        if '易經：' in stripped or '易经：' in stripped:
            section = 'jing'
            continue
        if re.fullmatch(r'彖曰：?', stripped):
            section = 'tuan'
            continue
        if re.fullmatch(r'象曰：?', stripped):
            section = 'xiang'
            continue
        if re.fullmatch(r'文言曰：?', stripped):
            section = 'skip'
            continue
        if stripped.startswith('周易'):
            continue

        is_item = re.match(r'^[*#]*#', raw.strip()) is not None
        if section == 'jing':
            m = re.match(r'^(%s)[：，](.*)$' % MARKER_ALT, stripped)
            if is_item and m:
                line_texts[m.group(1)] = m.group(2)
                line_order.append(m.group(1))
            elif stripped:
                judgment_parts.append(stripped)
        elif section == 'tuan':
            tuan_parts.append(stripped)
        elif section == 'xiang':
            if is_item:
                seen_xiang_item = True
                small_images.append(stripped)
            elif not seen_xiang_item:
                great_image_parts.append(stripped)

    judgment_full = ''.join(judgment_parts)
    name = None
    judgment = judgment_full
    jm = re.match(r'^([^，。：]{1,4})：(.*)$', judgment)
    if jm:
        name, judgment = jm.group(1), jm.group(2)
    return {
        'name': name,
        'judgment': judgment,
        'judgmentFull': judgment_full,
        'lineMarkers': line_order,
        'lineTexts': line_texts,
        'tuan': ''.join(tuan_parts),
        'greatImage': ''.join(great_image_parts),
        'smallImages': small_images,
        'variants': variants,
    }


def checksum_entry(e):
    canon = '|'.join([
        str(e['kingWenOrder']), e['name'], e['judgment'], e['tuan'] or '', e['greatImage'] or '',
        *[u"%d:%s:%s" % (l['line'], l['text'], l['smallImage'] or '') for l in e['lines']],
        e['specialUseText'] or '', e['specialUseSmallImage'] or '',
    ])
    return hashlib.sha256(canon.encode('utf-8')).hexdigest()


def kotlin_str(x):
    return '"' + (x or '').replace(chr(92), chr(92) * 2).replace('"', chr(92) + '"') + '"'


def kotlin_opt(x):
    return kotlin_str(x) if x else 'null'


def main():
    index = json.load(io.open(os.path.join(HERE, 'index.json'), encoding='utf-8'))
    entries = []
    for item in index:
        wt = io.open(os.path.join(RAW_DIR, item['file']), encoding='utf-8').read()
        parsed = parse_page(wt)
        trad_name = parsed['name'] or item['name']
        expected = EXPECTED_NAMES[item['order'] - 1]
        converted = s(trad_name)
        if converted == '干' and trad_name == '乾':
            converted = '乾'  # OpenCC 单字「乾」误转「干」，卦名以既定卦表为准
        if converted != expected:
            if expected in converted:
                # 卦辞前缀含卦名但不止卦名（如坎卦「習坎」属卦辞正文）：
                # 卦名取既定表，卦辞还原「前缀，正文」全形。
                head, sep, rest = parsed['judgmentFull'].partition('：')
                parsed['judgment'] = (s(head) + '，' + rest) if sep else parsed['judgment']
                print('note: order %d name prefix kept in judgment: %s' % (item['order'], converted))
            else:
                raise SystemExit('order %d: name mismatch %r != %r' % (item['order'], converted, expected))
        name = expected

        markers = parsed['lineMarkers']
        expect_n = 7 if item['order'] in (1, 2) else 6
        if len(markers) != expect_n:
            raise SystemExit('order %d %s: %d lines != %d' % (item['order'], name, len(markers), expect_n))
        if len(parsed['smallImages']) != expect_n:
            raise SystemExit('order %d %s: %d small images != %d' % (item['order'], name, len(parsed['smallImages']), expect_n))
        if not parsed['tuan'] or not parsed['greatImage'] or not parsed['judgment']:
            raise SystemExit('order %d %s: empty required field' % (item['order'], name))

        lines = []
        special_use = None
        special_use_si = None
        for i, marker in enumerate(markers):
            text = s(parsed['lineTexts'][marker])
            small = s(parsed['smallImages'][i])
            if marker in USE_MARKERS:
                special_use = '%s：%s' % (marker, text)
                special_use_si = small
            else:
                lines.append({'line': MARKER_TO_LINE[marker], 'text': text, 'smallImage': small})

        entry = {
            'kingWenOrder': item['order'],
            'name': name,
            'judgment': s(parsed['judgment']),
            'tuan': s(parsed['tuan']),
            'greatImage': s(parsed['greatImage']),
            'lines': sorted(lines, key=lambda l: l['line']),
            'specialUseText': special_use and s(special_use),
            'specialUseSmallImage': special_use_si and s(special_use_si),
            'textualVariants': [s(v) for v in parsed['variants']],
        }
        entry['checksum'] = checksum_entry(entry)
        entries.append(entry)

    all_orders = [e['kingWenOrder'] for e in entries]
    assert all_orders == list(range(1, 65)), 'order coverage'
    total_lines = sum(len(e['lines']) for e in entries)
    assert total_lines == 384, '384 lines expected, got %d' % total_lines

    corpus_checksum = hashlib.sha256(
        ''.join(e['checksum'] for e in entries).encode('utf-8')).hexdigest()

    with io.open(os.path.join(BUILD_DIR, 'corpus.json'), 'w', encoding='utf-8') as f:
        json.dump({'version': VERSION, 'edition': EDITION, 'sourceId': SOURCE_ID,
                   'corpusChecksum': corpus_checksum, 'entries': entries}, f, ensure_ascii=False, indent=1)

    # ---- 生成 Kotlin ----
    out = io.StringIO()
    w = out.write
    NL = chr(10)
    w('// GENERATED by core/classics/edition/build_corpus.py — DO NOT EDIT BY HAND.' + NL)
    w('// 来源与核验：DOCS/SOURCE_CATALOG.md（S-A01/S-AE2）、DOCS/YIJING_RULES.md §10。' + NL)
    w('// 异文记录（textualVariants）保留于数据内，正文不并入。' + NL)
    w('package com.shinefs.core.classics.data' + NL + NL)
    w('import com.shinefs.core.classics.CanonicalHexagramText' + NL)
    w('import com.shinefs.core.classics.CanonicalLineText' + NL + NL)
    w('/** 周易原典正式数据（版本 ' + VERSION + '）。生成自维基文库底本存档，结构校验 64 卦 / 386 爻。 */' + NL)
    w('object CanonicalTextsData {' + NL + NL)
    w('    const val VERSION = ' + kotlin_str(VERSION) + NL)
    w('    const val EDITION = ' + kotlin_str(EDITION) + NL)
    w('    const val SOURCE_ID = ' + kotlin_str(SOURCE_ID) + NL)
    w('    const val CORPUS_CHECKSUM = ' + kotlin_str(corpus_checksum) + NL + NL)
    w('    val all: List<CanonicalHexagramText> = listOf(' + NL)
    for e in entries:
        w('        CanonicalHexagramText(' + NL)
        w('            kingWenOrder = %d,' % e['kingWenOrder'] + NL)
        w('            name = ' + kotlin_str(e['name']) + ',' + NL)
        w('            judgment = ' + kotlin_str(e['judgment']) + ',' + NL)
        w('            tuan = ' + kotlin_str(e['tuan']) + ',' + NL)
        w('            greatImage = ' + kotlin_str(e['greatImage']) + ',' + NL)
        w('            lines = listOf(' + NL)
        for l in e['lines']:
            w('                CanonicalLineText(line = %d, text = %s, smallImage = %s),' % (
                l['line'], kotlin_str(l['text']), kotlin_str(l['smallImage'])) + NL)
        w('            ),' + NL)
        w('            specialUseText = ' + kotlin_opt(e['specialUseText']) + ',' + NL)
        w('            specialUseSmallImage = ' + kotlin_opt(e['specialUseSmallImage']) + ',' + NL)
        w('            textualVariants = listOf(' + ', '.join(kotlin_str(v) for v in e['textualVariants']) + '),' + NL)
        w('            sourceEdition = EDITION,' + NL)
        w('            sourceId = SOURCE_ID,' + NL)
        w('            verified = true,' + NL)
        w('            checksum = ' + kotlin_str(e['checksum']) + ',' + NL)
        w('        ),' + NL)
    w('    )' + NL + '}' + NL)
    io.open(OUT_KT, 'w', encoding='utf-8', newline=NL).write(out.getvalue())
    print('OK: 64 entries, 384 lines + 2 use-lines, corpus checksum %s...' % corpus_checksum[:16])
    print('variants recorded: %d' % sum(len(e['textualVariants']) for e in entries))


if __name__ == '__main__':
    main()
