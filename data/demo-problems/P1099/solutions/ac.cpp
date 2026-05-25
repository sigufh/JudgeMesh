#include <bits/stdc++.h>
using namespace std;

const int INF = 1e9;

int main() {
    int n, s;
    cin >> n >> s;
    vector<vector<pair<int, int>>> adj(n + 1);
    for (int i = 0; i < n - 1; i++) {
        int u, v, w;
        cin >> u >> v >> w;
        adj[u].push_back({v, w});
        adj[v].push_back({u, w});
    }

    // 计算所有点对之间的最短距离
    vector<vector<int>> dist(n + 1, vector<int>(n + 1, INF));
    for (int i = 1; i <= n; i++) {
        dist[i][i] = 0;
        for (auto &p : adj[i]) {
            dist[i][p.first] = p.second;
        }
    }
    for (int k = 1; k <= n; k++) {
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (dist[i][k] != INF && dist[k][j] != INF) {
                    dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j]);
                }
            }
        }
    }

    // 找到直径端点
    int maxDist = 0, u = 1, v = 1;
    for (int i = 1; i <= n; i++) {
        for (int j = i + 1; j <= n; j++) {
            if (dist[i][j] > maxDist) {
                maxDist = dist[i][j];
                u = i;
                v = j;
            }
        }
    }

    // 提取直径上的结点序列
    vector<int> path;
    function<bool(int, int, int)> dfs = [&](int cur, int target, int parent) -> bool {
        path.push_back(cur);
        if (cur == target) return true;
        for (auto &p : adj[cur]) {
            int nxt = p.first;
            if (nxt == parent) continue;
            if (dfs(nxt, target, cur)) return true;
        }
        path.pop_back();
        return false;
    };
    dfs(u, v, 0);

    int m = path.size();
    // 计算直径上每个点到其他点的最远距离
    vector<int> maxToNode(n + 1, 0);
    for (int i = 1; i <= n; i++) {
        int minDist = INF;
        for (int x : path) {
            minDist = min(minDist, dist[i][x]);
        }
        maxToNode[i] = minDist;
    }

    int ans = INF;
    // 枚举核的起点和终点在直径上的位置
    for (int i = 0; i < m; i++) {
        for (int j = i; j < m; j++) {
            // 计算路径长度
            int len = dist[path[i]][path[j]];
            if (len > s) continue;
            // 计算偏心距
            int ecc = 0;
            for (int k = 1; k <= n; k++) {
                int d = INF;
                for (int t = i; t <= j; t++) {
                    d = min(d, dist[k][path[t]]);
                }
                ecc = max(ecc, d);
            }
            ans = min(ans, ecc);
        }
    }

    cout << ans << endl;
    return 0;
}