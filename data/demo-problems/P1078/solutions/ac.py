import sys
sys.setrecursionlimit(10000)

def solve():
    N, K, M, S, T = map(int, sys.stdin.readline().split())
    culture = [0] + list(map(int, sys.stdin.readline().split()))
    reject = [[0] * (K + 1) for _ in range(K + 1)]
    for i in range(1, K + 1):
        row = list(map(int, sys.stdin.readline().split()))
        for j in range(1, K + 1):
            reject[i][j] = row[j - 1]
    graph = [[] for _ in range(N + 1)]
    for _ in range(M):
        u, v, d = map(int, sys.stdin.readline().split())
        graph[u].append((v, d))
        graph[v].append((u, d))
    
    ans = float('inf')
    visited = [False] * (N + 1)
    path_cultures = []
    
    def dfs(u, cost):
        nonlocal ans
        if u == T:
            ans = min(ans, cost)
            return
        if cost >= ans:
            return
        for v, w in graph[u]:
            if visited[v]:
                continue
            cul_v = culture[v]
            if cul_v in path_cultures:
                continue
            conflict = False
            for c in path_cultures:
                if reject[cul_v][c] or reject[c][cul_v]:
                    conflict = True
                    break
            if conflict:
                continue
            visited[v] = True
            path_cultures.append(cul_v)
            dfs(v, cost + w)
            path_cultures.pop()
            visited[v] = False
    
    visited[S] = True
    path_cultures.append(culture[S])
    dfs(S, 0)
    if ans == float('inf'):
        print(-1)
    else:
        print(ans)

if __name__ == "__main__":
    solve()