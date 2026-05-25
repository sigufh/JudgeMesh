import sys
import math

def factorize(n):
    factors = []
    i = 2
    while i * i <= n:
        if n % i == 0:
            cnt = 0
            while n % i == 0:
                n //= i
                cnt += 1
            factors.append((i, cnt))
        i += 1
    if n > 1:
        factors.append((n, 1))
    return factors

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    N = int(next(it))
    m1 = int(next(it))
    m2 = int(next(it))
    S = [int(next(it)) for _ in range(N)]

    # 对 M = m1^m2 进行质因数分解
    factors_m = factorize(m1)
    # 指数乘以 m2
    factors_m = [(p, cnt * m2) for p, cnt in factors_m]

    ans = float('inf')
    for si in S:
        ok = True
        max_time = 0
        temp = si
        for p, need in factors_m:
            cnt = 0
            while temp % p == 0:
                temp //= p
                cnt += 1
            if cnt == 0:
                ok = False
                break
            # 需要的时间为 ceil(need / cnt)
            time = (need + cnt - 1) // cnt
            if time > max_time:
                max_time = time
        if ok:
            if max_time < ans:
                ans = max_time

    if ans == float('inf'):
        print(-1)
    else:
        print(ans)

if __name__ == "__main__":
    main()