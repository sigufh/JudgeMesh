import sys

def solve():
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    it = iter(data)
    n = int(next(it))
    k = int(next(it))
    points = []
    for _ in range(n):
        xi = int(next(it))
        yi = int(next(it))
        points.append((xi, yi))
    
    # 按x坐标排序，x相同按y排序
    points.sort(key=lambda p: (p[0], p[1]))
    x = [0] + [p[0] for p in points]
    y = [0] + [p[1] for p in points]
    
    INF = 10**9
    dp = [[INF] * (k + 1) for _ in range(n + 1)]
    dp[0][0] = 0
    
    # 预处理面积
    area = [[0] * (n + 1) for _ in range(n + 1)]
    for i in range(1, n + 1):
        minx = maxx = x[i]
        miny = maxy = y[i]
        for j in range(i, n + 1):
            minx = min(minx, x[j])
            maxx = max(maxx, x[j])
            miny = min(miny, y[j])
            maxy = max(maxy, y[j])
            area[i][j] = (maxx - minx) * (maxy - miny)
    
    # DP
    for i in range(1, n + 1):
        for j in range(1, k + 1):
            for p in range(i):
                if dp[p][j-1] != INF:
                    dp[i][j] = min(dp[i][j], dp[p][j-1] + area[p+1][i])
    
    print(dp[n][k])

if __name__ == "__main__":
    solve()