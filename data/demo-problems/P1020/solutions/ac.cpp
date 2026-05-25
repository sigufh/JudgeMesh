#include <iostream>
#include <vector>
#include <algorithm>
#include <sstream>
#include <string>

using namespace std;

int main() {
    string line;
    getline(cin, line);
    istringstream iss(line);
    vector<int> heights;
    int h;
    while (iss >> h) {
        heights.push_back(h);
    }
    int n = heights.size();
    if (n == 0) {
        cout << 0 << endl << 0 << endl;
        return 0;
    }

    // 第一问：最长不上升子序列长度
    vector<int> dp;
    for (int i = 0; i < n; ++i) {
        // 在dp中找第一个小于heights[i]的位置（严格小于，因为要不上升，即<=）
        // 使用upper_bound，因为dp是递减的，需要自定义比较
        // 实际上，我们要维护一个递减序列，找第一个 < heights[i] 的位置
        auto it = upper_bound(dp.begin(), dp.end(), heights[i], greater<int>());
        if (it == dp.end()) {
            dp.push_back(heights[i]);
        } else {
            *it = heights[i];
        }
    }
    int max_intercept = dp.size();

    // 第二问：最少系统数 = 最长上升子序列长度（Dilworth定理）
    vector<int> dp2;
    for (int i = 0; i < n; ++i) {
        // 找第一个 >= heights[i] 的位置（严格上升，所以找 >= 的位置替换）
        auto it = lower_bound(dp2.begin(), dp2.end(), heights[i]);
        if (it == dp2.end()) {
            dp2.push_back(heights[i]);
        } else {
            *it = heights[i];
        }
    }
    int min_systems = dp2.size();

    cout << max_intercept << endl;
    cout << min_systems << endl;

    return 0;
}