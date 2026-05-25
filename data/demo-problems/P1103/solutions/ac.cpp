#include <bits/stdc++.h>
using namespace std;

int main() {
    int n, k;
    cin >> n >> k;
    vector<pair<int, int>> books(n);
    for (int i = 0; i < n; ++i) {
        cin >> books[i].first >> books[i].second;
    }
    sort(books.begin(), books.end());
    vector<int> w(n);
    for (int i = 0; i < n; ++i) {
        w[i] = books[i].second;
    }
    int m = n - k;
    const int INF = 1e9;
    vector<vector<int>> dp(m + 1, vector<int>(n, INF));
    for (int i = 0; i < n; ++i) {
        dp[1][i] = 0;
    }
    for (int len = 2; len <= m; ++len) {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                if (dp[len - 1][j] != INF) {
                    dp[len][i] = min(dp[len][i], dp[len - 1][j] + abs(w[i] - w[j]));
                }
            }
        }
    }
    int ans = INF;
    for (int i = 0; i < n; ++i) {
        ans = min(ans, dp[m][i]);
    }
    cout << ans << endl;
    return 0;
}