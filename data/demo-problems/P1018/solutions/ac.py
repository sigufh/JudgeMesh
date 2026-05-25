def multiply_str(a, b):
    """字符串表示的大数乘法"""
    res = [0] * (len(a) + len(b))
    a = a[::-1]
    b = b[::-1]
    for i in range(len(a)):
        for j in range(len(b)):
            res[i + j] += int(a[i]) * int(b[j])
            res[i + j + 1] += res[i + j] // 10
            res[i + j] %= 10
    while len(res) > 1 and res[-1] == 0:
        res.pop()
    return ''.join(map(str, res[::-1]))

def compare_str(a, b):
    """比较两个大数字符串的大小"""
    if len(a) != len(b):
        return len(a) < len(b)
    return a < b

def main():
    N, K = map(int, input().split())
    num = input().strip()
    
    # dp[i][j] 表示前i个数字插入j个乘号的最大乘积（字符串形式）
    dp = [["0"] * (K + 1) for _ in range(N + 1)]
    
    # 初始化：没有乘号的情况
    for i in range(1, N + 1):
        dp[i][0] = num[:i]
    
    # 动态规划
    for i in range(1, N + 1):
        for j in range(1, K + 1):
            if j >= i:
                continue
            dp[i][j] = "0"
            for k in range(j, i):
                # 最后一段数字从k到i-1（索引）
                last = num[k:i]
                prod = multiply_str(dp[k][j-1], last)
                if compare_str(dp[i][j], prod):
                    dp[i][j] = prod
    
    print(dp[N][K])

if __name__ == "__main__":
    main()