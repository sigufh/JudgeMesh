#include <bits/stdc++.h>
using namespace std;

struct Minister {
    int left, right;
    long long prod; // left * right
};

bool cmp(const Minister &x, const Minister &y) {
    return x.prod < y.prod;
}

// 高精度乘法：大数乘int
vector<int> mul(const vector<int> &A, int b) {
    vector<int> C;
    int t = 0;
    for (int i = 0; i < A.size() || t; i++) {
        if (i < A.size()) t += A[i] * b;
        C.push_back(t % 10);
        t /= 10;
    }
    while (C.size() > 1 && C.back() == 0) C.pop_back();
    return C;
}

// 高精度除法：大数除以int，返回商（向下取整）
vector<int> div(const vector<int> &A, int b) {
    vector<int> C;
    long long r = 0;
    for (int i = A.size() - 1; i >= 0; i--) {
        r = r * 10 + A[i];
        C.push_back(r / b);
        r %= b;
    }
    reverse(C.begin(), C.end());
    while (C.size() > 1 && C.back() == 0) C.pop_back();
    return C;
}

// 比较两个大数，返回true如果A < B
bool less_than(const vector<int> &A, const vector<int> &B) {
    if (A.size() != B.size()) return A.size() < B.size();
    for (int i = A.size() - 1; i >= 0; i--) {
        if (A[i] != B[i]) return A[i] < B[i];
    }
    return false;
}

// 打印大数
void print(const vector<int> &A) {
    for (int i = A.size() - 1; i >= 0; i--) cout << A[i];
    cout << endl;
}

int main() {
    int n;
    cin >> n;
    int king_left, king_right;
    cin >> king_left >> king_right;
    vector<Minister> ministers(n);
    for (int i = 0; i < n; i++) {
        cin >> ministers[i].left >> ministers[i].right;
        ministers[i].prod = (long long)ministers[i].left * ministers[i].right;
    }
    sort(ministers.begin(), ministers.end(), cmp);

    // 当前乘积，初始为国王左手数
    vector<int> product;
    int temp = king_left;
    while (temp) {
        product.push_back(temp % 10);
        temp /= 10;
    }
    if (product.empty()) product.push_back(0);

    vector<int> max_reward;
    max_reward.push_back(0); // 初始为0

    for (int i = 0; i < n; i++) {
        // 计算当前大臣的奖励 = product / right
        vector<int> reward = div(product, ministers[i].right);
        if (less_than(max_reward, reward)) {
            max_reward = reward;
        }
        // 更新乘积
        product = mul(product, ministers[i].left);
    }

    print(max_reward);
    return 0;
}