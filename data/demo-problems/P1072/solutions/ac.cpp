#include <iostream>
#include <cmath>
using namespace std;

int gcd(int a, int b) {
    while (b) {
        int t = a % b;
        a = b;
        b = t;
    }
    return a;
}

int main() {
    int n;
    cin >> n;
    while (n--) {
        int a0, a1, b0, b1;
        cin >> a0 >> a1 >> b0 >> b1;
        int ans = 0;
        int p = a0 / a1, q = b1 / b0;
        // 枚举 b1 的因子
        int limit = sqrt(b1);
        for (int i = 1; i <= limit; ++i) {
            if (b1 % i == 0) {
                // 检查 i
                if (i % a1 == 0 && gcd(i / a1, p) == 1 && gcd(q, b1 / i) == 1) {
                    ++ans;
                }
                int j = b1 / i;
                if (j != i) {
                    if (j % a1 == 0 && gcd(j / a1, p) == 1 && gcd(q, b1 / j) == 1) {
                        ++ans;
                    }
                }
            }
        }
        cout << ans << endl;
    }
    return 0;
}