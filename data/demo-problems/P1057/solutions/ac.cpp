#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    // dp[i][j] 表示传了 i 次球后，球在 j 号同学手里的方法数
    vector<vector<int>> dp(m + 1, vector<int>(n, 0));
    dp[0][0] = 1; // 初始球在 0 号（小蛮）手里
    for (int i = 1; i <= m; ++i) {
        for (int j = 0; j < n; ++j) {
            // 从左边传来
            int left = (j - 1 + n) % n;
            // 从右边传来
            int right = (j + 1) % n;
            dp[i][j] = dp[i-1][left] + dp[i-1][right];
        }
    }
    cout << dp[m][0] << endl;
    return 0;
}