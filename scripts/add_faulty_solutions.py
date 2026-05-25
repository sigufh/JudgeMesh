import os
import shutil

# 配置
base_dir = r"E:\2026JudgeMesh\GIT提交\JudgeMesh\data\demo-problems"
start_pid = 1010
end_pid = 1110

# 三个文件的内容
wa_content = '''# wa.py
# 故意输出错误答案
input()  # 读入第一行，防止 EOF 异常
print("-1")  # 永远输出 -1 作为错误答案
'''

tle_content = '''# tle.py
# 故意超时：死循环
import time

while True:
    pass
'''

ce_content = '''// ce.cpp
// 编译错误：缺少分号
#include <iostream>
int main() {
    std::cout << "Hello"  // 故意少写分号
    return 0;
}
'''

for pid in range(start_pid, end_pid + 1):
    problem_dir = os.path.join(base_dir, f"P{pid}")
    solutions_dir = os.path.join(problem_dir, "solutions")

    # 如果题目文件夹不存在，跳过（或可创建，但按题意应已存在）
    if not os.path.isdir(problem_dir):
        print(f"跳过不存在的目录: {problem_dir}")
        continue

    # 确保 solutions 目录存在
    os.makedirs(solutions_dir, exist_ok=True)

    # 写入 wa.py
    wa_path = os.path.join(solutions_dir, "wa.py")
    with open(wa_path, "w", encoding="utf-8") as f:
        f.write(wa_content)
    print(f"已写入: {wa_path}")

    # 写入 tle.py
    tle_path = os.path.join(solutions_dir, "tle.py")
    with open(tle_path, "w", encoding="utf-8") as f:
        f.write(tle_content)
    print(f"已写入: {tle_path}")

    # 写入 ce.cpp
    ce_path = os.path.join(solutions_dir, "ce.cpp")
    with open(ce_path, "w", encoding="utf-8") as f:
        f.write(ce_content)
    print(f"已写入: {ce_path}")

print("所有文件添加完成！")
