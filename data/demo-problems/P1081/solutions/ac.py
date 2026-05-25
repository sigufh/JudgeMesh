import sys
import math

def solve():
    input = sys.stdin.readline
    n = int(input().strip())
    h = [0] + list(map(int, input().split()))
    x0 = int(input().strip())
    m = int(input().strip())
    queries = []
    for _ in range(m):
        s, x = map(int, input().split())
        queries.append((s, x))
    
    # 预处理每个城市的下一个目的地
    cities = [(h[i], i) for i in range(1, n+1)]
    cities.sort()
    pos = [0] * (n+1)
    sorted_idx = [0] * (n+1)
    for i, (_, idx) in enumerate(cities, 1):
        pos[idx] = i
        sorted_idx[i] = idx
    
    pre = [0] * (n+2)
    nxt = [0] * (n+2)
    for i in range(1, n+1):
        pre[i] = i-1
        nxt[i] = i+1
    nxt[n] = 0
    pre[1] = 0
    
    nextA = [0] * (n+1)
    nextB = [0] * (n+1)
    distA = [0] * (n+1)
    distB = [0] * (n+1)
    
    for i in range(1, n+1):
        p = pos[i]
        candidates = []
        if pre[p] != 0:
            candidates.append(sorted_idx[pre[p]])
            if pre[pre[p]] != 0:
                candidates.append(sorted_idx[pre[pre[p]]])
        if nxt[p] != 0:
            candidates.append(sorted_idx[nxt[p]])
            if nxt[nxt[p]] != 0:
                candidates.append(sorted_idx[nxt[nxt[p]]])
        candidates.sort(key=lambda j: (abs(h[i]-h[j]), h[j]))
        if len(candidates) >= 1:
            nextB[i] = candidates[0]
            distB[i] = abs(h[i] - candidates[0])
        if len(candidates) >= 2:
            nextA[i] = candidates[1]
            distA[i] = abs(h[i] - candidates[1])
        # 删除当前节点
        if pre[p] != 0:
            nxt[pre[p]] = nxt[p]
        if nxt[p] != 0:
            pre[nxt[p]] = pre[p]
    
    # 倍增表
    LOG = 17
    f = [[0]*(n+1) for _ in range(LOG)]
    da = [[0]*(n+1) for _ in range(LOG)]
    db = [[0]*(n+1) for _ in range(LOG)]
    
    for i in range(1, n+1):
        if nextA[i] != 0 and nextB[nextA[i]] != 0:
            f[0][i] = nextB[nextA[i]]
            da[0][i] = distA[i]
            db[0][i] = distB[nextA[i]]
    
    for k in range(1, LOG):
        for i in range(1, n+1):
            if f[k-1][i] != 0:
                f[k][i] = f[k-1][f[k-1][i]]
                da[k][i] = da[k-1][i] + da[k-1][f[k-1][i]]
                db[k][i] = db[k-1][i] + db[k-1][f[k-1][i]]
    
    def query(s, X):
        totA = 0
        totB = 0
        cur = s
        # 第一步：小A
        if nextA[cur] != 0 and distA[cur] <= X:
            totA += distA[cur]
            X -= distA[cur]
            cur = nextA[cur]
        else:
            return totA, totB
        # 倍增
        for k in range(LOG-1, -1, -1):
            if f[k][cur] != 0 and da[k][cur] + db[k][cur] <= X:
                totA += da[k][cur]
                totB += db[k][cur]
                X -= da[k][cur] + db[k][cur]
                cur = f[k][cur]
        # 可能剩余小B一步
        if nextB[cur] != 0 and distB[cur] <= X:
            totB += distB[cur]
            X -= distB[cur]
            cur = nextB[cur]
        return totA, totB
    
    # 问题1
    best_s = 0
    best_ratio = float('inf')
    for i in range(1, n+1):
        a, b = query(i, x0)
        if b == 0:
            ratio = float('inf')
        else:
            ratio = a / b
        if ratio < best_ratio - 1e-9:
            best_ratio = ratio
            best_s = i
        elif abs(ratio - best_ratio) < 1e-9:
            if h[i] > h[best_s]:
                best_s = i
    print(best_s)
    
    # 问题2
    for s, x in queries:
        a, b = query(s, x)
        print(a, b)

if __name__ == "__main__":
    solve()