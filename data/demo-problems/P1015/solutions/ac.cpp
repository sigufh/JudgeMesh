#include <bits/stdc++.h>
using namespace std;

// 将字符转换为对应的数值
int charToVal(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    return c - 'A' + 10;
}

// 将数值转换为字符
char valToChar(int v) {
    if (v < 10) return '0' + v;
    return 'A' + v - 10;
}

// 判断字符串是否为回文
bool isPalindrome(const string& s) {
    int l = 0, r = s.size() - 1;
    while (l < r) {
        if (s[l] != s[r]) return false;
        l++; r--;
    }
    return true;
}

// N进制加法，返回相加后的字符串
string addInBase(const string& a, const string& b, int base) {
    string res;
    int carry = 0;
    int i = a.size() - 1, j = b.size() - 1;
    while (i >= 0 || j >= 0 || carry) {
        int sum = carry;
        if (i >= 0) sum += charToVal(a[i--]);
        if (j >= 0) sum += charToVal(b[j--]);
        carry = sum / base;
        res.push_back(valToChar(sum % base));
    }
    reverse(res.begin(), res.end());
    return res;
}

int main() {
    int N;
    string M;
    cin >> N >> M;

    for (int step = 1; step <= 30; step++) {
        string rev = M;
        reverse(rev.begin(), rev.end());
        M = addInBase(M, rev, N);
        if (isPalindrome(M)) {
            cout << "STEP=" << step << endl;
            return 0;
        }
    }
    cout << "Impossible!" << endl;
    return 0;
}