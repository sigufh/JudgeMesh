#include <bits/stdc++.h>
using namespace std;

const int MAXN = 10;
int cnt[MAXN];          // cnt[i] = 数字i能变成的不同数字个数（包括自身）
bool vis[MAXN];         // DFS标记
vector<int> g[MAXN];    // 变换图

void dfs(int u, int start) {
    vis[u] = true;
    cnt[start]++;
    for (int v : g[u]) {
        if (!vis[v]) {
            dfs(v, start);
        }
    }
}

// 高精度乘法：大整数乘以小整数
string multiply(const string &s, int x) {
    string res;
    int carry = 0;
    for (int i = s.size() - 1; i >= 0; --i) {
        int tmp = (s[i] - '0') * x + carry;
        res.push_back(tmp % 10 + '0');
        carry = tmp / 10;
    }
    while (carry) {
        res.push_back(carry % 10 + '0');
        carry /= 10;
    }
    reverse(res.begin(), res.end());
    return res;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    string n;
    int k;
    cin >> n >> k;

    for (int i = 0; i < k; ++i) {
        int x, y;
        cin >> x >> y;
        g[x].push_back(y);
    }

    // 计算每个数字能变成的不同数字个数
    for (int i = 0; i < 10; ++i) {
        memset(vis, 0, sizeof(vis));
        dfs(i, i);
    }

    // 高精度计算结果
    string ans = "1";
    for (char ch : n) {
        int d = ch - '0';
        if (cnt[d] > 0) {
            ans = multiply(ans, cnt[d]);
        }
    }

    cout << ans << "\n";
    return 0;
}