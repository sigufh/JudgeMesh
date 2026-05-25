#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

struct Item {
    int v, p, q;
};

int main() {
    int n, m;
    cin >> n >> m;
    // 价格都是10的倍数，除以10减少状态数
    n /= 10;
    vector<Item> items(m + 1);
    for (int i = 1; i <= m; ++i) {
        cin >> items[i].v >> items[i].p >> items[i].q;
        items[i].v /= 10;
    }

    // 分组：每个主件及其附件
    vector<vector<int>> groups(m + 1);
    for (int i = 1; i <= m; ++i) {
        if (items[i].q == 0) {
            groups[i].push_back(i); // 主件自己
        } else {
            groups[items[i].q].push_back(i); // 附件加入对应主件组
        }
    }

    vector<int> dp(n + 1, 0);
    for (int i = 1; i <= m; ++i) {
        if (groups[i].empty()) continue;
        // 主件必须选，先处理主件
        int master = groups[i][0];
        int master_v = items[master].v;
        int master_w = items[master].v * items[master].p;
        // 附件最多两个
        int attach1 = -1, attach2 = -1;
        if (groups[i].size() > 1) attach1 = groups[i][1];
        if (groups[i].size() > 2) attach2 = groups[i][2];

        // 生成该组所有可能的组合（主件必选）
        vector<pair<int, int>> options;
        // 只选主件
        options.push_back({master_v, master_w});
        // 主件+附件1
        if (attach1 != -1) {
            int v1 = items[attach1].v;
            int w1 = items[attach1].v * items[attach1].p;
            options.push_back({master_v + v1, master_w + w1});
        }
        // 主件+附件2
        if (attach2 != -1) {
            int v2 = items[attach2].v;
            int w2 = items[attach2].v * items[attach2].p;
            options.push_back({master_v + v2, master_w + w2});
        }
        // 主件+附件1+附件2
        if (attach1 != -1 && attach2 != -1) {
            int v1 = items[attach1].v;
            int w1 = items[attach1].v * items[attach1].p;
            int v2 = items[attach2].v;
            int w2 = items[attach2].v * items[attach2].p;
            options.push_back({master_v + v1 + v2, master_w + w1 + w2});
        }

        // 分组背包：每组只能选一种组合
        for (int j = n; j >= 0; --j) {
            for (auto &opt : options) {
                int v = opt.first;
                int w = opt.second;
                if (j >= v) {
                    dp[j] = max(dp[j], dp[j - v] + w);
                }
            }
        }
    }

    cout << dp[n] * 10 << endl;
    return 0;
}