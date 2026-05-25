def solve():
    import sys
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    it = iter(data)
    n = int(next(it)) // 10
    m = int(next(it))
    items = [None]  # 1-indexed
    for _ in range(m):
        v = int(next(it)) // 10
        p = int(next(it))
        q = int(next(it))
        items.append((v, p, q))
    
    # 分组
    groups = [[] for _ in range(m + 1)]
    for i in range(1, m + 1):
        v, p, q = items[i]
        if q == 0:
            groups[i].append(i)
        else:
            groups[q].append(i)
    
    dp = [0] * (n + 1)
    for i in range(1, m + 1):
        if not groups[i]:
            continue
        master = groups[i][0]
        master_v, master_p, _ = items[master]
        master_w = master_v * master_p
        
        attach = []
        for idx in groups[i][1:]:
            v, p, _ = items[idx]
            attach.append((v, v * p))
        
        # 生成所有组合
        options = [(master_v, master_w)]
        if len(attach) >= 1:
            v1, w1 = attach[0]
            options.append((master_v + v1, master_w + w1))
        if len(attach) >= 2:
            v2, w2 = attach[1]
            options.append((master_v + v2, master_w + w2))
            options.append((master_v + v1 + v2, master_w + w1 + w2))
        
        # 分组背包
        for j in range(n, -1, -1):
            for v, w in options:
                if j >= v:
                    if dp[j - v] + w > dp[j]:
                        dp[j] = dp[j - v] + w
    
    print(dp[n] * 10)

if __name__ == "__main__":
    solve()