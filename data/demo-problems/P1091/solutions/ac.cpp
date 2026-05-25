#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> t(n);
    for (int i = 0; i < n; ++i) {
        cin >> t[i];
    }

    // dp1[i]: 以i结尾的最长上升子序列长度
    vector<int> dp1(n, 1);
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < i; ++j) {
            if (t[j] < t[i]) {
                dp1[i] = max(dp1[i], dp1[j] + 1);
            }
        }
    }

    // dp2[i]: 以i开头的最长下降子序列长度
    vector<int> dp2(n, 1);
    for (int i = n - 1; i >= 0; --i) {
        for (int j = n - 1; j > i; --j) {
            if (t[j] < t[i]) {
                dp2[i] = max(dp2[i], dp2[j] + 1);
            }
        }
    }

    int max_len = 0;
    for (int i = 0; i < n; ++i) {
        max_len = max(max_len, dp1[i] + dp2[i] - 1);
    }

    cout << n - max_len << endl;
    return 0;
}