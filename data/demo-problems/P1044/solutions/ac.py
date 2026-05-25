import math

n = int(input())
# 卡特兰数 C(2n, n) // (n + 1)
ans = math.comb(2 * n, n) // (n + 1)
print(ans)