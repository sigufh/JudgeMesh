#include <iostream>
#include <vector>
#include <climits>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> score(n + 1);
    for (int i = 1; i <= n; ++i) {
        cin >> score[i];
    }

    // dp[i][j] 表示中序遍历区间 [i, j] 能得到的最大加分
    vector<vector<long long>> dp(n + 2, vector<long long>(n + 2, 0));
    // root[i][j] 记录区间 [i, j] 取最大加分时选择的根节点
    vector<vector<int>> root(n + 2, vector<int>(n + 2, 0));

    // 初始化：空子树加分为 1，但 dp 中我们直接处理
    for (int i = 1; i <= n; ++i) {
        dp[i][i] = score[i];
        root[i][i] = i;
    }

    // 区间 DP，枚举长度
    for (int len = 2; len <= n; ++len) {
        for (int i = 1; i + len - 1 <= n; ++i) {
            int j = i + len - 1;
            dp[i][j] = 0;
            for (int k = i; k <= j; ++k) {
                long long left = (k == i) ? 1 : dp[i][k - 1];
                long long right = (k == j) ? 1 : dp[k + 1][j];
                long long val = left * right + score[k];
                if (val > dp[i][j]) {
                    dp[i][j] = val;
                    root[i][j] = k;
                }
            }
        }
    }

    cout << dp[1][n] << endl;

    // 递归输出前序遍历
    vector<int> preorder;
    function<void(int, int)> dfs = [&](int l, int r) {
        if (l > r) return;
        int rt = root[l][r];
        preorder.push_back(rt);
        dfs(l, rt - 1);
        dfs(rt + 1, r);
    };
    dfs(1, n);

    for (int i = 0; i < n; ++i) {
        if (i > 0) cout << " ";
        cout << preorder[i];
    }
    cout << endl;

    return 0;
}