#include <iostream>
#include <algorithm>
using namespace std;

int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
}

int main() {
    int x0, y0;
    cin >> x0 >> y0;
    
    if (y0 % x0 != 0) {
        cout << 0 << endl;
        return 0;
    }
    
    int n = y0 / x0;
    int count = 0;
    
    for (int i = 1; i * i <= n; i++) {
        if (n % i == 0) {
            int a = i;
            int b = n / i;
            if (gcd(a, b) == 1) {
                if (a == b) count++;
                else count += 2;
            }
        }
    }
    
    cout << count << endl;
    return 0;
}