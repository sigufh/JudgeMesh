#include <bits/stdc++.h>
using namespace std;

const int MAXN = 100005;
const int MAXM = 500005;

int n, m;
int price[MAXN];
vector<int> graph[MAXN], rev_graph[MAXN];

int min_price[MAXN], max_price[MAXN];
bool vis1[MAXN], vis2[MAXN];

void bfs1() {
    queue<int> q;
    q.push(1);
    vis1[1] = true;
    min_price[1] = price[1];
    while (!q.empty()) {
        int u = q.front(); q.pop();
        for (int v : graph[u]) {
            if (!vis1[v]) {
                vis1[v] = true;
                min_price[v] = min(min_price[u], price[v]);
                q.push(v);
            } else {
                min_price[v] = min(min_price[v], min_price[u]);
            }
        }
    }
}

void bfs2() {
    queue<int> q;
    q.push(n);
    vis2[n] = true;
    max_price[n] = price[n];
    while (!q.empty()) {
        int u = q.front(); q.pop();
        for (int v : rev_graph[u]) {
            if (!vis2[v]) {
                vis2[v] = true;
                max_price[v] = max(max_price[u], price[v]);
                q.push(v);
            } else {
                max_price[v] = max(max_price[v], max_price[u]);
            }
        }
    }
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(0);
    
    cin >> n >> m;
    for (int i = 1; i <= n; ++i) {
        cin >> price[i];
    }
    
    for (int i = 0; i < m; ++i) {
        int x, y, z;
        cin >> x >> y >> z;
        graph[x].push_back(y);
        rev_graph[y].push_back(x);
        if (z == 2) {
            graph[y].push_back(x);
            rev_graph[x].push_back(y);
        }
    }
    
    memset(vis1, 0, sizeof(vis1));
    memset(vis2, 0, sizeof(vis2));
    memset(min_price, 0x3f, sizeof(min_price));
    memset(max_price, 0, sizeof(max_price));
    
    bfs1();
    bfs2();
    
    int ans = 0;
    for (int i = 1; i <= n; ++i) {
        if (vis1[i] && vis2[i]) {
            ans = max(ans, max_price[i] - min_price[i]);
        }
    }
    
    cout << ans << endl;
    return 0;
}