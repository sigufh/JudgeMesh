#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; ++i) {
        cin >> a[i];
    }
    // 破环成链，复制一份
    vector<int> val(2 * n);
    for (int i = 0; i < n; ++i) {
        val[i] = a[i];
        val[i + n] = a[i];
    }
    // dp[i][j] 表示从 i 到 j 合并的最大能量
    vector<vector<int>> dp(2 * n, vector<int>(2 * n, 0));
    // 枚举区间长度
    for (int len = 2; len <= n; ++len) {
        for (int i = 0; i + len - 1 < 2 * n; ++i) {
            int j = i + len - 1;
            for (int k = i; k < j; ++k) {
                dp[i][j] = max(dp[i][j], dp[i][k] + dp[k + 1][j] + val[i] * val[k + 1] * val[j + 1]);
            }
        }
    }
    int ans = 0;
    for (int i = 0; i < n; ++i) {
        ans = max(ans, dp[i][i + n - 1]);
    }
    cout << ans << endl;
    return 0;
}