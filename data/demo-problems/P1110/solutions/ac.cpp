#include <bits/stdc++.h>
using namespace std;

const int INF = 2e9 + 5;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, m;
    cin >> n >> m;

    vector<int> a(n + 1);
    for (int i = 1; i <= n; ++i) {
        cin >> a[i];
    }

    // 维护每个位置插入的元素列表
    vector<vector<int>> ins(n + 1);

    // 相邻差值的最小值（MIN_GAP）
    multiset<int> gaps;
    for (int i = 1; i < n; ++i) {
        gaps.insert(abs(a[i] - a[i + 1]));
    }

    // 所有元素的有序集合，用于 MIN_SORT_GAP
    multiset<int> all_vals;
    for (int i = 1; i <= n; ++i) {
        all_vals.insert(a[i]);
    }

    // 排序后相邻差值的最小值
    multiset<int> sort_gaps;
    {
        auto it = all_vals.begin();
        int prev = *it;
        ++it;
        for (; it != all_vals.end(); ++it) {
            sort_gaps.insert(*it - prev);
            prev = *it;
        }
    }

    // 处理操作
    while (m--) {
        string op;
        cin >> op;
        if (op == "INSERT") {
            int i, k;
            cin >> i >> k;

            // 确定插入位置的前一个元素
            int prev_val;
            if (ins[i].empty()) {
                prev_val = a[i];
            } else {
                prev_val = ins[i].back();
            }

            // 确定插入位置的后一个元素
            int next_val;
            if (i == n) {
                // 最后一个位置后面没有元素
                next_val = -1; // 标记不存在
            } else {
                if (ins[i + 1].empty()) {
                    next_val = a[i + 1];
                } else {
                    next_val = ins[i + 1].front();
                }
            }

            // 更新 gaps
            if (i < n) {
                // 删除原来的 gap (prev_val, next_val)
                int old_gap = abs(prev_val - next_val);
                auto it_gap = gaps.find(old_gap);
                if (it_gap != gaps.end()) {
                    gaps.erase(it_gap);
                }
                // 插入新的两个 gap
                gaps.insert(abs(prev_val - k));
                gaps.insert(abs(k - next_val));
            } else {
                // i == n，只有前面的 gap
                gaps.insert(abs(prev_val - k));
            }

            // 更新 all_vals 和 sort_gaps
            // 插入 k 到 all_vals
            auto it = all_vals.insert(k);
            // 获取前驱和后继
            int pred = -1, succ = -1;
            if (it != all_vals.begin()) {
                auto it_pred = prev(it);
                pred = *it_pred;
            }
            auto it_succ = next(it);
            if (it_succ != all_vals.end()) {
                succ = *it_succ;
            }

            // 删除旧的相邻差值
            if (pred != -1 && succ != -1) {
                int old_diff = succ - pred;
                auto it_diff = sort_gaps.find(old_diff);
                if (it_diff != sort_gaps.end()) {
                    sort_gaps.erase(it_diff);
                }
            }
            // 插入新的相邻差值
            if (pred != -1) {
                sort_gaps.insert(k - pred);
            }
            if (succ != -1) {
                sort_gaps.insert(succ - k);
            }

            // 记录插入
            ins[i].push_back(k);

        } else if (op == "MIN_GAP") {
            cout << *gaps.begin() << '\n';
        } else if (op == "MIN_SORT_GAP") {
            cout << *sort_gaps.begin() << '\n';
        }
    }

    return 0;
}