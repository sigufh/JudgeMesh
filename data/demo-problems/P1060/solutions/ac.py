n, m = map(int, input().split())
v = []
p = []
for _ in range(m):
    vi, pi = map(int, input().split())
    v.append(vi)
    p.append(pi)

dp = [0] * (n + 1)
for i in range(m):
    value = v[i] * p[i]
    for j in range(n, v[i] - 1, -1):
        if dp[j - v[i]] + value > dp[j]:
            dp[j] = dp[j - v[i]] + value

print(dp[n])