#include <iostream>
#include <string>
#include <iomanip>
#include <cctype>
using namespace std;

int main() {
    string eq;
    cin >> eq;
    
    int coeff = 0, constant = 0;
    int sign = 1; // 1 for left side, -1 for right side
    int current_sign = 1; // 1 for positive, -1 for negative
    int num = 0;
    bool has_num = false;
    char var = 0;
    
    for (size_t i = 0; i < eq.length(); ++i) {
        char c = eq[i];
        if (c == '=') {
            // process any pending number
            if (has_num) {
                constant += sign * current_sign * num;
                has_num = false;
                num = 0;
            }
            sign = -1;
            current_sign = 1;
        } else if (c == '+' || c == '-') {
            // process previous number
            if (has_num) {
                constant += sign * current_sign * num;
                has_num = false;
                num = 0;
            }
            if (c == '+') current_sign = 1;
            else current_sign = -1;
        } else if (isdigit(c)) {
            num = num * 10 + (c - '0');
            has_num = true;
        } else if (islower(c)) {
            var = c;
            if (!has_num) num = 1;
            coeff += sign * current_sign * num;
            has_num = false;
            num = 0;
        }
    }
    // process any remaining number at the end
    if (has_num) {
        constant += sign * current_sign * num;
    }
    
    // Equation: coeff * var + constant = 0  => var = -constant / coeff
    double result = -static_cast<double>(constant) / coeff;
    // handle -0.000 case
    if (result == 0.0) result = 0.0;
    cout << fixed << setprecision(3) << result << endl;
    
    return 0;
}