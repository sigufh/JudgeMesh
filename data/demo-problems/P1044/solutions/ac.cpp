#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    // 卡特兰数 C(2n, n) / (n + 1)
    long long ans = 1;
    for (int i = 1; i <= n; ++i) {
        ans = ans * (2 * n - i + 1) / i;
    }
    ans /= (n + 1);
    cout << ans << endl;
    return 0;
}