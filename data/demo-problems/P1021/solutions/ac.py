import sys

def calc_continuous(stamps, N):
    max_val = stamps[-1] * N
    dp = [float('inf')] * (max_val + 1)
    dp[0] = 0
    for i in range(1, max_val + 1):
        for s in stamps:
            if i >= s and dp[i - s] != float('inf'):
                dp[i] = min(dp[i], dp[i - s] + 1)
    cnt = 0
    for i in range(1, max_val + 1):
        if dp[i] <= N:
            cnt += 1
        else:
            break
    return cnt

def dfs(idx, prev, current, N, K, best_stamps, max_continuous):
    if idx == K:
        cont = calc_continuous(current, N)
        if cont > max_continuous[0]:
            max_continuous[0] = cont
            best_stamps[0] = current[:]
        return
    max_next = 1 if idx == 0 else calc_continuous(current, N) + 1
    for v in range(prev + 1, max_next + 1):
        current.append(v)
        dfs(idx + 1, v, current, N, K, best_stamps, max_continuous)
        current.pop()

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    N = int(data[0])
    K = int(data[1])
    best_stamps = [[]]
    max_continuous = [0]
    dfs(0, 0, [], N, K, best_stamps, max_continuous)
    print(' '.join(map(str, best_stamps[0])))
    print(f"MAX={max_continuous[0]}")

if __name__ == "__main__":
    main()