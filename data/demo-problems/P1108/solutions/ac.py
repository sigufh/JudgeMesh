import sys

def solve():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    n = int(data[0])
    a = list(map(int, data[1:1+n]))
    
    dp = [1] * n
    cnt = [1] * n
    
    max_len = 0
    for i in range(n):
        for j in range(i):
            if a[j] > a[i]:
                if dp[j] + 1 > dp[i]:
                    dp[i] = dp[j] + 1
                    cnt[i] = cnt[j]
                elif dp[j] + 1 == dp[i]:
                    cnt[i] += cnt[j]
        # Remove duplicates
        for j in range(i):
            if a[j] == a[i] and dp[j] == dp[i]:
                cnt[j] = 0
        if dp[i] > max_len:
            max_len = dp[i]
    
    total_cnt = 0
    for i in range(n):
        if dp[i] == max_len:
            total_cnt += cnt[i]
    
    print(f"{max_len} {total_cnt}")

if __name__ == "__main__":
    solve()