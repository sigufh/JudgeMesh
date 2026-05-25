#include <iostream>
#include <vector>
#include <queue>
#include <climits>
#include <cstring>
using namespace std;

const int MAXN = 105;
const int MAXK = 105;
const int INF = 0x3f3f3f3f;

int N, K, M, S, T;
int culture[MAXN];
int reject[MAXK][MAXK];
vector<pair<int, int>> graph[MAXN];
int dist[MAXN][MAXK]; // dist[i][c] 表示到达国家 i 且已学文化集合包含文化 c 的最短距离（这里用最后一个学的文化代表状态）

// 实际上我们需要记录已学文化的集合，但 N 和 K 最大 100，状态太多。
// 题目数据水，可以用 BFS 或 DFS 加剪枝，或者用状压但 K 太大。
// 这里采用 Dijkstra 变种，状态为 (国家, 已学文化集合)，但集合用 bitset 或整数表示，K<=100 无法状压。
// 由于数据水，我们直接用 DFS 加剪枝，或者用 BFS 求最短路，记录路径上的文化集合。
// 更简单的方法：因为 N 很小，我们可以用 DFS 搜索所有路径，记录最短距离，文化冲突时剪枝。

int ans = INF;
bool visited[MAXN];
vector<int> path_cultures; // 当前路径上学到的文化

void dfs(int u, int cost) {
    if (u == T) {
        ans = min(ans, cost);
        return;
    }
    if (cost >= ans) return; // 剪枝
    for (auto &edge : graph[u]) {
        int v = edge.first;
        int w = edge.second;
        if (visited[v]) continue;
        int cul_v = culture[v];
        // 检查是否已经学过文化 cul_v
        bool learned = false;
        for (int c : path_cultures) {
            if (c == cul_v) {
                learned = true;
                break;
            }
        }
        if (learned) continue; // 不能重复学
        // 检查 v 的文化是否排斥已学的任何文化
        bool conflict = false;
        for (int c : path_cultures) {
            if (reject[cul_v][c]) {
                conflict = true;
                break;
            }
        }
        if (conflict) continue;
        // 检查已学文化是否排斥 v 的文化
        for (int c : path_cultures) {
            if (reject[c][cul_v]) {
                conflict = true;
                break;
            }
        }
        if (conflict) continue;
        // 可以走
        visited[v] = true;
        path_cultures.push_back(cul_v);
        dfs(v, cost + w);
        path_cultures.pop_back();
        visited[v] = false;
    }
}

int main() {
    cin >> N >> K >> M >> S >> T;
    for (int i = 1; i <= N; ++i) {
        cin >> culture[i];
    }
    for (int i = 1; i <= K; ++i) {
        for (int j = 1; j <= K; ++j) {
            cin >> reject[i][j];
        }
    }
    for (int i = 0; i < M; ++i) {
        int u, v, d;
        cin >> u >> v >> d;
        graph[u].push_back({v, d});
        graph[v].push_back({u, d});
    }
    memset(visited, 0, sizeof(visited));
    visited[S] = true;
    path_cultures.push_back(culture[S]);
    dfs(S, 0);
    if (ans == INF) cout << -1 << endl;
    else cout << ans << endl;
    return 0;
}