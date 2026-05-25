#include <iostream>
#include <vector>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> dp(n + 1, 0);
    dp[1] = 1;
    for (int i = 2; i <= n; ++i) {
        int sum = 1; // 数列只有 i 本身
        for (int j = 1; j <= i / 2; ++j) {
            sum += dp[j];
        }
        dp[i] = sum;
    }
    cout << dp[n] << endl;
    return 0;
}