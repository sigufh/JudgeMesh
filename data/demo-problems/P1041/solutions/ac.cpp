#include <bits/stdc++.h>
using namespace std;

const int MAXN = 305;
vector<int> adj[MAXN];
int parent[MAXN];
int depth[MAXN];
int sz[MAXN];
int n, p;
int ans = INT_MAX;

void dfs(int u, int p, int d) {
    parent[u] = p;
    depth[u] = d;
    sz[u] = 1;
    for (int v : adj[u]) {
        if (v != p) {
            dfs(v, u, d + 1);
            sz[u] += sz[v];
        }
    }
}

void solve(vector<int> infected, int total) {
    if (infected.empty()) {
        ans = min(ans, total);
        return;
    }
    // 找出所有可以切断的边：感染节点到其未感染的孩子
    vector<pair<int,int>> cuts;
    for (int u : infected) {
        for (int v : adj[u]) {
            if (v != parent[u]) {
                cuts.push_back({u, v});
            }
        }
    }
    if (cuts.empty()) {
        ans = min(ans, total);
        return;
    }
    for (auto &cut : cuts) {
        int u = cut.first, v = cut.second;
        // 切断后，v及其子树不会被感染
        // 下一轮感染：当前感染节点中，除了u的孩子v被切断，其他孩子都会被感染
        vector<int> next_infected;
        for (int x : infected) {
            for (int y : adj[x]) {
                if (y != parent[x] && !(x == u && y == v)) {
                    next_infected.push_back(y);
                }
            }
        }
        solve(next_infected, total + sz[v]);
    }
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    cin >> n >> p;
    for (int i = 0; i < p; i++) {
        int u, v;
        cin >> u >> v;
        adj[u].push_back(v);
        adj[v].push_back(u);
    }
    dfs(1, 0, 0);
    vector<int> start = {1};
    solve(start, 0);
    cout << ans << endl;
    return 0;
}