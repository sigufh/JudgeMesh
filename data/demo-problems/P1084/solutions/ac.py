import sys
sys.setrecursionlimit(200000)

def solve():
    input = sys.stdin.readline
    n = int(input())
    adj = [[] for _ in range(n+1)]
    for _ in range(n-1):
        u, v, w = map(int, input().split())
        adj[u].append((v, w))
        adj[v].append((u, w))
    
    LOG = 20
    parent = [[0]*LOG for _ in range(n+1)]
    dist = [0]*(n+1)
    depth = [0]*(n+1)
    
    def dfs(u, p, d):
        parent[u][0] = p
        dist[u] = d
        depth[u] = depth[p] + 1
        for i in range(1, LOG):
            if parent[u][i-1] != 0:
                parent[u][i] = parent[parent[u][i-1]][i-1]
            else:
                parent[u][i] = 0
        for v, w in adj[u]:
            if v == p: continue
            dfs(v, u, d + w)
    
    dfs(1, 0, 0)
    
    m = int(input())
    army = list(map(int, input().split()))
    
    def check(limit):
        has_army = [False]*(n+1)
        free_army = []
        need_cover = []
        
        for u in army:
            if dist[u] <= limit:
                free_army.append((limit - dist[u], u))
            else:
                cur = u
                cur_dist = dist[u]
                for j in range(LOG-1, -1, -1):
                    if parent[cur][j] != 0 and cur_dist - dist[parent[cur][j]] <= limit:
                        cur_dist -= dist[cur] - dist[parent[cur][j]]
                        cur = parent[cur][j]
                has_army[cur] = True
        
        covered = [False]*(n+1)
        
        def mark(u, p):
            if has_army[u]:
                covered[u] = True
                return
            all_child_covered = True
            is_leaf = True
            for v, w in adj[u]:
                if v == p: continue
                is_leaf = False
                mark(v, u)
                if not covered[v]:
                    all_child_covered = False
            if not is_leaf and all_child_covered:
                covered[u] = True
        
        mark(1, 0)
        
        for v, w in adj[1]:
            if not covered[v]:
                need_cover.append((dist[v], v))
        
        free_army.sort()
        need_cover.sort()
        
        used = [False]*len(free_army)
        j = 0
        for nc_dist, nc_node in need_cover:
            ok = False
            for i in range(len(free_army)):
                if used[i]: continue
                rem, u = free_army[i]
                if dist[u] <= nc_dist:
                    used[i] = True
                    ok = True
                    break
            if not ok:
                for i in range(len(free_army)):
                    if used[i]: continue
                    rem, u = free_army[i]
                    if rem >= nc_dist:
                        used[i] = True
                        ok = True
                        break
            if not ok:
                return False
        return True
    
    left, right = 0, 10**15
    ans = -1
    while left <= right:
        mid = (left + right) // 2
        if check(mid):
            ans = mid
            right = mid - 1
        else:
            left = mid + 1
    
    print(ans)

if __name__ == "__main__":
    solve()