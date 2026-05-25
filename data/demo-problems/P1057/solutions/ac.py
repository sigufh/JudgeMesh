n, m = map(int, input().split())
dp = [[0] * n for _ in range(m + 1)]
dp[0][0] = 1
for i in range(1, m + 1):
    for j in range(n):
        left = (j - 1) % n
        right = (j + 1) % n
        dp[i][j] = dp[i-1][left] + dp[i-1][right]
print(dp[m][0])