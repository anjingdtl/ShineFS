# -*- coding: utf-8 -*-
"""抓取维基文库《周易》64卦原文（wikitext 源码，繁体，版本证据存档）。

管线（构建期一次性，产物入库；App 运行时零网络）：
1. allpages 枚举「周易/」全部子页（标题混用繁简，不能靠导航链猜测）；
2. 逐页抓取 wikitext → raw/（已存在则复用，断点续抓）；
3. 从正文「周易　第N卦」提取 King Wen 序号 + 卦名，排序校验 1..64 完整；
4. 生成 index.json。
"""
import io
import json
import os
import re
import time
import urllib.parse
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
RAW_DIR = os.path.join(HERE, 'raw')
os.makedirs(RAW_DIR, exist_ok=True)

UA = {'User-Agent': 'ShineFS-corpus-build/2.0 (offline app corpus pipeline)'}
LIST_URL = ('https://zh.wikisource.org/w/api.php?action=query&list=allpages'
            '&apprefix={prefix}&aplimit=500&format=json&apfilterredir=nonredirects')
PAGE_URL = 'https://zh.wikisource.org/w/api.php?action=parse&page={page}&format=json&prop=wikitext'

CN_NUM = {c: i for i, c in enumerate('一二三四五六七八九', 1)}


def cn_to_int(s):
    s = s.strip()
    if s == '十':
        return 10
    if '十' in s:
        tens, _, ones = s.partition('十')
        return (CN_NUM.get(tens, 1) if tens else 1) * 10 + (CN_NUM.get(ones, 0) if ones else 0)
    return CN_NUM[s]


def http_json(url, retries=6):
    last = None
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers=UA)
            with urllib.request.urlopen(req, timeout=40) as resp:
                return json.load(resp)
        except Exception as e:
            last = e
            wait = 8 * (attempt + 1)
            print(f'  retry after {wait}s: {e}')
            time.sleep(wait)
    raise last


def list_subpages():
    data = http_json(LIST_URL.format(prefix=urllib.parse.quote('周易/')))
    return [p['title'] for p in data['query']['allpages']]


def fetch_page(title):
    data = http_json(PAGE_URL.format(page=urllib.parse.quote(title)))
    if 'error' in data:
        raise RuntimeError(f'{title}: {data["error"]["code"]}')
    return data['parse']['wikitext']['*']


def parse_meta(wt):
    clean = wt.replace('[[', '').replace(']]', '').replace('-{', '').replace('}-', '')
    m = re.search(r'第([一二三四五六七八九十]{1,3})卦\s*([^\s，。；：〈]{1,4})', clean)
    if not m:
        return None, None
    order = cn_to_int(m.group(1))
    name = m.group(2).strip('；;，。 ')
    return order, name


def parse_meta_from_cache(title):
    stem = title.split('/', 1)[1]
    for f in os.listdir(RAW_DIR):
        if f.endswith(stem + '.txt'):
            wt = io.open(os.path.join(RAW_DIR, f), encoding='utf-8').read()
            return parse_meta(wt)
    return None


def safe_name(name):
    return re.sub(r'[\\/:*?"<>|]', '_', name)


def main():
    titles = list_subpages()
    print(f'{len(titles)} subpages')
    pages = []
    for title in titles:
        cached = parse_meta_from_cache(title)
        if cached and cached[0]:
            order, name = cached
        else:
            wt = fetch_page(title)
            order, name = parse_meta(wt)
            fname = f'{order:02d}_{safe_name(title.split("/", 1)[1])}.txt' if order else f'00_{safe_name(title.split("/", 1)[1])}.txt'
            with io.open(os.path.join(RAW_DIR, fname), 'w', encoding='utf-8', newline='\n') as f:
                f.write(wt)
            time.sleep(2.5)
        pages.append({'title': title, 'order': order, 'name': name})
        print(f'{title} -> {order} {name}')

    hexagrams = sorted([p for p in pages if p['order']], key=lambda p: p['order'])
    missing = [i for i in range(1, 65) if i not in {p['order'] for p in hexagrams}]
    print(f'hexagram chapters: {len(hexagrams)}; other pages: {len(pages) - len(hexagrams)}')
    if missing:
        raise SystemExit(f'missing orders: {missing}')
    index = [{
        'order': p['order'],
        'name': p['name'],
        'page': p['title'],
        'file': f"{p['order']:02d}_{safe_name(p['title'].split('/', 1)[1])}.txt",
    } for p in hexagrams]
    with io.open(os.path.join(HERE, 'index.json'), 'w', encoding='utf-8') as f:
        json.dump(index, f, ensure_ascii=False, indent=1)
    print('index.json written, 64 chapters verified')


if __name__ == '__main__':
    main()
