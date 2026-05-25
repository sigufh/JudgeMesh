import sys
import bisect

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        print(0)
        print(0)
        return
    heights = list(map(int, data))
    n = len(heights)

    # 第一问：最长不上升子序列长度
    dp = []  # 维护递减序列
    for h in heights:
        # 在dp中找第一个小于h的位置，因为dp是递减的，所以用bisect_left找第一个<=h的位置？不对
        # 我们需要找第一个 < h 的位置，因为dp递减，所以用bisect_left找第一个 <= h 的位置，然后取反？
        # 实际上，对于递减序列，我们可以用负数技巧或者自定义key
        # 简单方法：将dp存为负数，然后找第一个 > -h 的位置，即第一个 < h 的位置
        # 或者使用bisect_left，但需要反转比较
        # 这里采用维护递减序列，使用bisect_left找第一个 <= h 的位置，但我们要找第一个 < h 的位置
        # 因为dp递减，所以第一个 <= h 的位置可能是等于h的，我们需要替换第一个小于h的，所以用bisect_left找第一个 <= h 的位置，如果等于h，则替换该位置？不对
        # 标准做法：对于不上升（即允许相等），我们维护递减序列，找第一个 < h 的位置替换
        # 使用bisect_left，但dp是递减的，所以需要传入reverse=True？Python的bisect不支持reverse
        # 简单方法：将dp存为负数，然后找第一个 > -h 的位置，即第一个 < h 的位置
        # 或者自己写二分
        # 这里采用负数技巧：
        pos = bisect.bisect_left(dp, -h)  # 因为dp存的是负数，找第一个 >= -h 的位置，即第一个 <= h 的位置？不对
        # 实际上，dp存的是递减序列，我们想找第一个 < h 的位置。如果dp存负数，则递减序列的负数变成递增序列。
        # 例如 heights: 5,3,3,2 -> dp负数: -5,-3,-3,-2 递增。我们要找第一个 < h 的位置，即第一个负数 > -h 的位置？因为 < h 等价于 -x > -h。
        # 所以找第一个 > -h 的位置，即bisect_right(dp, -h) 或者 bisect_left(dp, -h) 如果相等则跳过？我们想要严格小于h，所以找第一个 > -h 的位置，即bisect.bisect_right(dp, -h)
        # 但bisect_right返回的是插入点，即第一个 > -h 的位置索引。
        # 测试：h=3, -h=-3, dp=[-5,-3,-3,-2], bisect_right(dp, -3) 返回3（指向-2），正确，第一个<-3即-2的位置。
        # 如果h=4, -h=-4, bisect_right(dp, -4) 返回1（指向-3），正确，第一个<-4即-3的位置。
        # 所以用bisect_right
        pos = bisect.bisect_right(dp, -h)
        if pos == len(dp):
            dp.append(-h)
        else:
            dp[pos] = -h
    max_intercept = len(dp)

    # 第二问：最少系统数 = 最长上升子序列长度
    dp2 = []
    for h in heights:
        pos = bisect.bisect_left(dp2, h)
        if pos == len(dp2):
            dp2.append(h)
        else:
            dp2[pos] = h
    min_systems = len(dp2)

    print(max_intercept)
    print(min_systems)

if __name__ == "__main__":
    main()