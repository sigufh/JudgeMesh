import sys
sys.setrecursionlimit(10000)

def dfs(u, p, d):
    parent[u] = p
    depth[u] = d
    sz[u] = 1
    for v in adj[u]:
        if v != p:
            dfs(v, u, d + 1)
            sz[u] += sz[v]

def solve(infected, total):
    global ans
    if not infected:
        ans = min(ans, total)
        return
    cuts = []
    for u in infected:
        for v in adj[u]:
            if v != parent[u]:
                cuts.append((u, v))
    if not cuts:
        ans = min(ans, total)
        return
    for u, v in cuts:
        next_infected = []
        for x in infected:
            for y in adj[x]:
                if y != parent[x] and not (x == u and y == v):
                    next_infected.append(y)
        solve(next_infected, total + sz[v])

n, p = map(int, input().split())
adj = [[] for _ in range(n + 1)]
for _ in range(p):
    u, v = map(int, input().split())
    adj[u].append(v)
    adj[v].append(u)

parent = [0] * (n + 1)
depth = [0] * (n + 1)
sz = [0] * (n + 1)
dfs(1, 0, 0)

ans = float('inf')
solve([1], 0)
print(ans)