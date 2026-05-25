def overlap(a, b):
    """返回a和b的重叠长度，如果无法连接返回0"""
    len_a, len_b = len(a), len(b)
    for i in range(1, min(len_a, len_b)):
        if a[-i:] == b[:i]:
            return i
    return 0

def dfs(current):
    global max_len
    max_len = max(max_len, len(current))
    for i in range(n):
        if used[i] >= 2:
            continue
        ov = overlap(current, words[i])
        if ov > 0:
            used[i] += 1
            dfs(current + words[i][ov:])
            used[i] -= 1

n = int(input())
words = [input().strip() for _ in range(n)]
start = input().strip()
used = [0] * n
max_len = 0

for i in range(n):
    if words[i][0] == start:
        used[i] += 1
        dfs(words[i])
        used[i] -= 1

print(max_len)