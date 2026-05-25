#include <iostream>
#include <string>
using namespace std;

char getType(const string& s) {
    bool has0 = false, has1 = false;
    for (char c : s) {
        if (c == '0') has0 = true;
        else if (c == '1') has1 = true;
    }
    if (has0 && has1) return 'F';
    if (has0) return 'B';
    return 'I';
}

void postorder(const string& s) {
    if (s.empty()) return;
    if (s.length() == 1) {
        cout << getType(s);
        return;
    }
    int mid = s.length() / 2;
    string left = s.substr(0, mid);
    string right = s.substr(mid);
    postorder(left);
    postorder(right);
    cout << getType(s);
}

int main() {
    int N;
    cin >> N;
    string s;
    cin >> s;
    postorder(s);
    cout << endl;
    return 0;
}