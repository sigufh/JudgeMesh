#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    // dp[i][j] 表示把整数 i 分成 j 份的方案数
    vector<vector<int>> dp(n + 1, vector<int>(k + 1, 0));
    dp[0][0] = 1; // 0 分成 0 份有一种方案
    for (int i = 1; i <= n; ++i) {
        for (int j = 1; j <= k; ++j) {
            if (i >= j) {
                // 两种情况：至少有一份是 1，或者所有份都大于 1
                dp[i][j] = dp[i - 1][j - 1] + dp[i - j][j];
            }
        }
    }
    cout << dp[n][k] << endl;
    return 0;
}