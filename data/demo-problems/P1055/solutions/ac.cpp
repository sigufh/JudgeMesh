#include <iostream>
#include <string>
using namespace std;

int main() {
    string s;
    cin >> s;
    int sum = 0, j = 1;
    for (int i = 0; i < s.size() - 1; i++) {
        if (s[i] != '-') {
            sum += (s[i] - '0') * j;
            j++;
        }
    }
    int mod = sum % 11;
    char check = (mod == 10) ? 'X' : (mod + '0');
    if (check == s.back()) {
        cout << "Right" << endl;
    } else {
        s.back() = check;
        cout << s << endl;
    }
    return 0;
}