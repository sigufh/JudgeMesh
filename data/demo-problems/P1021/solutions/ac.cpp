#include <iostream>
#include <vector>
#include <algorithm>
#include <climits>

using namespace std;

int N, K;
int max_continuous = 0;
vector<int> best_stamps;

// 计算当前邮票组合能覆盖的连续最大值
int calc_continuous(const vector<int>& stamps) {
    // dp[i] 表示凑出邮资 i 所需的最少邮票数
    // 最大可能邮资为 stamps.back() * N
    int max_val = stamps.back() * N;
    vector<int> dp(max_val + 1, INT_MAX);
    dp[0] = 0;
    for (int i = 1; i <= max_val; ++i) {
        for (int s : stamps) {
            if (i >= s && dp[i - s] != INT_MAX) {
                dp[i] = min(dp[i], dp[i - s] + 1);
            }
        }
    }
    int cnt = 0;
    for (int i = 1; i <= max_val; ++i) {
        if (dp[i] <= N) cnt++;
        else break;
    }
    return cnt;
}

// 深度优先搜索生成邮票面值组合
void dfs(int idx, int prev, vector<int>& current) {
    if (idx == K) {
        int cont = calc_continuous(current);
        if (cont > max_continuous) {
            max_continuous = cont;
            best_stamps = current;
        }
        return;
    }
    // 下一个面值至少为 prev+1，最大为当前连续值+1
    int max_next = (idx == 0) ? 1 : calc_continuous(current) + 1;
    for (int v = prev + 1; v <= max_next; ++v) {
        current.push_back(v);
        dfs(idx + 1, v, current);
        current.pop_back();
    }
}

int main() {
    cin >> N >> K;
    vector<int> current;
    dfs(0, 0, current);
    for (int i = 0; i < best_stamps.size(); ++i) {
        if (i > 0) cout << " ";
        cout << best_stamps[i];
    }
    cout << endl;
    cout << "MAX=" << max_continuous << endl;
    return 0;
}