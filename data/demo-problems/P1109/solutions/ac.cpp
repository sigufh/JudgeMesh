#include <iostream>
#include <vector>
#include <algorithm>
#include <numeric>

using namespace std;

int main() {
    int n;
    cin >> n;
    vector<int> a(n);
    for (int i = 0; i < n; ++i) {
        cin >> a[i];
    }
    int L, R;
    cin >> L >> R;

    long long sum = accumulate(a.begin(), a.end(), 0LL);
    if (sum < 1LL * L * n || sum > 1LL * R * n) {
        cout << -1 << endl;
        return 0;
    }

    long long need = 0, extra = 0;
    for (int i = 0; i < n; ++i) {
        if (a[i] < L) {
            need += L - a[i];
        } else if (a[i] > R) {
            extra += a[i] - R;
        }
    }

    cout << max(need, extra) << endl;
    return 0;
}