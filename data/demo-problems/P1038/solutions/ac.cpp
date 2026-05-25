#include <iostream>
#include <vector>
#include <queue>
#include <algorithm>
using namespace std;

struct Neuron {
    int c, u;
    bool is_input;
};

int main() {
    int n, p;
    cin >> n >> p;
    vector<Neuron> neurons(n + 1);
    vector<vector<pair<int, long long>>> adj(n + 1); // 邻接表，存储 (目标, 权重)
    vector<int> in_degree(n + 1, 0);
    vector<int> out_degree(n + 1, 0);
    
    for (int i = 1; i <= n; ++i) {
        cin >> neurons[i].c >> neurons[i].u;
        neurons[i].is_input = (neurons[i].c != 0);
        if (!neurons[i].is_input) {
            neurons[i].c = -neurons[i].u; // 非输入层初始状态设为 -u
        }
    }
    
    for (int k = 0; k < p; ++k) {
        int i, j;
        long long w;
        cin >> i >> j >> w;
        adj[i].push_back({j, w});
        in_degree[j]++;
        out_degree[i]++;
    }
    
    queue<int> q;
    for (int i = 1; i <= n; ++i) {
        if (in_degree[i] == 0) {
            q.push(i);
        }
    }
    
    vector<int> topo;
    while (!q.empty()) {
        int u = q.front();
        q.pop();
        topo.push_back(u);
        for (auto &edge : adj[u]) {
            int v = edge.first;
            if (--in_degree[v] == 0) {
                q.push(v);
            }
        }
    }
    
    // 按拓扑序更新状态
    for (int u : topo) {
        if (neurons[u].c > 0) { // 只有兴奋状态才传递信号
            for (auto &edge : adj[u]) {
                int v = edge.first;
                long long w = edge.second;
                neurons[v].c += w * neurons[u].c;
            }
        }
    }
    
    vector<pair<int, int>> output;
    for (int i = 1; i <= n; ++i) {
        if (out_degree[i] == 0 && neurons[i].c > 0) {
            output.push_back({i, neurons[i].c});
        }
    }
    
    if (output.empty()) {
        cout << "NULL" << endl;
    } else {
        sort(output.begin(), output.end());
        for (auto &p : output) {
            cout << p.first << " " << p.second << endl;
        }
    }
    
    return 0;
}