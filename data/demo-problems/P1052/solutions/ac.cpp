#include <iostream>
#include <vector>
#include <algorithm>
#include <cstring>
using namespace std;

const int MAXM = 105;
const int MAXL = 10005; // 压缩后的最大长度
const int INF = 0x3f3f3f3f;

int L, S, T, M;
int stones[MAXM];
int dp[MAXL];
int stone_flag[MAXL];

int main() {
    cin >> L >> S >> T >> M;
    for (int i = 0; i < M; ++i) {
        cin >> stones[i];
    }
    if (S == T) {
        int ans = 0;
        for (int i = 0; i < M; ++i) {
            if (stones[i] % S == 0) ans++;
        }
        cout << ans << endl;
        return 0;
    }
    sort(stones, stones + M);
    // 路径压缩
    int newL = 0;
    int last = 0;
    memset(stone_flag, 0, sizeof(stone_flag));
    for (int i = 0; i < M; ++i) {
        int dist = stones[i] - last;
        if (dist > 100) dist = 100; // 压缩距离
        newL += dist;
        stone_flag[newL] = 1;
        last = stones[i];
    }
    // 终点处理
    int dist = L - last;
    if (dist > 100) dist = 100;
    newL += dist;
    // DP
    memset(dp, 0x3f, sizeof(dp));
    dp[0] = 0;
    for (int i = 1; i <= newL + T; ++i) {
        for (int j = S; j <= T; ++j) {
            if (i - j >= 0) {
                dp[i] = min(dp[i], dp[i - j] + stone_flag[i]);
            }
        }
    }
    int ans = INF;
    for (int i = newL; i <= newL + T; ++i) {
        ans = min(ans, dp[i]);
    }
    cout << ans << endl;
    return 0;
}