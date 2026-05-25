import sys
from collections import deque

def main() -> None:
    data = list(map(int, sys.stdin.buffer.read().split()))
    it = iter(data)
    n = next(it)
    m = next(it)
    p = next(it)

    # f[time][pos], 1-indexed for time and pos, with extra row/col for zeros
    f = [[0] * (n + 1) for _ in range(m + 1)]
    for pos in range(1, n + 1):
        for time in range(1, m + 1):
            val = next(it)
            f[time][pos] = val + f[time - 1][pos - 1]

    cost = [next(it) for _ in range(n)]

    # queues for each residue class (0..n-1)
    queues = [deque() for _ in range(n)]
    for i in range(n):
        # initial element: time 0, value -cost[i]
        queues[i].append((0, -cost[i]))

    dp = [-10**18] * (m + 1)
    dp[0] = 0
    add = [0] * n

    for i in range(1, m + 1):
        # first pass: compute dp[i] using current queues
        for j in range(n):
            rid = (j - i) % n
            q = queues[rid]
            # remove expired entries (time + p < i)
            while q and q[0][0] + p < i:
                q.popleft()
            if j == 0:
                add[rid] += f[i][n]
            if q:
                cand = q[0][1] + add[rid] + f[i][j]
                if cand > dp[i]:
                    dp[i] = cand

        # second pass: update queues with the new dp[i]
        for j in range(n):
            rid = (j - i) % n
            q = queues[rid]
            tmp = dp[i] - add[rid] - f[i][j] - cost[j]
            # maintain decreasing order of values
            while q and q[-1][1] <= tmp:
                q.pop()
            q.append((i, tmp))

    print(dp[m])

if __name__ == "__main__":
    main()
