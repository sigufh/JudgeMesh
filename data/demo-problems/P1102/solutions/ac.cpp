#include <bits/stdc++.h>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int N, C;
    cin >> N >> C;
    vector<int> a(N);
    for (int i = 0; i < N; ++i) {
        cin >> a[i];
    }

    sort(a.begin(), a.end());

    long long ans = 0;
    for (int i = 0; i < N; ++i) {
        int B = a[i];
        int A = B + C;
        auto lower = lower_bound(a.begin(), a.end(), A);
        auto upper = upper_bound(a.begin(), a.end(), A);
        ans += (upper - lower);
    }

    cout << ans << "\n";
    return 0;
}