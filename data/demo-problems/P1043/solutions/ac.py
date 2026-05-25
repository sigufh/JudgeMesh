import sys

def mod10(x):
    return ((x % 10) + 10) % 10

def solve():
    input_data = sys.stdin.read().strip().split()
    if not input_data:
        return
    it = iter(input_data)
    n = int(next(it))
    m = int(next(it))
    a = [int(next(it)) for _ in range(n)]
    
    a = a + a
    N = 2 * n
    prefix = [0] * (N + 1)
    for i in range(1, N + 1):
        prefix[i] = prefix[i - 1] + a[i - 1]
    
    INF = 10**9
    ans_min = INF
    ans_max = -INF
    
    for start in range(n):
        dp_min = [[[INF] * (m + 1) for _ in range(N)] for _ in range(N)]
        dp_max = [[[-INF] * (m + 1) for _ in range(N)] for _ in range(N)]
        
        for i in range(start, start + n):
            dp_min[i][i][1] = dp_max[i][i][1] = mod10(a[i])
        
        for length in range(2, n + 1):
            for i in range(start, start + n - length + 1):
                j = i + length - 1
                for k in range(2, min(m, length) + 1):
                    for p in range(i + k - 2, j):
                        if dp_min[i][p][k - 1] != INF:
                            val = mod10(prefix[j + 1] - prefix[p + 1])
                            dp_min[i][j][k] = min(dp_min[i][j][k], dp_min[i][p][k - 1] * val)
                            dp_max[i][j][k] = max(dp_max[i][j][k], dp_max[i][p][k - 1] * val)
        
        ans_min = min(ans_min, dp_min[start][start + n - 1][m])
        ans_max = max(ans_max, dp_max[start][start + n - 1][m])
    
    print(ans_min)
    print(ans_max)

if __name__ == "__main__":
    solve()