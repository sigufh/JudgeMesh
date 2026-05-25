def main():
    n = int(input())
    dp = [0] * (n + 1)
    dp[1] = 1
    for i in range(2, n + 1):
        total = 1  # 数列只有 i 本身
        for j in range(1, i // 2 + 1):
            total += dp[j]
        dp[i] = total
    print(dp[n])

if __name__ == "__main__":
    main()