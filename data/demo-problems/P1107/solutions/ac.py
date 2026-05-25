import sys

def main():
    input = sys.stdin.read
    data = input().split()
    it = iter(data)
    N = int(next(it))
    H = int(next(it))
    Delta = int(next(it))

    # 存储每棵树每个高度的柿子数量
    tree = [[0] * (H + 1) for _ in range(N + 1)]
    for i in range(1, N + 1):
        Ni = int(next(it))
        for _ in range(Ni):
            h = int(next(it))
            tree[i][h] += 1

    # dp[i][h] 表示小猫在树i高度h时能吃到的最多柿子数
    dp = [[0] * (H + 1) for _ in range(N + 1)]
    # max_dp[h] 表示高度h时所有树中dp的最大值
    max_dp = [0] * (H + 1)

    # 从高到低处理高度
    for h in range(H, 0, -1):
        for i in range(1, N + 1):
            val = tree[i][h]
            if h < H:
                val += dp[i][h + 1]
            if h + Delta <= H:
                val = max(val, tree[i][h] + max_dp[h + Delta])
            dp[i][h] = val
            if dp[i][h] > max_dp[h]:
                max_dp[h] = dp[i][h]

    # 小猫可以从阳台跳到任意一棵树的树顶
    ans = max(dp[i][H] for i in range(1, N + 1))
    print(ans)

if __name__ == "__main__":
    main()