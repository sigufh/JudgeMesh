#include <bits/stdc++.h>
using namespace std;

const int MAXN = 50005;
int n;
int wish[MAXN][2];
int pos[MAXN]; // 目标环中编号为i的人在位置pos[i]
int target[MAXN]; // 目标环中位置i的人的编号
int cnt1[MAXN], cnt2[MAXN];

int main() {
    ios::sync_with_stdio(false);
    cin.tie(0);
    cin >> n;
    for (int i = 1; i <= n; ++i) {
        cin >> wish[i][0] >> wish[i][1];
    }
    // 检查是否每个愿望都是相互的，并且每个人恰好被两个人希望相邻
    for (int i = 1; i <= n; ++i) {
        int a = wish[i][0], b = wish[i][1];
        bool ok = false;
        if ((wish[a][0] == i || wish[a][1] == i) && (wish[b][0] == i || wish[b][1] == i)) {
            ok = true;
        }
        if (!ok) {
            cout << -1 << endl;
            return 0;
        }
    }
    // 构建目标环
    target[1] = 1;
    pos[1] = 1;
    int cur = 1;
    int prev = 1;
    // 选择wish[1][0]作为下一个
    int next = wish[1][0];
    for (int i = 2; i <= n; ++i) {
        target[i] = next;
        pos[next] = i;
        // 找到next的下一个，不能是prev
        if (wish[next][0] == prev) {
            prev = next;
            next = wish[next][1];
        } else {
            prev = next;
            next = wish[next][0];
        }
    }
    // 检查是否形成环
    if (next != 1) {
        cout << -1 << endl;
        return 0;
    }
    // 计算顺时针和逆时针的偏移量
    memset(cnt1, 0, sizeof(cnt1));
    memset(cnt2, 0, sizeof(cnt2));
    for (int i = 1; i <= n; ++i) {
        int offset = (pos[i] - i + n) % n;
        cnt1[offset]++;
        offset = (pos[i] - (n - i + 1) + n) % n; // 逆时针相当于反转后的位置
        cnt2[offset]++;
    }
    int max_match = 0;
    for (int i = 0; i < n; ++i) {
        max_match = max(max_match, cnt1[i]);
        max_match = max(max_match, cnt2[i]);
    }
    cout << n - max_match << endl;
    return 0;
}