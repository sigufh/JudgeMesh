#include <iostream>
#include <vector>
#include <cmath>
#include <iomanip>
using namespace std;

// 高精度乘法，只保留最后500位
vector<int> multiply(const vector<int>& a, const vector<int>& b) {
    vector<int> res(500, 0);
    for (int i = 0; i < 500; ++i) {
        if (a[i] == 0) continue;
        for (int j = 0; j < 500 - i; ++j) {
            res[i + j] += a[i] * b[j];
            if (res[i + j] >= 10) {
                res[i + j + 1] += res[i + j] / 10;
                res[i + j] %= 10;
            }
        }
    }
    return res;
}

// 快速幂，只保留最后500位
vector<int> power(int p) {
    vector<int> base(500, 0);
    base[0] = 2;
    vector<int> result(500, 0);
    result[0] = 1;
    while (p > 0) {
        if (p & 1) {
            result = multiply(result, base);
        }
        base = multiply(base, base);
        p >>= 1;
    }
    return result;
}

int main() {
    int P;
    cin >> P;
    
    // 计算位数
    int digits = (int)(P * log10(2)) + 1;
    cout << digits << endl;
    
    // 计算2^P - 1的最后500位
    vector<int> num = power(P);
    // 减1
    num[0] -= 1;
    for (int i = 0; i < 500; ++i) {
        if (num[i] < 0) {
            num[i] += 10;
            num[i + 1] -= 1;
        } else {
            break;
        }
    }
    
    // 输出最后500位，每行50位，高位补0
    for (int i = 499; i >= 0; --i) {
        cout << num[i];
        if (i % 50 == 0) cout << endl;
    }
    
    return 0;
}