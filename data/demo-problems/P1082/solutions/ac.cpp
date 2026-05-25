#include <iostream>
#include <algorithm>
using namespace std;

// 扩展欧几里得算法，求 ax + by = gcd(a, b) 的解
long long exgcd(long long a, long long b, long long &x, long long &y) {
    if (b == 0) {
        x = 1;
        y = 0;
        return a;
    }
    long long d = exgcd(b, a % b, y, x);
    y -= a / b * x;
    return d;
}

int main() {
    long long a, b;
    cin >> a >> b;
    long long x, y;
    long long d = exgcd(a, b, x, y);
    // 根据题意，d 一定为 1，因为方程有解
    // 求最小正整数解
    x = (x % b + b) % b;
    cout << x << endl;
    return 0;
}