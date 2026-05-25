MOD = 1000007

def main():
    n, m = map(int, input().split())
    a = list(map(int, input().split()))
    
    dp = [0] * (m + 1)
    dp[0] = 1  # 0盆花有一种方案
    
    for i in range(n):
        # 从后往前更新，避免重复使用当前种类
        for j in range(m, -1, -1):
            if dp[j] == 0:
                continue
            # 枚举当前第i种花摆多少盆
            for k in range(1, a[i] + 1):
                if j + k > m:
                    break
                dp[j + k] = (dp[j + k] + dp[j]) % MOD
    
    print(dp[m])

if __name__ == "__main__":
    main()