#include <bits/stdc++.h>
using namespace std;

double S, C, L, P0;
int N;
double D[10], P[10];
double ans = 1e18;

void dfs(int cur, double oil, double cost) {
    if (cur == N + 1) {
        double need = (S - D[cur]) / L;
        if (oil >= need) {
            ans = min(ans, cost);
        }
        return;
    }
    double dist = D[cur + 1] - D[cur];
    double need = dist / L;
    if (need > C) return;
    // 在当前站加油
    for (double add = 0; add <= C - oil; add += 0.01) {
        if (oil + add < need) continue;
        double new_oil = oil + add - need;
        double new_cost = cost + add * P[cur];
        dfs(cur + 1, new_oil, new_cost);
    }
}

int main() {
    cin >> S >> C >> L >> P0 >> N;
    D[0] = 0; P[0] = P0;
    for (int i = 1; i <= N; i++) {
        cin >> D[i] >> P[i];
    }
    D[N + 1] = S;
    P[N + 1] = 0;
    // 检查是否可达
    for (int i = 0; i <= N; i++) {
        if (D[i + 1] - D[i] > C * L) {
            cout << "No Solution" << endl;
            return 0;
        }
    }
    dfs(0, 0, 0);
    if (ans == 1e18) {
        cout << "No Solution" << endl;
    } else {
        cout << fixed << setprecision(2) << ans << endl;
    }
    return 0;
}