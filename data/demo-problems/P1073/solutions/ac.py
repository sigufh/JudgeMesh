import sys
from collections import deque

def solve():
    input = sys.stdin.read
    data = input().split()
    idx = 0
    n = int(data[idx]); idx += 1
    m = int(data[idx]); idx += 1
    
    price = [0] * (n + 1)
    for i in range(1, n + 1):
        price[i] = int(data[idx]); idx += 1
    
    graph = [[] for _ in range(n + 1)]
    rev_graph = [[] for _ in range(n + 1)]
    
    for _ in range(m):
        x = int(data[idx]); idx += 1
        y = int(data[idx]); idx += 1
        z = int(data[idx]); idx += 1
        graph[x].append(y)
        rev_graph[y].append(x)
        if z == 2:
            graph[y].append(x)
            rev_graph[x].append(y)
    
    INF = 10**9
    min_price = [INF] * (n + 1)
    vis1 = [False] * (n + 1)
    
    q = deque()
    q.append(1)
    vis1[1] = True
    min_price[1] = price[1]
    
    while q:
        u = q.popleft()
        for v in graph[u]:
            if not vis1[v]:
                vis1[v] = True
                min_price[v] = min(min_price[u], price[v])
                q.append(v)
            else:
                if min_price[u] < min_price[v]:
                    min_price[v] = min_price[u]
                    q.append(v)
    
    max_price = [0] * (n + 1)
    vis2 = [False] * (n + 1)
    
    q.append(n)
    vis2[n] = True
    max_price[n] = price[n]
    
    while q:
        u = q.popleft()
        for v in rev_graph[u]:
            if not vis2[v]:
                vis2[v] = True
                max_price[v] = max(max_price[u], price[v])
                q.append(v)
            else:
                if max_price[u] > max_price[v]:
                    max_price[v] = max_price[u]
                    q.append(v)
    
    ans = 0
    for i in range(1, n + 1):
        if vis1[i] and vis2[i]:
            ans = max(ans, max_price[i] - min_price[i])
    
    print(ans)

if __name__ == "__main__":
    solve()