import sys

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    M = int(next(it))
    N = int(next(it))
    K = int(next(it))
    peanuts = []
    for i in range(1, M + 1):
        for j in range(1, N + 1):
            p = int(next(it))
            if p > 0:
                peanuts.append((p, i, j))
    peanuts.sort(reverse=True)
    if not peanuts:
        print(0)
        return
    time = 0
    ans = 0
    cur_x, cur_y = 0, peanuts[0][2]
    for i, (val, x, y) in enumerate(peanuts):
        dist = abs(x - cur_x) + abs(y - cur_y)
        pick_time = 1
        back_time = x
        if i == 0:
            if dist + pick_time + back_time <= K:
                time += dist + pick_time
                ans += val
                cur_x, cur_y = x, y
            else:
                break
        else:
            if time + dist + pick_time + back_time <= K:
                time += dist + pick_time
                ans += val
                cur_x, cur_y = x, y
            else:
                break
    print(ans)

if __name__ == "__main__":
    main()