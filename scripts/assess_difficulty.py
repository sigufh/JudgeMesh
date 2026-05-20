import os
import json
import time
from openai import OpenAI

# ========== 配置区域 ==========
DEEPSEEK_API_KEY = "api_key"
BASE_URL = "https://api.deepseek.com"
MODEL_NAME = "deepseek-v4-flash"

# 题目存放的根目录
BASE_DIR = r"E:\2026JudgeMesh\GIT提交\JudgeMesh\data\demo-problems"

# 要评估的题目范围
START_PID = 1010
END_PID = 1110

# 请求间隔（秒）
REQUEST_INTERVAL = 1.0

# ========== 初始化客户端 ==========
client = OpenAI(
    api_key=DEEPSEEK_API_KEY,
    base_url=BASE_URL
)

# ========== 工具函数 ==========
def read_description(pid):
    """读取题目的 description.md 内容，若不存在返回 None"""
    file_path = os.path.join(BASE_DIR, pid, "description.md")
    if not os.path.isfile(file_path):
        print(f"  ⚠️ {pid}: description.md 不存在，跳过")
        return None
    with open(file_path, "r", encoding="utf-8") as f:
        return f.read()

def update_meta_difficulty(pid, difficulty):
    """更新 meta.json 中的 difficulty 字段"""
    meta_path = os.path.join(BASE_DIR, pid, "meta.json")
    if not os.path.isfile(meta_path):
        print(f"  ⚠️ {pid}: meta.json 不存在，无法更新")
        return False
    try:
        with open(meta_path, "r", encoding="utf-8") as f:
            meta = json.load(f)
        meta["difficulty"] = difficulty
        with open(meta_path, "w", encoding="utf-8") as f:
            json.dump(meta, f, ensure_ascii=False, indent=2)
        return True
    except Exception as e:
        print(f"  ❌ {pid}: 更新 meta.json 失败 - {e}")
        return False

def assess_difficulty(description):
    """调用 DeepSeek API 评估难度，返回 EASY / MEDIUM / HARD，失败返回 None"""
    system_prompt = (
        "你是一个算法竞赛专家。请根据以下题目描述，评估该题目的难度。"
        "严格只回复一个单词：EASY、MEDIUM 或 HARD。不要输出任何其他内容。"
    )
    try:
        response = client.chat.completions.create(
            model=MODEL_NAME,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": description[:3000]}  # 截断过长的题面，节省 token
            ],
            temperature=0.0,
            max_tokens=10,
            # 不启用思考模式（即不返回推理过程）
            extra_body={"thinking": {"type": "disabled"}}
        )
        result = response.choices[0].message.content.strip().upper()
        # 兜底：如果返回的内容包含多个单词，取第一个合法的
        for word in ["EASY", "MEDIUM", "HARD"]:
            if word in result:
                return word
        print(f"  ⚠️ API 返回了无法识别的结果: '{result}'，默认设为 MEDIUM")
        return "MEDIUM"
    except Exception as e:
        print(f"  ❌ API 调用失败: {e}")
        return None

# ========== 主流程 ==========
def main():
    print("🚀 题目难度评估脚本启动（使用 DeepSeek API）")
    print(f"📂 题目目录：{BASE_DIR}")
    print(f"📊 评估范围：P{START_PID} ~ P{END_PID}")
    print()

    success_count = 0
    skip_count = 0
    fail_count = 0

    for num in range(START_PID, END_PID + 1):
        pid = f"P{num}"
        print(f"[{pid}] ", end="", flush=True)

        # 读取题面
        desc = read_description(pid)
        if desc is None:
            skip_count += 1
            continue

        # 调用 AI 评估
        difficulty = assess_difficulty(desc)
        if difficulty is None:
            fail_count += 1
            time.sleep(REQUEST_INTERVAL)
            continue

        # 更新 meta.json
        if update_meta_difficulty(pid, difficulty):
            print(f"✅ → {difficulty}")
            success_count += 1
        else:
            fail_count += 1

        # 控制频率
        time.sleep(REQUEST_INTERVAL)

    print("\n========== 评估完成 ==========")
    print(f"✅ 成功: {success_count}")
    print(f"⏭️ 跳过: {skip_count} (缺少 description.md)")
    print(f"❌ 失败: {fail_count} (API错误或文件更新失败)")

if __name__ == "__main__":
    main()
