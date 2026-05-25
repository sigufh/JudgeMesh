#include <bits/stdc++.h>
using namespace std;

int n;
vector<string> table[10][10];
vector<char> letters;
int val[26];
bool used[10];
int base;

bool check() {
    for (int i = 1; i < n; ++i) {
        for (int j = 1; j < n; ++j) {
            int a = val[letters[i-1] - 'A'];
            int b = val[letters[j-1] - 'A'];
            int sum = a + b;
            string expected;
            if (sum < base) {
                expected = string(1, 'A' + sum);
                for (int k = 0; k < 26; ++k) if (val[k] == sum) { expected = string(1, 'A' + k); break; }
            } else {
                int high = sum / base;
                int low = sum % base;
                string h, l;
                for (int k = 0; k < 26; ++k) {
                    if (val[k] == high) h = string(1, 'A' + k);
                    if (val[k] == low) l = string(1, 'A' + k);
                }
                expected = h + l;
            }
            string got;
            for (auto &s : table[i][j]) got += s;
            if (got != expected) return false;
        }
    }
    return true;
}

bool dfs(int idx) {
    if (idx == letters.size()) {
        for (base = 2; base <= 10; ++base) {
            if (check()) return true;
        }
        return false;
    }
    char c = letters[idx];
    for (int d = 0; d < base; ++d) {
        if (!used[d]) {
            val[c - 'A'] = d;
            used[d] = true;
            if (dfs(idx + 1)) return true;
            used[d] = false;
        }
    }
    return false;
}

int main() {
    cin >> n;
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            string s;
            cin >> s;
            table[i][j].push_back(s);
        }
    }
    for (int i = 1; i < n; ++i) {
        letters.push_back(table[0][i][0][0]);
    }
    memset(val, -1, sizeof(val));
    memset(used, 0, sizeof(used));
    for (base = 2; base <= 10; ++base) {
        if (dfs(0)) {
            for (int i = 0; i < letters.size(); ++i) {
                if (i) cout << " ";
                cout << letters[i] << "=" << val[letters[i] - 'A'];
            }
            cout << "\n" << base << "\n";
            return 0;
        }
    }
    cout << "ERROR!\n";
    return 0;
}