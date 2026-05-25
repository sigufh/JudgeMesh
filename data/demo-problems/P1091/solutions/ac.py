n = int(input())
t = list(map(int, input().split()))

# dp1[i]: 以i结尾的最长上升子序列长度
dp1 = [1] * n
for i in range(n):
    for j in range(i):
        if t[j] < t[i]:
            dp1[i] = max(dp1[i], dp1[j] + 1)

# dp2[i]: 以i开头的最长下降子序列长度
dp2 = [1] * n
for i in range(n - 1, -1, -1):
    for j in range(n - 1, i, -1):
        if t[j] < t[i]:
            dp2[i] = max(dp2[i], dp2[j] + 1)

max_len = 0
for i in range(n):
    max_len = max(max_len, dp1[i] + dp2[i] - 1)

print(n - max_len)