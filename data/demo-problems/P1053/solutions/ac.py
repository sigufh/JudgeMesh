import sys

def solve():
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    n = int(data[0])
    wish = [[0, 0] for _ in range(n + 1)]
    idx = 1
    for i in range(1, n + 1):
        wish[i][0] = int(data[idx]); idx += 1
        wish[i][1] = int(data[idx]); idx += 1
    
    # 检查每个愿望是否相互
    for i in range(1, n + 1):
        a, b = wish[i]
        if not ((wish[a][0] == i or wish[a][1] == i) and (wish[b][0] == i or wish[b][1] == i)):
            print(-1)
            return
    
    # 构建目标环
    target = [0] * (n + 1)
    pos = [0] * (n + 1)
    target[1] = 1
    pos[1] = 1
    cur = 1
    prev = 1
    nxt = wish[1][0]
    for i in range(2, n + 1):
        target[i] = nxt
        pos[nxt] = i
        if wish[nxt][0] == prev:
            prev = nxt
            nxt = wish[nxt][1]
        else:
            prev = nxt
            nxt = wish[nxt][0]
    
    if nxt != 1:
        print(-1)
        return
    
    cnt1 = [0] * n
    cnt2 = [0] * n
    for i in range(1, n + 1):
        offset = (pos[i] - i) % n
        cnt1[offset] += 1
        # 逆时针：反转后的位置为 n - i + 1
        offset = (pos[i] - (n - i + 1)) % n
        cnt2[offset] += 1
    
    max_match = max(max(cnt1), max(cnt2))
    print(n - max_match)

if __name__ == "__main__":
    solve()