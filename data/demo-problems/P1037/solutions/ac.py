from collections import defaultdict
import sys

sys.setrecursionlimit(10000)

def dfs(u, start, vis, cnt, g):
    vis[u] = True
    cnt[start] += 1
    for v in g[u]:
        if not vis[v]:
            dfs(v, start, vis, cnt, g)

def main():
    n, k = input().split()
    k = int(k)
    g = defaultdict(list)
    for _ in range(k):
        x, y = map(int, input().split())
        g[x].append(y)

    cnt = [0] * 10
    for i in range(10):
        vis = [False] * 10
        dfs(i, i, vis, cnt, g)

    ans = 1
    for ch in n:
        d = int(ch)
        if cnt[d] > 0:
            ans *= cnt[d]

    print(ans)

if __name__ == "__main__":
    main()