import sys
sys.setrecursionlimit(10000)

def main():
    n = int(sys.stdin.readline())
    scores = [0] + list(map(int, sys.stdin.readline().split()))
    
    # dp[i][j] 表示区间 [i, j] 的最大加分
    dp = [[0] * (n + 2) for _ in range(n + 2)]
    root = [[0] * (n + 2) for _ in range(n + 2)]
    
    # 初始化单个节点
    for i in range(1, n + 1):
        dp[i][i] = scores[i]
        root[i][i] = i
    
    # 区间 DP
    for length in range(2, n + 1):
        for i in range(1, n - length + 2):
            j = i + length - 1
            dp[i][j] = 0
            for k in range(i, j + 1):
                left = 1 if k == i else dp[i][k - 1]
                right = 1 if k == j else dp[k + 1][j]
                val = left * right + scores[k]
                if val > dp[i][j]:
                    dp[i][j] = val
                    root[i][j] = k
    
    print(dp[1][n])
    
    # 递归构建前序遍历
    preorder = []
    def dfs(l, r):
        if l > r:
            return
        rt = root[l][r]
        preorder.append(rt)
        dfs(l, rt - 1)
        dfs(rt + 1, r)
    
    dfs(1, n)
    print(' '.join(map(str, preorder)))

if __name__ == "__main__":
    main()