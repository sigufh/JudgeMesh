import requests
import random
import io

#这个脚本，暂时先搞一点水题，测试流程是否成功运行

# Problem-Service 本地地址
API_URL = "http://localhost:8082/api/problems"
HEADERS = {"X-User-Id": "99"} # 模拟一个出题人ID

# 给批量生成的占位题准备的词库
TITLES = ["Two Sum", "Fibonacci Number", "Reverse Linked List", "Binary Search", "Climbing Stairs", "Valid Parentheses"]
TAGS = ["Math", "Array", "String", "Dynamic Programming", "Graph", "Greedy"]
DIFFICULTIES = ["EASY", "MEDIUM", "HARD"]

def create_problem(title, desc, tl, ml, diff, tags):
    payload = {
        "title": title,
        "description": desc,
        "timeLimitMs": tl,
        "memoryLimitMb": ml,
        "difficulty": diff,
        "tags": tags
    }
    resp = requests.post(API_URL, json=payload, headers=HEADERS)
    resp_json = resp.json()
    if resp_json.get("code") == "0":
        print(f"✅ 创建成功 [ID: {resp_json['data']}] - {title}")
        return resp_json["data"]
    else:
        print(f"❌ 创建失败: {resp_json}")
        return None

def upload_testcase(problem_id, case_index, in_content, ans_content):
    url = f"{API_URL}/{problem_id}/testcases"

    # 将字符串转为内存文件流，模拟真实文件上传
    # 明确指定 Content-Type 为 text/plain
    files = {
            'inputFile': (f'{case_index}.in', io.BytesIO(in_content.encode('utf-8')), 'text/plain'),
            'outputFile': (f'{case_index}.ans', io.BytesIO(ans_content.encode('utf-8')), 'text/plain')
    }
    data = {'caseIndex': case_index, 'score': 100 // 2} # 假设每题2个用例，每个50分

    resp = requests.post(url, files=files, data=data, headers=HEADERS)
    if resp.json().get("code") == "0":
        print(f"  └── 📁 用例 {case_index} 上传 MinIO 成功")
    else:
        print(f"  └── ⚠️ 用例上传失败: {resp.text}")

def main():
    print("🚀 开始一键导入 JudgeMesh 测试题库...\n")

    # 1. 创建 1 道“黄金演示题”
    print("--- 🌟 正在创建黄金演示题 ---")
    pid_1 = create_problem("A+B Problem", "请计算两个整数 $a$ 和 $b$ 的和。\n\n**输入**：两个空格隔开的整数。\n**输出**：一个整数，即它们的和。", 1000, 256, "EASY", ["Math", "Basic"])
    if pid_1:
        upload_testcase(pid_1, 1, "1 2\n", "3\n")
        upload_testcase(pid_1, 2, "100 200\n", "300\n")

    # 2. 自动生成 49 道占位题凑数
    print("\n--- 🤖 正在批量生成 49 道占位题 ---")
    for i in range(2, 51):
        title = f"{random.choice(TITLES)} {i}"
        desc = f"这是系统自动生成的第 {i} 道测试题，用于演示分页和大量数据处理。\n\n请使用恰当的算法解决该问题。"
        tags = random.sample(TAGS, k=random.randint(1, 3))
        diff = random.choice(DIFFICULTIES)

        pid = create_problem(title, desc, 1000, 256, diff, tags)
        if pid:
            # 顺便给水题塞一对假用例
            upload_testcase(pid, 1, f"fake input {i}\n", f"fake output {i}\n")

    print("\n🎉 题库初始化完毕！共 50 道题目！")

if __name__ == "__main__":
    main()
