#include <bits/stdc++.h>
using namespace std;

const int MAXN = 1000005;
int n, m;
long long r[MAXN], diff[MAXN];
int d[MAXN], s[MAXN], t[MAXN];

bool check(int k) {
    memset(diff, 0, sizeof(diff));
    for (int i = 1; i <= k; i++) {
        diff[s[i]] += d[i];
        diff[t[i] + 1] -= d[i];
    }
    long long sum = 0;
    for (int i = 1; i <= n; i++) {
        sum += diff[i];
        if (sum > r[i]) return false;
    }
    return true;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(0);
    cin >> n >> m;
    for (int i = 1; i <= n; i++) cin >> r[i];
    for (int i = 1; i <= m; i++) cin >> d[i] >> s[i] >> t[i];
    
    if (check(m)) {
        cout << 0 << endl;
        return 0;
    }
    
    int left = 1, right = m, ans = 0;
    while (left <= right) {
        int mid = (left + right) / 2;
        if (!check(mid)) {
            ans = mid;
            right = mid - 1;
        } else {
            left = mid + 1;
        }
    }
    cout << -1 << endl << ans << endl;
    return 0;
}