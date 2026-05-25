import sys

def dfs(cur, oil, cost):
    global ans
    if cur == N + 1:
        need = (S - D[cur]) / L
        if oil >= need:
            ans = min(ans, cost)
        return
    dist = D[cur + 1] - D[cur]
    need = dist / L
    if need > C:
        return
    # 在当前站加油，步长0.01
    add = 0.0
    while add <= C - oil + 1e-9:
        if oil + add >= need:
            new_oil = oil + add - need
            new_cost = cost + add * P[cur]
            dfs(cur + 1, new_oil, new_cost)
        add += 0.01

if __name__ == "__main__":
    data = sys.stdin.read().strip().split()
    if not data:
        exit()
    S = float(data[0]); C = float(data[1]); L = float(data[2]); P0 = float(data[3]); N = int(data[4])
    D = [0.0] * (N + 2)
    P = [0.0] * (N + 2)
    D[0] = 0.0; P[0] = P0
    idx = 5
    for i in range(1, N + 1):
        D[i] = float(data[idx]); P[i] = float(data[idx + 1])
        idx += 2
    D[N + 1] = S; P[N + 1] = 0.0
    # 检查可达性
    for i in range(N + 1):
        if D[i + 1] - D[i] > C * L + 1e-9:
            print("No Solution")
            sys.exit()
    ans = float('inf')
    dfs(0, 0.0, 0.0)
    if ans == float('inf'):
        print("No Solution")
    else:
        print("{:.2f}".format(ans))