#include <iostream>
#include <string>
#include <algorithm>
using namespace std;

string convertToNegativeBase(int n, int R) {
    if (n == 0) return "0";
    string result = "";
    while (n != 0) {
        int remainder = n % R;
        n /= R;
        if (remainder < 0) {
            remainder -= R;
            n += 1;
        }
        if (remainder < 10) {
            result += char('0' + remainder);
        } else {
            result += char('A' + remainder - 10);
        }
    }
    reverse(result.begin(), result.end());
    return result;
}

int main() {
    int n, R;
    cin >> n >> R;
    string result = convertToNegativeBase(n, R);
    cout << result << "=" << n << "(base" << R << ")" << endl;
    return 0;
}