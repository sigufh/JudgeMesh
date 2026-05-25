import sys

def main():
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    n = int(data[0])
    k = int(data[1])
    books = []
    idx = 2
    for _ in range(n):
        h = int(data[idx])
        w = int(data[idx + 1])
        books.append((h, w))
        idx += 2
    books.sort(key=lambda x: x[0])
    w = [b[1] for b in books]
    m = n - k
    INF = 10**9
    dp = [[INF] * n for _ in range(m + 1)]
    for i in range(n):
        dp[1][i] = 0
    for length in range(2, m + 1):
        for i in range(n):
            for j in range(i):
                if dp[length - 1][j] != INF:
                    dp[length][i] = min(dp[length][i], dp[length - 1][j] + abs(w[i] - w[j]))
    ans = min(dp[m][i] for i in range(n))
    print(ans)

if __name__ == "__main__":
    main()