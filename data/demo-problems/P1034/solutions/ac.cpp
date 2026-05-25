#include <bits/stdc++.h>
using namespace std;

const int INF = 1e9;
int n, k;
int x[55], y[55];
int dp[55][5]; // dp[i][j] 表示前i个点分成j个矩形的最小面积和

// 计算覆盖点[l, r]的矩形面积
int area(int l, int r) {
    if (l > r) return 0;
    int minx = INF, maxx = -INF, miny = INF, maxy = -INF;
    for (int i = l; i <= r; i++) {
        minx = min(minx, x[i]);
        maxx = max(maxx, x[i]);
        miny = min(miny, y[i]);
        maxy = max(maxy, y[i]);
    }
    return (maxx - minx) * (maxy - miny);
}

int main() {
    cin >> n >> k;
    for (int i = 1; i <= n; i++) {
        cin >> x[i] >> y[i];
    }
    
    // 按x坐标排序，x相同按y排序
    for (int i = 1; i <= n; i++) {
        for (int j = i + 1; j <= n; j++) {
            if (x[i] > x[j] || (x[i] == x[j] && y[i] > y[j])) {
                swap(x[i], x[j]);
                swap(y[i], y[j]);
            }
        }
    }
    
    // 初始化dp
    for (int i = 0; i <= n; i++) {
        for (int j = 0; j <= k; j++) {
            dp[i][j] = INF;
        }
    }
    dp[0][0] = 0;
    
    // DP
    for (int i = 1; i <= n; i++) {
        for (int j = 1; j <= k; j++) {
            for (int p = 0; p < i; p++) {
                dp[i][j] = min(dp[i][j], dp[p][j-1] + area(p+1, i));
            }
        }
    }
    
    cout << dp[n][k] << endl;
    return 0;
}