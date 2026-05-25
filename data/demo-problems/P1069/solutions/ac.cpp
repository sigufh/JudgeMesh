#include <iostream>
#include <vector>
#include <cmath>
#include <climits>
#include <algorithm>

using namespace std;

// 分解质因数，返回质因数及其指数的列表
vector<pair<int, int>> factorize(int n) {
    vector<pair<int, int>> factors;
    for (int i = 2; i * i <= n; ++i) {
        if (n % i == 0) {
            int cnt = 0;
            while (n % i == 0) {
                n /= i;
                ++cnt;
            }
            factors.emplace_back(i, cnt);
        }
    }
    if (n > 1) {
        factors.emplace_back(n, 1);
    }
    return factors;
}

int main() {
    int N;
    cin >> N;
    int m1, m2;
    cin >> m1 >> m2;
    vector<int> S(N);
    for (int i = 0; i < N; ++i) {
        cin >> S[i];
    }

    // 对 M = m1^m2 进行质因数分解
    auto factors_m = factorize(m1);
    // 将指数乘以 m2
    for (auto& p : factors_m) {
        p.second *= m2;
    }

    int ans = INT_MAX;
    for (int i = 0; i < N; ++i) {
        int si = S[i];
        bool ok = true;
        int max_time = 0;
        // 检查 si 是否包含 M 的所有质因数，并计算所需时间
        for (auto& p : factors_m) {
            int prime = p.first;
            int need = p.second;
            int cnt = 0;
            while (si % prime == 0) {
                si /= prime;
                ++cnt;
            }
            if (cnt == 0) {
                ok = false;
                break;
            }
            // 需要的时间为 ceil(need / cnt)
            int time = (need + cnt - 1) / cnt;
            max_time = max(max_time, time);
        }
        if (ok) {
            ans = min(ans, max_time);
        }
    }

    if (ans == INT_MAX) {
        cout << -1 << endl;
    } else {
        cout << ans << endl;
    }

    return 0;
}