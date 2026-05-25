#include <bits/stdc++.h>
using namespace std;

const int INF = 0x3f3f3f3f;
int n, m;
int a[105], sum[105];
int dp_min[105][105][15], dp_max[105][105][15];

int mod10(int x) {
    return ((x % 10) + 10) % 10;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    
    cin >> n >> m;
    for (int i = 1; i <= n; i++) {
        cin >> a[i];
        a[i + n] = a[i];
    }
    
    int N = 2 * n;
    sum[0] = 0;
    for (int i = 1; i <= N; i++) {
        sum[i] = sum[i - 1] + a[i];
    }
    
    int ans_min = INF, ans_max = -INF;
    
    for (int start = 1; start <= n; start++) {
        memset(dp_min, 0x3f, sizeof(dp_min));
        memset(dp_max, -0x3f, sizeof(dp_max));
        
        for (int i = start; i < start + n; i++) {
            dp_min[i][i][1] = dp_max[i][i][1] = mod10(a[i]);
        }
        
        for (int len = 2; len <= n; len++) {
            for (int i = start; i + len - 1 < start + n; i++) {
                int j = i + len - 1;
                for (int k = 2; k <= m && k <= len; k++) {
                    for (int p = i + k - 2; p < j; p++) {
                        if (dp_min[i][p][k - 1] != INF) {
                            int val = mod10(sum[j] - sum[p]);
                            dp_min[i][j][k] = min(dp_min[i][j][k], dp_min[i][p][k - 1] * val);
                            dp_max[i][j][k] = max(dp_max[i][j][k], dp_max[i][p][k - 1] * val);
                        }
                    }
                }
            }
        }
        
        ans_min = min(ans_min, dp_min[start][start + n - 1][m]);
        ans_max = max(ans_max, dp_max[start][start + n - 1][m]);
    }
    
    cout << ans_min << "\n" << ans_max << "\n";
    return 0;
}