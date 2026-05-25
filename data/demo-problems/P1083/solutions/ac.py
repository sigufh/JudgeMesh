import sys

def check(k, n, r, d, s, t):
    diff = [0] * (n + 2)
    for i in range(1, k + 1):
        diff[s[i]] += d[i]
        diff[t[i] + 1] -= d[i]
    cur = 0
    for i in range(1, n + 1):
        cur += diff[i]
        if cur > r[i]:
            return False
    return True

def main():
    input = sys.stdin.read
    data = input().split()
    idx = 0
    n = int(data[idx]); idx += 1
    m = int(data[idx]); idx += 1
    r = [0] * (n + 1)
    for i in range(1, n + 1):
        r[i] = int(data[idx]); idx += 1
    d = [0] * (m + 1)
    s = [0] * (m + 1)
    t = [0] * (m + 1)
    for i in range(1, m + 1):
        d[i] = int(data[idx]); idx += 1
        s[i] = int(data[idx]); idx += 1
        t[i] = int(data[idx]); idx += 1
    
    if check(m, n, r, d, s, t):
        print(0)
        return
    
    left, right = 1, m
    ans = 0
    while left <= right:
        mid = (left + right) // 2
        if not check(mid, n, r, d, s, t):
            ans = mid
            right = mid - 1
        else:
            left = mid + 1
    print(-1)
    print(ans)

if __name__ == "__main__":
    main()