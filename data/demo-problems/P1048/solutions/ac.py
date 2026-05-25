def main():
    import sys
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    T = int(data[0])
    M = int(data[1])
    idx = 2
    time = []
    value = []
    for _ in range(M):
        time.append(int(data[idx]))
        value.append(int(data[idx + 1]))
        idx += 2
    dp = [0] * (T + 1)
    for i in range(M):
        for j in range(T, time[i] - 1, -1):
            if dp[j - time[i]] + value[i] > dp[j]:
                dp[j] = dp[j - time[i]] + value[i]
    print(dp[T])

if __name__ == "__main__":
    main()