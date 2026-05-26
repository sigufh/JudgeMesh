# data/demo-problems/ — 演示题库(@KY-raika 维护)

脚本会生成并导入 50 道确定性 demo 题,覆盖:

- 4 种语言(C / C++ / Java / Python)的 AC / TLE / WA / RE / CE 各典型场景
- 难度梯度:EASY 25 + MEDIUM 20 + HARD 5
- 答辩当天用 `scripts/import-problems.py` 一键导入

若目录下存在 `problems.json`,脚本会优先读取该显式目录;否则使用内置 50 题目录生成器。

## 单题目录结构

```
data/demo-problems/01-a-plus-b/
  ├── meta.json          # 标题、描述、TL/ML、tags、difficulty
  ├── description.md     # Markdown 题面
  ├── testcases/
  │   ├── 01.in
  │   ├── 01.out
  │   ├── 02.in
  │   └── 02.out
  └── solutions/
      ├── ac.cpp
      ├── ac.py
      └── tle.py        # 故意慢的解,用于测试 TLE 判定
```

## 导入

```bash
python3 scripts/import-problems.py \
  --service http://localhost:8082 \
  --dir data/demo-problems
```
