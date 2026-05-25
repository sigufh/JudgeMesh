#include <bits/stdc++.h>
using namespace std;

typedef long long ll;
const int MAXN = 100005;
const int LOG = 17;
const ll INF = 1e18;

int n, m;
ll h[MAXN];
int x0;
int s[MAXN];
ll x[MAXN];

// 前驱后继相关
int pos[MAXN];
int sorted_idx[MAXN];
int pre[MAXN], nxt[MAXN];

// 每个城市小A和小B的下一个目的地
int nextA[MAXN], nextB[MAXN];
ll distA[MAXN], distB[MAXN];

// 倍增表
int f[LOG][MAXN];
ll da[LOG][MAXN];
ll db[LOG][MAXN];

// 计算距离
ll get_dist(int i, int j) {
    return abs(h[i] - h[j]);
}

// 预处理每个城市的下一个目的地
void preprocess_next() {
    // 按海拔排序
    vector<pair<ll, int>> cities;
    for (int i = 1; i <= n; i++) {
        cities.push_back({h[i], i});
    }
    sort(cities.begin(), cities.end());
    for (int i = 0; i < n; i++) {
        sorted_idx[i+1] = cities[i].second;
        pos[cities[i].second] = i+1;
    }
    
    // 初始化链表
    for (int i = 1; i <= n; i++) {
        pre[i] = i-1;
        nxt[i] = i+1;
    }
    nxt[n] = 0;
    pre[1] = 0;
    
    // 从西到东处理每个城市
    for (int i = 1; i <= n; i++) {
        int p = pos[i];
        // 候选：前驱的前驱，前驱，后继，后继的后继
        vector<int> candidates;
        if (pre[p] != 0) {
            candidates.push_back(sorted_idx[pre[p]]);
            if (pre[pre[p]] != 0) {
                candidates.push_back(sorted_idx[pre[pre[p]]]);
            }
        }
        if (nxt[p] != 0) {
            candidates.push_back(sorted_idx[nxt[p]]);
            if (nxt[nxt[p]] != 0) {
                candidates.push_back(sorted_idx[nxt[nxt[p]]]);
            }
        }
        // 按距离排序，距离相同按海拔低优先
        sort(candidates.begin(), candidates.end(), [&](int a, int b) {
            ll da = get_dist(i, a);
            ll db = get_dist(i, b);
            if (da != db) return da < db;
            return h[a] < h[b];
        });
        // 设置nextA和nextB
        if (candidates.size() >= 1) {
            nextB[i] = candidates[0];
            distB[i] = get_dist(i, candidates[0]);
        } else {
            nextB[i] = 0;
            distB[i] = 0;
        }
        if (candidates.size() >= 2) {
            nextA[i] = candidates[1];
            distA[i] = get_dist(i, candidates[1]);
        } else {
            nextA[i] = 0;
            distA[i] = 0;
        }
        // 从链表中删除当前城市
        if (pre[p] != 0) nxt[pre[p]] = nxt[p];
        if (nxt[p] != 0) pre[nxt[p]] = pre[p];
    }
}

// 预处理倍增表
void preprocess_binary_lifting() {
    for (int i = 1; i <= n; i++) {
        // 第一天小A开车，所以从i出发，经过1天到达nextA[i]，小A行驶distA[i]
        // 但倍增表f[0][i]表示从i出发，经过2^0=1轮（小A一次，小B一次）后的位置
        // 实际上，我们定义f[k][i]为从i出发，经过2^k轮后的位置，一轮包含小A一次和小B一次
        // 但第一天是小A开车，所以我们需要特殊处理第一步
        // 这里采用常见写法：f[0][i] = nextB[nextA[i]]，如果nextA[i]存在且nextB[nextA[i]]存在
        if (nextA[i] != 0 && nextB[nextA[i]] != 0) {
            f[0][i] = nextB[nextA[i]];
            da[0][i] = distA[i];
            db[0][i] = distB[nextA[i]];
        } else {
            f[0][i] = 0;
            da[0][i] = 0;
            db[0][i] = 0;
        }
    }
    for (int k = 1; k < LOG; k++) {
        for (int i = 1; i <= n; i++) {
            if (f[k-1][i] != 0) {
                f[k][i] = f[k-1][f[k-1][i]];
                da[k][i] = da[k-1][i] + da[k-1][f[k-1][i]];
                db[k][i] = db[k-1][i] + db[k-1][f[k-1][i]];
            } else {
                f[k][i] = 0;
                da[k][i] = 0;
                db[k][i] = 0;
            }
        }
    }
}

// 查询从s出发，总距离限制为X，返回小A和小B的行驶距离
pair<ll, ll> query(int s, ll X) {
    ll totA = 0, totB = 0;
    int cur = s;
    // 先处理第一步：小A开车
    if (nextA[cur] != 0 && distA[cur] <= X) {
        totA += distA[cur];
        X -= distA[cur];
        cur = nextA[cur];
    } else {
        return {totA, totB};
    }
    // 然后倍增处理后续的轮次（小B+小A）
    for (int k = LOG-1; k >= 0; k--) {
        if (f[k][cur] != 0 && da[k][cur] + db[k][cur] <= X) {
            totA += da[k][cur];
            totB += db[k][cur];
            X -= da[k][cur] + db[k][cur];
            cur = f[k][cur];
        }
    }
    // 可能还有剩余的一步小B开车
    if (nextB[cur] != 0 && distB[cur] <= X) {
        totB += distB[cur];
        X -= distB[cur];
        cur = nextB[cur];
    }
    return {totA, totB};
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(0);
    
    cin >> n;
    for (int i = 1; i <= n; i++) {
        cin >> h[i];
    }
    cin >> x0;
    cin >> m;
    for (int i = 1; i <= m; i++) {
        cin >> s[i] >> x[i];
    }
    
    preprocess_next();
    preprocess_binary_lifting();
    
    // 解决问题1
    int best_s = 0;
    double best_ratio = INF;
    for (int i = 1; i <= n; i++) {
        auto res = query(i, x0);
        ll a = res.first, b = res.second;
        double ratio;
        if (b == 0) {
            ratio = INF;
        } else {
            ratio = (double)a / b;
        }
        if (ratio < best_ratio - 1e-9) {
            best_ratio = ratio;
            best_s = i;
        } else if (fabs(ratio - best_ratio) < 1e-9) {
            if (h[i] > h[best_s]) {
                best_s = i;
            }
        }
    }
    cout << best_s << "\n";
    
    // 解决问题2
    for (int i = 1; i <= m; i++) {
        auto res = query(s[i], x[i]);
        cout << res.first << " " << res.second << "\n";
    }
    
    return 0;
}