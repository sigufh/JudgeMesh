import sys

def solve():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    L = int(next(it))
    S = int(next(it))
    T = int(next(it))
    M = int(next(it))
    stones = [int(next(it)) for _ in range(M)]
    
    if S == T:
        ans = sum(1 for pos in stones if pos % S == 0)
        print(ans)
        return
    
    stones.sort()
    # 路径压缩
    newL = 0
    last = 0
    stone_flag = {}
    for pos in stones:
        dist = pos - last
        if dist > 100:
            dist = 100
        newL += dist
        stone_flag[newL] = 1
        last = pos
    dist = L - last
    if dist > 100:
        dist = 100
    newL += dist
    
    INF = 10**9
    dp = [INF] * (newL + T + 1)
    dp[0] = 0
    for i in range(1, newL + T + 1):
        for j in range(S, T + 1):
            if i - j >= 0:
                add = stone_flag.get(i, 0)
                dp[i] = min(dp[i], dp[i - j] + add)
    
    ans = min(dp[newL:newL + T + 1])
    print(ans)

if __name__ == "__main__":
    solve()