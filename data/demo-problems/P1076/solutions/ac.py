import sys

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    N = int(next(it))
    M = int(next(it))
    stairs = [[0] * M for _ in range(N)]
    signs = [[0] * M for _ in range(N)]
    for i in range(N):
        for j in range(M):
            stairs[i][j] = int(next(it))
            signs[i][j] = int(next(it))
    start = int(next(it))
    
    MOD = 20123
    ans = 0
    for i in range(N):
        ans = (ans + signs[i][start]) % MOD
        x = signs[i][start]
        cnt = sum(stairs[i])
        steps = (x - 1) % cnt + 1
        pos = start
        while True:
            if stairs[i][pos] == 1:
                steps -= 1
                if steps == 0:
                    break
            pos = (pos + 1) % M
        start = pos
    print(ans % MOD)

if __name__ == "__main__":
    main()