import os
import time
import json
import requests
import re

MY_COOKIE = "曲奇"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36",
    "Accept-Language": "zh-CN,zh;q=0.9",
    "Cookie": MY_COOKIE
}

BASE_DIR = r"E:\2026JudgeMesh\GIT提交\JudgeMesh\data\demo-problems"

def scrape_problem(pid):
    # 不要加参数，就伪装成最普通的人类访问
    url = f"https://www.luogu.com.cn/problem/{pid}"
    print(f"正在抓取 {pid} ...", end=" ", flush=True)

    try:
        response = requests.get(url, headers=HEADERS, timeout=10)

        if response.status_code != 200:
            print(f"❌ 失败 (HTTP {response.status_code})")
            return False

        # 利用抓包发现的新结构，提取 lentille-context 里的 JSON
        match = re.search(r'<script id="lentille-context" type="application/json">(.*?)</script>', response.text, re.DOTALL)
        if not match:
            print(f"❌ 失败：找不到 JSON 数据，可能 Cookie 失效或被防火墙拦截。")
            return False

        json_str = match.group(1).strip()
        data = json.loads(json_str)

        # 根据抓包的数据结构逐层解析
        problem_data = data['data']['problem']
        title = problem_data['name']

        # 安全获取时间和内存限制
        time_arr = problem_data['limits']['time']
        time_limit = max(time_arr) if time_arr else 1000
        mem_arr = problem_data['limits']['memory']
        # 洛谷内存单位是 KB，除以 1024 变 MB
        memory_limit = max(mem_arr) // 1024 if mem_arr else 256

        # 提取题面文本（新版洛谷在 content 字段里）
        content_data = problem_data.get('content', problem_data)

        # 拼接 Markdown
        markdown = f"## 题目背景\n{content_data.get('background', '')}\n\n"
        markdown += f"## 题目描述\n{content_data.get('description', '')}\n\n"
        markdown += f"## 输入格式\n{content_data.get('formatI', '')}\n\n"
        markdown += f"## 输出格式\n{content_data.get('formatO', '')}\n\n"
        markdown += f"## 提示\n{content_data.get('hint', '')}\n"

        samples = problem_data.get('samples', [])

        # --- 开始生成本地文件结构 ---
        folder_name = f"{pid}"
        prob_dir = os.path.join(BASE_DIR, folder_name)
        testcase_dir = os.path.join(prob_dir, "testcases")

        os.makedirs(testcase_dir, exist_ok=True)

        meta = {
            "title": f"[{pid}] {title}",
            "timeLimitMs": time_limit,
            "memoryLimitMb": memory_limit,
            "difficulty": "MEDIUM",
            "tags": ["洛谷真题"]
        }
        with open(os.path.join(prob_dir, "meta.json"), "w", encoding="utf-8") as f:
            json.dump(meta, f, ensure_ascii=False, indent=2)

        with open(os.path.join(prob_dir, "description.md"), "w", encoding="utf-8") as f:
            f.write(markdown)

        for idx, sample in enumerate(samples, start=1):
            with open(os.path.join(testcase_dir, f"{idx}.in"), "w", encoding="utf-8") as f:
                f.write(sample[0])
            with open(os.path.join(testcase_dir, f"{idx}.ans"), "w", encoding="utf-8") as f:
                f.write(sample[1])

        print(f"✅ 成功! [{title}] (提取了 {len(samples)} 组样例)")
        return True

    except Exception as e:
        print(f"❌ 异常报错: {e}")
        return False

def main():
    print("🤖 洛谷最新版题面自动爬取脚本启动...")
    os.makedirs(BASE_DIR, exist_ok=True)

    start_pid = 1010
    end_pid = 1110

    for i in range(start_pid, end_pid + 1):
        pid = f"P{i}"
        scrape_problem(pid)
        time.sleep(3) # 模拟人类慢速访问

    print("\n🎉 爬取结束！")

if __name__ == "__main__":
    main()
