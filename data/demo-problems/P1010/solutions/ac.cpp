#include <iostream>
#include <string>
using namespace std;

string dfs(int n) {
    if (n == 0) return "0";
    if (n == 1) return "2(0)";
    if (n == 2) return "2";
    string res = "";
    bool first = true;
    for (int i = 15; i >= 0; --i) {
        if (n & (1 << i)) {
            if (!first) res += "+";
            first = false;
            if (i == 0) res += "2(0)";
            else if (i == 1) res += "2";
            else res += "2(" + dfs(i) + ")";
        }
    }
    return res;
}

int main() {
    int n;
    cin >> n;
    cout << dfs(n) << endl;
    return 0;
}