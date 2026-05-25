#include <iostream>
#include <vector>
#include <algorithm>
#include <string>
using namespace std;

// 高精度加法
string add(const string& a, const string& b) {
    string res;
    int carry = 0;
    int i = a.size() - 1, j = b.size() - 1;
    while (i >= 0 || j >= 0 || carry) {
        int sum = carry;
        if (i >= 0) sum += a[i--] - '0';
        if (j >= 0) sum += b[j--] - '0';
        carry = sum / 10;
        res.push_back(sum % 10 + '0');
    }
    reverse(res.begin(), res.end());
    return res;
}

// 高精度乘法（大数乘以小整数）
string mul(const string& a, int b) {
    string res;
    int carry = 0;
    for (int i = a.size() - 1; i >= 0; --i) {
        int prod = (a[i] - '0') * b + carry;
        carry = prod / 10;
        res.push_back(prod % 10 + '0');
    }
    while (carry) {
        res.push_back(carry % 10 + '0');
        carry /= 10;
    }
    reverse(res.begin(), res.end());
    return res;
}

int main() {
    int n;
    cin >> n;
    // A_n = 2^{n+1} - 2
    // 计算 2^{n+1}
    string pow2 = "1";
    for (int i = 0; i < n + 1; ++i) {
        pow2 = mul(pow2, 2);
    }
    // 减去 2
    // 高精度减法：pow2 - 2
    string res;
    int borrow = 0;
    int i = pow2.size() - 1;
    // 处理个位减2
    int digit = (pow2[i] - '0') - 2 - borrow;
    if (digit < 0) {
        digit += 10;
        borrow = 1;
    } else {
        borrow = 0;
    }
    res.push_back(digit + '0');
    --i;
    // 处理剩余位
    while (i >= 0) {
        digit = (pow2[i] - '0') - borrow;
        if (digit < 0) {
            digit += 10;
            borrow = 1;
        } else {
            borrow = 0;
        }
        res.push_back(digit + '0');
        --i;
    }
    // 去除前导零
    while (res.size() > 1 && res.back() == '0') {
        res.pop_back();
    }
    reverse(res.begin(), res.end());
    cout << res << endl;
    return 0;
}