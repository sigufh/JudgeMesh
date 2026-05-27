import os
import re
import json
import time
from openai import OpenAI

# ========== 配置 ==========
DEEPSEEK_API_KEY = "key"
BASE_URL = "https://api.deepseek.com"
MODEL_NAME = "deepseek-v4-pro"

# 题目根目录
BASE_DIR = r"E:\2026JudgeMesh\GIT提交\JudgeMesh\data\demo-problems"

# 要处理的题目范围
START_PID = 1013
END_PID = 1110#1110

# API 请求间隔（秒）
REQUEST_INTERVAL = 2.0

# ========== 初始化客户端 ==========
client = OpenAI(
    api_key=DEEPSEEK_API_KEY,
    base_url=BASE_URL
)

# ========== 工具函数 ==========
def read_description(pid):
    """读取题目描述"""
    path = os.path.join(BASE_DIR, pid, "description.md")
    if not os.path.isfile(path):
        return None
    with open(path, "r", encoding="utf-8") as f:
        return f.read()

def ensure_solutions_dir(pid):
    """确保 solutions 目录存在"""
    sol_dir = os.path.join(BASE_DIR, pid, "solutions")
    os.makedirs(sol_dir, exist_ok=True)
    return sol_dir

def write_solution(pid, filename, code):
    """将代码写入 solutions 目录下的文件"""
    sol_dir = ensure_solutions_dir(pid)
    file_path = os.path.join(sol_dir, filename)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)

def generate_solutions(description):
    """
    调用 DeepSeek API，要求生成 C++ 和 Python 两种 AC 代码。
    返回 (cpp_code, py_code)，如果解析失败则返回 (None, None)。
    """
    system_prompt = (
        "你是一个资深算法竞赛选手。请根据下面的题目描述，编写正确的、可以通过测试的 AC 代码。\n"
        "要求：\n"
        "1. 用 C++ 和 Python 分别编写。\n"
        "2. 代码必须是完整可运行的，包含必要的头文件和主函数。\n"
        "3. 严格按以下格式输出，不要添加任何额外解释：\n\n"
        "```cpp\n"
        "你的 C++ 代码\n"
        "```\n\n"
        "```python\n"
        "你的 Python 代码\n"
        "```\n\n"
        "注意：只输出这两个代码块，不要输出其他文字。"
    )

    try:
        response = client.chat.completions.create(
            model=MODEL_NAME,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": description[:3500]}  # 截断防止超长
            ],
            temperature=0.2,
            max_tokens=4096,
            extra_body={"thinking": {"type": "disabled"}}  # 不启用思考
        )
        content = response.choices[0].message.content

        # 用正则提取 C++ 和 Python 代码块
        cpp_match = re.search(r"```cpp\n(.*?)```", content, re.DOTALL)
        py_match = re.search(r"```python\n(.*?)```", content, re.DOTALL)

        cpp_code = cpp_match.group(1).strip() if cpp_match else None
        py_code = py_match.group(1).strip() if py_match else None

        return cpp_code, py_code

    except Exception as e:
        print(f"  ❌ API 调用失败: {e}")
        return None, None

# ========== 主流程 ==========
def main():
    print("🚀 自动生成题解脚本启动（C++ & Python）")
    print(f"📂 题目目录：{BASE_DIR}")
    print(f"📊 范围：P{START_PID} ~ P{END_PID}")
    print()

    success = 0
    skip = 0
    fail = 0

    for num in range(START_PID, END_PID + 1):
        pid = f"P{num}"
        print(f"[{pid}] ", end="", flush=True)

        desc = read_description(pid)
        if desc is None:
            print("⏭️ 跳过（无 description.md）")
            skip += 1
            continue

        cpp, py = generate_solutions(desc)

        if cpp is None or py is None:
            print("❌ 代码生成失败，可能解析出错")
            fail += 1
            time.sleep(REQUEST_INTERVAL)
            continue

        # 写入文件
        write_solution(pid, "ac.cpp", cpp)
        write_solution(pid, "ac.py", py)
        print(f"✅ 已生成 ac.cpp 和 ac.py")
        success += 1

        time.sleep(REQUEST_INTERVAL)

    print("\n========== 完成 ==========")
    print(f"✅ 成功: {success}")
    print(f"⏭️ 跳过: {skip}")
    print(f"❌ 失败: {fail}")

if __name__ == "__main__":
    main()
