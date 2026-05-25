def solve():
    a, n, m, x = map(int, input().split())

    # 系数 A 和 B，使得 up[i] = A[i]*a + B[i]*t
    A = [0] * (n + 1)
    B = [0] * (n + 1)
    A[1] = 1
    B[1] = 0
    A[2] = 0
    B[2] = 1
    for i in range(3, n + 1):
        A[i] = A[i - 1] + A[i - 2]
        B[i] = B[i - 1] + B[i - 2]

    # 第 n 站下车人数 = 第 n-1 站上车人数 = m
    # m = A[n-1]*a + B[n-1]*t
    if B[n - 1] != 0:
        t = (m - A[n - 1] * a) // B[n - 1]
    else:
        t = 0

    # 第 x 站开出时车上人数 = a + up[x] - t
    up_x = A[x] * a + B[x] * t
    ans = a + up_x - t
    print(ans)


if __name__ == "__main__":
    solve()