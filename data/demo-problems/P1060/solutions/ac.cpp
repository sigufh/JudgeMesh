#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    vector<int> v(m), p(m);
    for (int i = 0; i < m; ++i) {
        cin >> v[i] >> p[i];
    }
    vector<int> dp(n + 1, 0);
    for (int i = 0; i < m; ++i) {
        int value = v[i] * p[i];
        for (int j = n; j >= v[i]; --j) {
            dp[j] = max(dp[j], dp[j - v[i]] + value);
        }
    }
    cout << dp[n] << endl;
    return 0;
}