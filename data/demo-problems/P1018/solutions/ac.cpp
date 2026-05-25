#include <iostream>
#include <string>
#include <algorithm>
#include <cstring>
using namespace std;

const int MAXN = 45;
const int MAXK = 10;

// 大数结构，支持乘法、比较和赋值
struct BigInt {
    int len;
    int d[100]; // 倒序存储，d[0]是最低位
    
    BigInt() {
        len = 1;
        memset(d, 0, sizeof(d));
    }
    
    BigInt(const string& s) {
        len = s.length();
        memset(d, 0, sizeof(d));
        for (int i = 0; i < len; i++) {
            d[i] = s[len - 1 - i] - '0';
        }
    }
    
    BigInt operator*(const BigInt& other) const {
        BigInt res;
        res.len = len + other.len;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < other.len; j++) {
                res.d[i + j] += d[i] * other.d[j];
                res.d[i + j + 1] += res.d[i + j] / 10;
                res.d[i + j] %= 10;
            }
        }
        while (res.len > 1 && res.d[res.len - 1] == 0) res.len--;
        return res;
    }
    
    bool operator<(const BigInt& other) const {
        if (len != other.len) return len < other.len;
        for (int i = len - 1; i >= 0; i--) {
            if (d[i] != other.d[i]) return d[i] < other.d[i];
        }
        return false;
    }
    
    void print() const {
        for (int i = len - 1; i >= 0; i--) {
            cout << d[i];
        }
    }
};

BigInt dp[MAXN][MAXK]; // dp[i][j] 表示前i个数字插入j个乘号的最大乘积
string num;

int main() {
    int N, K;
    cin >> N >> K;
    cin >> num;
    
    // 初始化：没有乘号的情况
    for (int i = 1; i <= N; i++) {
        dp[i][0] = BigInt(num.substr(0, i));
    }
    
    // 动态规划
    for (int i = 1; i <= N; i++) {
        for (int j = 1; j <= K; j++) {
            if (j >= i) continue; // 乘号数不能超过数字个数-1
            dp[i][j] = BigInt("0");
            for (int k = j; k < i; k++) {
                // 最后一段数字从k+1到i
                BigInt last = BigInt(num.substr(k, i - k));
                BigInt prod = dp[k][j-1] * last;
                if (dp[i][j] < prod) {
                    dp[i][j] = prod;
                }
            }
        }
    }
    
    dp[N][K].print();
    cout << endl;
    
    return 0;
}