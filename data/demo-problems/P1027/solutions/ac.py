import sys
import math

def dist(p1, p2):
    return math.hypot(p1[0] - p2[0], p1[1] - p2[1])

def get_fourth(p1, p2, p3):
    # 判断直角顶点
    d12 = dist(p1, p2)
    d13 = dist(p1, p3)
    d23 = dist(p2, p3)
    if abs(d12**2 + d13**2 - d23**2) < 1e-6:
        # p1是直角顶点
        return (p2[0] + p3[0] - p1[0], p2[1] + p3[1] - p1[1])
    if abs(d12**2 + d23**2 - d13**2) < 1e-6:
        # p2是直角顶点
        return (p1[0] + p3[0] - p2[0], p1[1] + p3[1] - p2[1])
    # p3是直角顶点
    return (p1[0] + p2[0] - p3[0], p1[1] + p2[1] - p3[1])

def solve():
    input_data = sys.stdin.read().strip().split()
    if not input_data:
        return
    it = iter(input_data)
    n = int(next(it))
    out_lines = []
    for _ in range(n):
        S = int(next(it))
        t = float(next(it))
        A = int(next(it)) - 1
        B = int(next(it)) - 1
        cities = []
        T = []
        for i in range(S):
            x1 = float(next(it)); y1 = float(next(it))
            x2 = float(next(it)); y2 = float(next(it))
            x3 = float(next(it)); y3 = float(next(it))
            ti = float(next(it))
            T.append(ti)
            p1 = (x1, y1)
            p2 = (x2, y2)
            p3 = (x3, y3)
            p4 = get_fourth(p1, p2, p3)
            cities.append([p1, p2, p3, p4])
        total = S * 4
        INF = 1e18
        g = [[INF] * total for _ in range(total)]
        for i in range(total):
            g[i][i] = 0
        # 建图
        for i in range(S):
            # 城市内高铁
            for a in range(4):
                for b in range(a + 1, 4):
                    d = dist(cities[i][a], cities[i][b]) * T[i]
                    u = i * 4 + a
                    v = i * 4 + b
                    if d < g[u][v]:
                        g[u][v] = g[v][u] = d
            # 城市间飞机
            for j in range(i + 1, S):
                for a in range(4):
                    for b in range(4):
                        d = dist(cities[i][a], cities[j][b]) * t
                        u = i * 4 + a
                        v = j * 4 + b
                        if d < g[u][v]:
                            g[u][v] = g[v][u] = d
        # Floyd
        for k in range(total):
            gk = g[k]
            for i in range(total):
                if g[i][k] == INF:
                    continue
                gi = g[i]
                gik = gi[k]
                for j in range(total):
                    if gk[j] == INF:
                        continue
                    newd = gik + gk[j]
                    if newd < gi[j]:
                        gi[j] = newd
        ans = INF
        for a in range(4):
            for b in range(4):
                u = A * 4 + a
                v = B * 4 + b
                if g[u][v] < ans:
                    ans = g[u][v]
        out_lines.append(f"{ans:.1f}")
    sys.stdout.write("\n".join(out_lines))

if __name__ == "__main__":
    solve()