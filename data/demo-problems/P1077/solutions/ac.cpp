#include <iostream>
#include <vector>
using namespace std;

const int MOD = 1000007;

int main() {
    int n, m;
    cin >> n >> m;
    vector<int> a(n);
    for (int i = 0; i < n; ++i) {
        cin >> a[i];
    }

    // dp[j] 表示摆 j 盆花的方案数
    vector<int> dp(m + 1, 0);
    dp[0] = 1; // 0 盆花有一种方案（什么都不摆）

    for (int i = 0; i < n; ++i) {
        // 使用滚动数组，从后往前更新
        for (int j = m; j >= 0; --j) {
            // 枚举当前第 i 种花摆多少盆
            for (int k = 1; k <= a[i] && j + k <= m; ++k) {
                dp[j + k] = (dp[j + k] + dp[j]) % MOD;
            }
        }
    }

    cout << dp[m] << endl;
    return 0;
}