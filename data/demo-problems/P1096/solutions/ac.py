n = int(input())
# A_n = 2^{n+1} - 2
ans = (1 << (n + 1)) - 2
print(ans)