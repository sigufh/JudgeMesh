#include <bits/stdc++.h>
using namespace std;

int n;
string s1, s2, s3;
int val[26];
bool used[26];
bool found = false;

void dfs(int pos, int carry, int mask) {
    if (found) return;
    if (pos == n) {
        if (carry == 0) {
            found = true;
            for (int i = 0; i < n; ++i) {
                cout << val[i] << (i == n - 1 ? '\n' : ' ');
            }
        }
        return;
    }
    int idx1 = s1[n - 1 - pos] - 'A';
    int idx2 = s2[n - 1 - pos] - 'A';
    int idx3 = s3[n - 1 - pos] - 'A';
    if (val[idx1] != -1 && val[idx2] != -1 && val[idx3] != -1) {
        int sum = val[idx1] + val[idx2] + carry;
        if (sum % n == val[idx3]) {
            dfs(pos + 1, sum / n, mask);
        }
        return;
    }
    if (val[idx1] != -1 && val[idx2] != -1) {
        int sum = val[idx1] + val[idx2] + carry;
        int d = sum % n;
        if (val[idx3] == -1 && !used[d]) {
            val[idx3] = d;
            used[d] = true;
            dfs(pos + 1, sum / n, mask);
            val[idx3] = -1;
            used[d] = false;
        } else if (val[idx3] == d) {
            dfs(pos + 1, sum / n, mask);
        }
        return;
    }
    if (val[idx1] != -1 && val[idx3] != -1) {
        int d = (val[idx3] - val[idx1] - carry + n) % n;
        if (val[idx2] == -1 && !used[d]) {
            val[idx2] = d;
            used[d] = true;
            int sum = val[idx1] + val[idx2] + carry;
            dfs(pos + 1, sum / n, mask);
            val[idx2] = -1;
            used[d] = false;
        } else if (val[idx2] == d) {
            int sum = val[idx1] + val[idx2] + carry;
            if (sum % n == val[idx3]) {
                dfs(pos + 1, sum / n, mask);
            }
        }
        return;
    }
    if (val[idx2] != -1 && val[idx3] != -1) {
        int d = (val[idx3] - val[idx2] - carry + n) % n;
        if (val[idx1] == -1 && !used[d]) {
            val[idx1] = d;
            used[d] = true;
            int sum = val[idx1] + val[idx2] + carry;
            dfs(pos + 1, sum / n, mask);
            val[idx1] = -1;
            used[d] = false;
        } else if (val[idx1] == d) {
            int sum = val[idx1] + val[idx2] + carry;
            if (sum % n == val[idx3]) {
                dfs(pos + 1, sum / n, mask);
            }
        }
        return;
    }
    if (val[idx1] != -1) {
        for (int d2 = 0; d2 < n; ++d2) {
            if (used[d2]) continue;
            int sum = val[idx1] + d2 + carry;
            int d3 = sum % n;
            if (val[idx3] == -1 && !used[d3] && d3 != d2) {
                val[idx2] = d2;
                val[idx3] = d3;
                used[d2] = used[d3] = true;
                dfs(pos + 1, sum / n, mask);
                val[idx2] = val[idx3] = -1;
                used[d2] = used[d3] = false;
            } else if (val[idx3] == d3 && !used[d2] && d2 != d3) {
                val[idx2] = d2;
                used[d2] = true;
                dfs(pos + 1, sum / n, mask);
                val[idx2] = -1;
                used[d2] = false;
            }
        }
        return;
    }
    if (val[idx2] != -1) {
        for (int d1 = 0; d1 < n; ++d1) {
            if (used[d1]) continue;
            int sum = d1 + val[idx2] + carry;
            int d3 = sum % n;
            if (val[idx3] == -1 && !used[d3] && d3 != d1) {
                val[idx1] = d1;
                val[idx3] = d3;
                used[d1] = used[d3] = true;
                dfs(pos + 1, sum / n, mask);
                val[idx1] = val[idx3] = -1;
                used[d1] = used[d3] = false;
            } else if (val[idx3] == d3 && !used[d1] && d1 != d3) {
                val[idx1] = d1;
                used[d1] = true;
                dfs(pos + 1, sum / n, mask);
                val[idx1] = -1;
                used[d1] = false;
            }
        }
        return;
    }
    if (val[idx3] != -1) {
        for (int d1 = 0; d1 < n; ++d1) {
            if (used[d1]) continue;
            int d2 = (val[idx3] - d1 - carry + n) % n;
            if (!used[d2] && d2 != d1) {
                val[idx1] = d1;
                val[idx2] = d2;
                used[d1] = used[d2] = true;
                int sum = d1 + d2 + carry;
                dfs(pos + 1, sum / n, mask);
                val[idx1] = val[idx2] = -1;
                used[d1] = used[d2] = false;
            }
        }
        return;
    }
    for (int d1 = 0; d1 < n; ++d1) {
        if (used[d1]) continue;
        for (int d2 = 0; d2 < n; ++d2) {
            if (used[d2] || d2 == d1) continue;
            int sum = d1 + d2 + carry;
            int d3 = sum % n;
            if (!used[d3] && d3 != d1 && d3 != d2) {
                val[idx1] = d1;
                val[idx2] = d2;
                val[idx3] = d3;
                used[d1] = used[d2] = used[d3] = true;
                dfs(pos + 1, sum / n, mask);
                val[idx1] = val[idx2] = val[idx3] = -1;
                used[d1] = used[d2] = used[d3] = false;
            }
        }
    }
}

int main() {
    cin >> n >> s1 >> s2 >> s3;
    memset(val, -1, sizeof(val));
    memset(used, 0, sizeof(used));
    dfs(0, 0, 0);
    return 0;
}