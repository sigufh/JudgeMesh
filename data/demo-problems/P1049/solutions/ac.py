V = int(input())
n = int(input())
w = [int(input()) for _ in range(n)]

dp = [False] * (V + 1)
dp[0] = True

for weight in w:
    for j in range(V, weight - 1, -1):
        if dp[j - weight]:
            dp[j] = True

for j in range(V, -1, -1):
    if dp[j]:
        print(V - j)
        break