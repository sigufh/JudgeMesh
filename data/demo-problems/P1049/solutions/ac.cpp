#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    int V, n;
    cin >> V >> n;
    vector<int> w(n);
    for (int i = 0; i < n; ++i) {
        cin >> w[i];
    }
    vector<bool> dp(V + 1, false);
    dp[0] = true;
    for (int i = 0; i < n; ++i) {
        for (int j = V; j >= w[i]; --j) {
            if (dp[j - w[i]]) {
                dp[j] = true;
            }
        }
    }
    int ans = V;
    for (int j = V; j >= 0; --j) {
        if (dp[j]) {
            ans = V - j;
            break;
        }
    }
    cout << ans << endl;
    return 0;
}