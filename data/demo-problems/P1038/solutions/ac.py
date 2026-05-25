from collections import deque

def main():
    n, p = map(int, input().split())
    neurons = [None] * (n + 1)
    for i in range(1, n + 1):
        c, u = map(int, input().split())
        is_input = (c != 0)
        if not is_input:
            c = -u
        neurons[i] = {'c': c, 'u': u, 'is_input': is_input}
    
    adj = [[] for _ in range(n + 1)]
    in_degree = [0] * (n + 1)
    out_degree = [0] * (n + 1)
    
    for _ in range(p):
        i, j, w = map(int, input().split())
        adj[i].append((j, w))
        in_degree[j] += 1
        out_degree[i] += 1
    
    q = deque()
    for i in range(1, n + 1):
        if in_degree[i] == 0:
            q.append(i)
    
    topo = []
    while q:
        u = q.popleft()
        topo.append(u)
        for v, w in adj[u]:
            in_degree[v] -= 1
            if in_degree[v] == 0:
                q.append(v)
    
    for u in topo:
        if neurons[u]['c'] > 0:
            for v, w in adj[u]:
                neurons[v]['c'] += w * neurons[u]['c']
    
    output = []
    for i in range(1, n + 1):
        if out_degree[i] == 0 and neurons[i]['c'] > 0:
            output.append((i, neurons[i]['c']))
    
    if not output:
        print("NULL")
    else:
        output.sort()
        for idx, val in output:
            print(idx, val)

if __name__ == "__main__":
    main()