n, k = map(int, input().split())
# dp[i][j] 表示把整数 i 分成 j 份的方案数
dp = [[0] * (k + 1) for _ in range(n + 1)]
dp[0][0] = 1  # 0 分成 0 份有一种方案
for i in range(1, n + 1):
    for j in range(1, k + 1):
        if i >= j:
            # 两种情况：至少有一份是 1，或者所有份都大于 1
            dp[i][j] = dp[i - 1][j - 1] + dp[i - j][j]
print(dp[n][k])