#include <iostream>
#include <cmath>
using namespace std;

int main() {
    int n;
    cin >> n;
    int coeff[101];
    for (int i = 0; i <= n; ++i) {
        cin >> coeff[i];
    }
    
    bool first = true;
    for (int i = 0; i <= n; ++i) {
        int exp = n - i;
        int a = coeff[i];
        if (a == 0) continue;
        
        if (first) {
            if (a < 0) cout << "-";
            first = false;
        } else {
            if (a > 0) cout << "+";
            else cout << "-";
        }
        
        int abs_a = abs(a);
        if (exp == 0) {
            cout << abs_a;
        } else if (exp == 1) {
            if (abs_a != 1) cout << abs_a;
            cout << "x";
        } else {
            if (abs_a != 1) cout << abs_a;
            cout << "x^" << exp;
        }
    }
    
    if (first) cout << "0";
    cout << endl;
    return 0;
}