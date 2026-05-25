#include <bits/stdc++.h>
using namespace std;
typedef long long ll;

const int MAXN = 50005;
const ll INF = 1e15;

struct Edge {
    int to, w;
};

int n, m;
vector<Edge> adj[MAXN];
int army[MAXN];
int parent[MAXN][20];
ll dist[MAXN];
int depth[MAXN];

void dfs(int u, int p, ll d) {
    parent[u][0] = p;
    dist[u] = d;
    depth[u] = depth[p] + 1;
    for (int i = 1; i < 20; i++) {
        if (parent[u][i-1] != 0)
            parent[u][i] = parent[parent[u][i-1]][i-1];
        else
            parent[u][i] = 0;
    }
    for (auto &e : adj[u]) {
        if (e.to == p) continue;
        dfs(e.to, u, d + e.w);
    }
}

int lca(int u, int v) {
    if (depth[u] < depth[v]) swap(u, v);
    int diff = depth[u] - depth[v];
    for (int i = 0; i < 20; i++) {
        if (diff & (1 << i)) u = parent[u][i];
    }
    if (u == v) return u;
    for (int i = 19; i >= 0; i--) {
        if (parent[u][i] != parent[v][i]) {
            u = parent[u][i];
            v = parent[v][i];
        }
    }
    return parent[u][0];
}

int jump(int u, int steps) {
    for (int i = 0; i < 20; i++) {
        if (steps & (1 << i)) u = parent[u][i];
    }
    return u;
}

bool covered[MAXN];
bool has_army[MAXN];
vector<pair<ll, int>> free_army;
vector<pair<ll, int>> need_cover;

bool dfs_check(int u, int p) {
    if (has_army[u]) return true;
    bool is_leaf = true;
    bool all_covered = true;
    for (auto &e : adj[u]) {
        if (e.to == p) continue;
        is_leaf = false;
        if (!dfs_check(e.to, u)) {
            all_covered = false;
        }
    }
    if (is_leaf) return false;
    return all_covered;
}

bool check(ll limit) {
    memset(has_army, 0, sizeof(has_army));
    free_army.clear();
    need_cover.clear();
    
    for (int i = 1; i <= m; i++) {
        int u = army[i];
        if (dist[u] <= limit) {
            free_army.push_back({limit - dist[u], u});
        } else {
            int steps = 0;
            ll cur_dist = dist[u];
            int cur = u;
            for (int j = 19; j >= 0; j--) {
                if (parent[cur][j] != 0 && cur_dist - dist[parent[cur][j]] <= limit) {
                    cur_dist -= dist[cur] - dist[parent[cur][j]];
                    cur = parent[cur][j];
                }
            }
            has_army[cur] = true;
        }
    }
    
    for (int i = 1; i <= n; i++) covered[i] = false;
    
    function<void(int,int)> mark = [&](int u, int p) {
        if (has_army[u]) {
            covered[u] = true;
            return;
        }
        bool all_child_covered = true;
        bool is_leaf = true;
        for (auto &e : adj[u]) {
            if (e.to == p) continue;
            is_leaf = false;
            mark(e.to, u);
            if (!covered[e.to]) all_child_covered = false;
        }
        if (!is_leaf && all_child_covered) covered[u] = true;
    };
    
    mark(1, 0);
    
    for (auto &e : adj[1]) {
        if (!covered[e.to]) {
            need_cover.push_back({dist[e.to], e.to});
        }
    }
    
    sort(free_army.begin(), free_army.end());
    sort(need_cover.begin(), need_cover.end());
    
    vector<bool> used(free_army.size(), false);
    int j = 0;
    for (auto &nc : need_cover) {
        bool ok = false;
        for (int i = 0; i < free_army.size(); i++) {
            if (used[i]) continue;
            ll rem = free_army[i].first;
            int u = free_army[i].second;
            if (dist[u] <= nc.first) {
                used[i] = true;
                ok = true;
                break;
            }
        }
        if (!ok) {
            for (int i = 0; i < free_army.size(); i++) {
                if (used[i]) continue;
                ll rem = free_army[i].first;
                int u = free_army[i].second;
                if (rem >= nc.first) {
                    used[i] = true;
                    ok = true;
                    break;
                }
            }
        }
        if (!ok) return false;
    }
    return true;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(0);
    
    cin >> n;
    for (int i = 0; i < n-1; i++) {
        int u, v, w;
        cin >> u >> v >> w;
        adj[u].push_back({v, w});
        adj[v].push_back({u, w});
    }
    
    dfs(1, 0, 0);
    
    cin >> m;
    for (int i = 1; i <= m; i++) {
        cin >> army[i];
    }
    
    ll left = 0, right = INF, ans = -1;
    while (left <= right) {
        ll mid = (left + right) / 2;
        if (check(mid)) {
            ans = mid;
            right = mid - 1;
        } else {
            left = mid + 1;
        }
    }
    
    cout << ans << "\n";
    return 0;
}