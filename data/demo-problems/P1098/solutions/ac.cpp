#include <iostream>
#include <string>
#include <cctype>
using namespace std;

int main() {
    int p1, p2, p3;
    cin >> p1 >> p2 >> p3;
    string s;
    cin >> s;
    string ans;
    for (size_t i = 0; i < s.size(); ++i) {
        if (s[i] != '-' || i == 0 || i == s.size() - 1) {
            ans += s[i];
            continue;
        }
        char left = s[i - 1];
        char right = s[i + 1];
        if (!((isdigit(left) && isdigit(right)) || (islower(left) && islower(right)))) {
            ans += s[i];
            continue;
        }
        if (right <= left) {
            ans += s[i];
            continue;
        }
        if (right == left + 1) {
            continue;
        }
        string fill;
        for (char c = left + 1; c < right; ++c) {
            char ch = c;
            if (p1 == 2 && islower(ch)) {
                ch = toupper(ch);
            } else if (p1 == 3) {
                ch = '*';
            }
            fill += string(p2, ch);
        }
        if (p3 == 2) {
            reverse(fill.begin(), fill.end());
        }
        ans += fill;
    }
    cout << ans << endl;
    return 0;
}