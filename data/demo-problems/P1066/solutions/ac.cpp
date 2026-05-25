#include <iostream>
#include <vector>
#include <algorithm>
#include <string>

using namespace std;

// 高精度加法
vector<int> add(const vector<int>& a, const vector<int>& b) {
    vector<int> res;
    int carry = 0;
    size_t max_len = max(a.size(), b.size());
    for (size_t i = 0; i < max_len; ++i) {
        int digit_a = (i < a.size()) ? a[i] : 0;
        int digit_b = (i < b.size()) ? b[i] : 0;
        int sum = digit_a + digit_b + carry;
        res.push_back(sum % 10);
        carry = sum / 10;
    }
    if (carry) res.push_back(carry);
    return res;
}

// 高精度减法，假设 a >= b
vector<int> sub(const vector<int>& a, const vector<int>& b) {
    vector<int> res;
    int borrow = 0;
    for (size_t i = 0; i < a.size(); ++i) {
        int digit_a = a[i];
        int digit_b = (i < b.size()) ? b[i] : 0;
        int diff = digit_a - digit_b - borrow;
        if (diff < 0) {
            diff += 10;
            borrow = 1;
        } else {
            borrow = 0;
        }
        res.push_back(diff);
    }
    while (res.size() > 1 && res.back() == 0) res.pop_back();
    return res;
}

// 高精度比较 a >= b
bool ge(const vector<int>& a, const vector<int>& b) {
    if (a.size() != b.size()) return a.size() > b.size();
    for (int i = a.size() - 1; i >= 0; --i) {
        if (a[i] != b[i]) return a[i] > b[i];
    }
    return true;
}

// 高精度输出
void print(const vector<int>& num) {
    for (int i = num.size() - 1; i >= 0; --i) {
        cout << num[i];
    }
}

int main() {
    int k, w;
    cin >> k >> w;

    int max_digit = (1 << k) - 1; // 2^k - 1
    int max_len = (w + k - 1) / k; // 最多段数

    // 组合数 C(n, m) 高精度计算
    // 使用递推 C(n, m) = C(n-1, m-1) + C(n-1, m)
    // 只需要计算到 n = max_digit, m = max_len
    vector<vector<vector<int>>> C(max_digit + 1, vector<vector<int>>(max_len + 1));
    for (int i = 0; i <= max_digit; ++i) {
        C[i][0] = {1}; // C(i,0)=1
        int limit = min(i, max_len);
        for (int j = 1; j <= limit; ++j) {
            C[i][j] = add(C[i-1][j-1], C[i-1][j]);
        }
    }

    vector<int> ans = {0};

    // 枚举位数
    for (int len = 2; len <= max_len; ++len) {
        // 最高位不能为0，且严格递增，所以从 len-1 到 max_digit-1 选 len-1 个数
        // 组合数 C(max_digit, len)
        // 但需要考虑最高位的限制
        // 实际上，r 的每一位在 1..max_digit 中选，且严格递增，所以总数为 C(max_digit, len)
        // 但是最高位不能为0，所以就是 C(max_digit, len)
        if (len <= max_digit) {
            ans = add(ans, C[max_digit][len]);
        }
    }

    // 处理最高位受限的情况
    // 当 w 不是 k 的整数倍时，最高段位数不足 k 位
    int first_len = w % k;
    if (first_len != 0) {
        int max_first = (1 << first_len) - 1; // 最高位能取的最大值
        // 此时总段数为 max_len，最高位取值 1..max_first
        // 后面还有 max_len-1 位，从大于最高位的数中选，即从 max_digit - 最高位 中选 max_len-1 个
        // 需要减去之前已经计算过的 C(max_digit, max_len) 中最高位超过 max_first 的部分
        // 更简单：直接计算最高位受限的情况
        // 对于最高位 i 从 1 到 max_first，后面选 max_len-1 个，从 i+1..max_digit 中选
        // 组合数为 C(max_digit - i, max_len - 1)
        // 累加
        for (int i = 1; i <= max_first; ++i) {
            int n = max_digit - i;
            int m = max_len - 1;
            if (n >= m) {
                ans = add(ans, C[n][m]);
            }
        }
        // 但是之前 ans 中已经加了 C(max_digit, max_len)，其中包含了最高位超过 max_first 的情况
        // 所以需要减去那些情况，或者直接重新计算
        // 这里采用重新计算的方式：先清空 ans，然后分别处理完整段和不足段
        // 重新组织代码
        ans = {0};
        // 完整段情况：段数从 2 到 max_len-1
        for (int len = 2; len <= max_len - 1; ++len) {
            if (len <= max_digit) {
                ans = add(ans, C[max_digit][len]);
            }
        }
        // 不足段情况：段数为 max_len，最高位受限
        for (int i = 1; i <= max_first; ++i) {
            int n = max_digit - i;
            int m = max_len - 1;
            if (n >= m) {
                ans = add(ans, C[n][m]);
            }
        }
    }

    print(ans);
    cout << endl;
    return 0;
}