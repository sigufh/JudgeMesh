import sys
import math

def gcd(a, b):
    while b:
        a, b = b, a % b
    return a

def solve():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    n = int(next(it))
    out_lines = []
    for _ in range(n):
        a0 = int(next(it))
        a1 = int(next(it))
        b0 = int(next(it))
        b1 = int(next(it))
        p = a0 // a1
        q = b1 // b0
        ans = 0
        limit = int(math.isqrt(b1))
        for i in range(1, limit + 1):
            if b1 % i == 0:
                # 检查 i
                if i % a1 == 0 and gcd(i // a1, p) == 1 and gcd(q, b1 // i) == 1:
                    ans += 1
                j = b1 // i
                if j != i:
                    if j % a1 == 0 and gcd(j // a1, p) == 1 and gcd(q, b1 // j) == 1:
                        ans += 1
        out_lines.append(str(ans))
    sys.stdout.write("\n".join(out_lines))

if __name__ == "__main__":
    solve()