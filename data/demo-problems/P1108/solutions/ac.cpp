#include <iostream>
#include <vector>
#include <algorithm>
#include <cstring>
using namespace std;

const int MAXN = 5005;
int n;
int a[MAXN];
int dp[MAXN];      // dp[i] = length of longest decreasing subsequence ending at i
int cnt[MAXN];     // cnt[i] = number of LDS of length dp[i] ending at i

int main() {
    cin >> n;
    for (int i = 0; i < n; ++i) {
        cin >> a[i];
    }

    int max_len = 0;
    for (int i = 0; i < n; ++i) {
        dp[i] = 1;
        cnt[i] = 1;
        for (int j = 0; j < i; ++j) {
            if (a[j] > a[i]) {
                if (dp[j] + 1 > dp[i]) {
                    dp[i] = dp[j] + 1;
                    cnt[i] = cnt[j];
                } else if (dp[j] + 1 == dp[i]) {
                    cnt[i] += cnt[j];
                }
            }
        }
        // Remove duplicates: if there is a[j] == a[i] and dp[j] == dp[i] for j < i,
        // then sequences ending at j and i would produce the same price string,
        // so we set cnt[j] = 0 to avoid double counting.
        for (int j = 0; j < i; ++j) {
            if (a[j] == a[i] && dp[j] == dp[i]) {
                cnt[j] = 0;
            }
        }
        max_len = max(max_len, dp[i]);
    }

    int total_cnt = 0;
    for (int i = 0; i < n; ++i) {
        if (dp[i] == max_len) {
            total_cnt += cnt[i];
        }
    }

    cout << max_len << " " << total_cnt << endl;
    return 0;
}