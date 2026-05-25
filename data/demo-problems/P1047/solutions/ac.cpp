#include <iostream>
#include <vector>
using namespace std;

int main() {
    int l, m;
    cin >> l >> m;
    vector<bool> tree(l + 1, true);
    for (int i = 0; i < m; ++i) {
        int u, v;
        cin >> u >> v;
        for (int j = u; j <= v; ++j) {
            tree[j] = false;
        }
    }
    int count = 0;
    for (int i = 0; i <= l; ++i) {
        if (tree[i]) ++count;
    }
    cout << count << endl;
    return 0;
}