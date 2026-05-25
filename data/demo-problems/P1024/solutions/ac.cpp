#include <iostream>
#include <iomanip>
#include <cmath>
using namespace std;

double a, b, c, d;

double f(double x) {
    return a * x * x * x + b * x * x + c * x + d;
}

double find_root(double l, double r) {
    double mid;
    while (r - l > 1e-4) {
        mid = (l + r) / 2;
        if (f(mid) == 0) return mid;
        if (f(l) * f(mid) < 0) r = mid;
        else l = mid;
    }
    return (l + r) / 2;
}

int main() {
    cin >> a >> b >> c >> d;
    double roots[3];
    int cnt = 0;
    for (double i = -100; i <= 100; i += 1.0) {
        double l = i, r = i + 1.0;
        if (f(l) == 0) {
            roots[cnt++] = l;
        } else if (f(l) * f(r) < 0) {
            roots[cnt++] = find_root(l, r);
        }
        if (cnt == 3) break;
    }
    cout << fixed << setprecision(2);
    for (int i = 0; i < 3; i++) {
        if (i > 0) cout << " ";
        cout << roots[i];
    }
    cout << endl;
    return 0;
}