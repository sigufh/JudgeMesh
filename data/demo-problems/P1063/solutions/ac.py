def main():
    n = int(input())
    a = list(map(int, input().split()))
    # 破环成链
    val = a + a
    dp = [[0] * (2 * n) for _ in range(2 * n)]
    # 区间 DP
    for length in range(2, n + 1):
        for i in range(2 * n - length + 1):
            j = i + length - 1
            for k in range(i, j):
                dp[i][j] = max(dp[i][j], dp[i][k] + dp[k + 1][j] + val[i] * val[k + 1] * val[j + 1])
    ans = 0
    for i in range(n):
        ans = max(ans, dp[i][i + n - 1])
    print(ans)

if __name__ == "__main__":
    main()