import sys
sys.setrecursionlimit(10000)

def main():
    n, s = map(int, sys.stdin.readline().split())
    adj = [[] for _ in range(n + 1)]
    for _ in range(n - 1):
        u, v, w = map(int, sys.stdin.readline().split())
        adj[u].append((v, w))
        adj[v].append((u, w))

    INF = 10**9
    dist = [[INF] * (n + 1) for _ in range(n + 1)]
    for i in range(1, n + 1):
        dist[i][i] = 0
        for v, w in adj[i]:
            dist[i][v] = w

    for k in range(1, n + 1):
        for i in range(1, n + 1):
            if dist[i][k] == INF:
                continue
            for j in range(1, n + 1):
                if dist[k][j] == INF:
                    continue
                if dist[i][k] + dist[k][j] < dist[i][j]:
                    dist[i][j] = dist[i][k] + dist[k][j]

    max_dist = 0
    u = v = 1
    for i in range(1, n + 1):
        for j in range(i + 1, n + 1):
            if dist[i][j] > max_dist:
                max_dist = dist[i][j]
                u, v = i, j

    path = []
    def dfs(cur, target, parent):
        path.append(cur)
        if cur == target:
            return True
        for nxt, _ in adj[cur]:
            if nxt == parent:
                continue
            if dfs(nxt, target, cur):
                return True
        path.pop()
        return False

    dfs(u, v, 0)

    m = len(path)
    ans = INF
    for i in range(m):
        for j in range(i, m):
            length = dist[path[i]][path[j]]
            if length > s:
                continue
            ecc = 0
            for k in range(1, n + 1):
                d = min(dist[k][path[t]] for t in range(i, j + 1))
                if d > ecc:
                    ecc = d
            if ecc < ans:
                ans = ecc

    print(ans)

if __name__ == "__main__":
    main()