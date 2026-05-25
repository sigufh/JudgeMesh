#include <bits/stdc++.h>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int N, H, Delta;
    cin >> N >> H >> Delta;

    // 存储每棵树每个高度的柿子数量
    vector<vector<int>> tree(N + 1, vector<int>(H + 1, 0));
    for (int i = 1; i <= N; ++i) {
        int Ni;
        cin >> Ni;
        for (int j = 0; j < Ni; ++j) {
            int h;
            cin >> h;
            tree[i][h]++;
        }
    }

    // dp[i][h] 表示小猫在树i高度h时能吃到的最多柿子数（包括当前位置）
    vector<vector<int>> dp(N + 1, vector<int>(H + 1, 0));
    // max_dp[h] 表示高度h时所有树中dp的最大值
    vector<int> max_dp(H + 1, 0);

    // 从高到低处理高度
    for (int h = H; h >= 1; --h) {
        for (int i = 1; i <= N; ++i) {
            // 从当前树向下跳
            int val = tree[i][h];
            if (h < H) {
                val += dp[i][h + 1];
            }
            // 从其他树跳过来
            if (h + Delta <= H) {
                val = max(val, tree[i][h] + max_dp[h + Delta]);
            }
            dp[i][h] = val;
            max_dp[h] = max(max_dp[h], dp[i][h]);
        }
    }

    // 小猫可以从阳台跳到任意一棵树的树顶
    int ans = 0;
    for (int i = 1; i <= N; ++i) {
        ans = max(ans, dp[i][H]);
    }
    cout << ans << "\n";

    return 0;
}