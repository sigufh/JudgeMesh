import os
import json
import requests

API_URL = "http://localhost:8082/api/problems"
HEADERS = {"X-User-Id": "99"} # 模拟一个高权限的出题人

# 动态定位 demo-problems 目录
base_dir = os.path.dirname(os.path.abspath(__file__))          # scripts/
project_root = os.path.dirname(base_dir)                       # JudgeMesh/
DEMO_DIR = os.path.join(project_root, "data", "demo-problems") # JudgeMesh/data/demo-problems

def create_problem(title, desc, tl, ml, diff, tags):
    payload = {
        "title": title,
        "description": desc,
        "timeLimitMs": tl,
        "memoryLimitMb": ml,
        "difficulty": diff,
        "tags": tags
    }
    try:
        resp = requests.post(API_URL, json=payload, headers=HEADERS, timeout=5)
        resp_json = resp.json()
        if resp.status_code == 200 and resp_json.get("code") == "0":
            return resp_json["data"]
        else:
            print(f"❌ 题目创建失败: {resp.text}")
            return None
    except Exception as e:
        print(f"❌ 请求 API 异常: {e}")
        return None

def upload_testcase(problem_id, case_index, in_path, ans_path, score):
    url = f"{API_URL}/{problem_id}/testcases"
    try:
        # 直接读取本地真实文件并上传
        with open(in_path, 'rb') as f_in, open(ans_path, 'rb') as f_ans:
            files = {
                # 加上 'text/plain' 完美解决之前的 content-type 报错问题
                'inputFile': (f'{case_index}.in', f_in, 'text/plain'),
                'outputFile': (f'{case_index}.ans', f_ans, 'text/plain')
            }
            data = {'caseIndex': case_index, 'score': score}
            resp = requests.post(url, files=files, data=data, headers=HEADERS, timeout=10)

            if resp.status_code == 200 and resp.json().get("code") == "0":
                print(f"    └── 📁 测试用例 {case_index} 上传 MinIO 成功")
            else:
                print(f"    └── ⚠️ 用例上传失败: {resp.text}")
    except Exception as e:
        print(f"    └── ⚠️ 文件读取或上传异常: {e}")

def main():
    print("🚀 开始自动导入本地真实题库...")
    if not os.path.exists(DEMO_DIR):
        print(f"⚠️ 找不到目录 {DEMO_DIR}，请确认路径是否正确。")
        return

    # 按文件夹名字（P1010, P1011...）排序遍历
    folders = sorted(os.listdir(DEMO_DIR))
    success_count = 0

    for folder_name in folders:
        prob_path = os.path.join(DEMO_DIR, folder_name)
        if not os.path.isdir(prob_path):
            continue

        meta_path = os.path.join(prob_path, "meta.json")
        desc_path = os.path.join(prob_path, "description.md")

        # 严格检查文件完整性
        if not os.path.exists(meta_path) or not os.path.exists(desc_path):
            continue

        # 加载元数据和 Markdown
        with open(meta_path, 'r', encoding='utf-8') as f:
            meta = json.load(f)
        with open(desc_path, 'r', encoding='utf-8') as f:
            desc = f.read()

        print(f"\n正在导入: {folder_name} - {meta.get('title')}")

        # 1. 写入 MySQL
        pid = create_problem(meta['title'], desc, meta['timeLimitMs'], meta['memoryLimitMb'], meta['difficulty'], meta['tags'])

        if pid:
            success_count += 1
            tc_path = os.path.join(prob_path, "testcases")
            if os.path.exists(tc_path):
                # 2. 依次读取用例文件传到 MinIO
                case_index = 1
                while True:
                    in_file = os.path.join(tc_path, f"{case_index}.in")
                    ans_file = os.path.join(tc_path, f"{case_index}.ans")

                    # 用例断层，说明这个题目的用例读完了
                    if not os.path.exists(in_file) or not os.path.exists(ans_file):
                        break

                    upload_testcase(pid, case_index, in_file, ans_file, 10) # 默认每条用例10分
                    case_index += 1

    print(f"\n🎉 题库自动部署完毕！成功导入 {success_count} 道真实题目。")

if __name__ == "__main__":
    main()
