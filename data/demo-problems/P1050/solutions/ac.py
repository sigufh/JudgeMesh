def solve():
    s, k = input().split()
    k = int(k)
    # 提取后 k 位，高位用 0 补齐
    n = int(s[-k:]) if k > 0 else 0   # 相当于 C++ 的倒序存储
    mod = 10 ** k

    ans = 1
    mul = n      # 当前乘数

    for i in range(k):
        tmp = n
        found = False
        for j in range(1, 11):
            tmp = (tmp * mul) % mod
            # 检查第 i 位（从右往左数，i=0是个位）是否相等
            if (tmp // (10 ** i)) % 10 == (n // (10 ** i)) % 10:
                ans *= j
                # 更新 mul = mul^j (mod mod)
                mul = pow(mul, j, mod)
                found = True
                break
        if not found:
            print(-1)
            return

    print(ans)

if __name__ == "__main__":
    solve()
