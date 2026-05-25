#include <iostream>
#include <vector>
using namespace std;

int main() {
    int a, n, m, x;
    cin >> a >> n >> m >> x;

    // 上车人数数组 up[i] 表示第 i 站上车人数
    vector<int> up(n + 1, 0);
    // 下车人数数组 down[i] 表示第 i 站下车人数
    vector<int> down(n + 1, 0);
    // 车上人数数组 on[i] 表示第 i 站开出时车上人数
    vector<int> on(n + 1, 0);

    // 第1站
    up[1] = a;
    down[1] = 0;
    on[1] = a;

    // 第2站：上下车人数相同，设为 t，但 t 未知
    // 我们不知道 t，但可以用递推关系，先假设 up[2] = t，down[2] = t
    // 由于 on[2] = on[1] + up[2] - down[2] = a
    // 所以 t 可以是任意值，但后续规律会确定 t

    // 从第3站开始，上车人数 = 前两站上车人数之和
    // 下车人数 = 上一站上车人数
    // 我们需要找到 t 使得第 n 站下车人数为 m（全部下车）
    // 第 n 站下车人数 = 第 n-1 站上车人数

    // 设 up[2] = t，则：
    // up[3] = up[1] + up[2] = a + t
    // up[4] = up[2] + up[3] = t + (a + t) = a + 2t
    // up[5] = up[3] + up[4] = (a + t) + (a + 2t) = 2a + 3t
    // 可以发现 up[i] 可以表示为 A[i] * a + B[i] * t 的形式
    // 其中 A[1]=1, B[1]=0; A[2]=0, B[2]=1; A[3]=1, B[3]=1; A[4]=1, B[4]=2; A[5]=2, B[5]=3; ...
    // 实际上 A 和 B 满足斐波那契数列的变形

    vector<int> A(n + 1, 0), B(n + 1, 0);
    A[1] = 1; B[1] = 0;
    A[2] = 0; B[2] = 1;
    for (int i = 3; i <= n; i++) {
        A[i] = A[i - 1] + A[i - 2];
        B[i] = B[i - 1] + B[i - 2];
    }

    // 第 n 站下车人数 = 第 n-1 站上车人数 = up[n-1] = A[n-1]*a + B[n-1]*t
    // 已知这个值等于 m
    // 所以 m = A[n-1]*a + B[n-1]*t
    // 如果 B[n-1] == 0，则 t 可以是任意值，但题目保证有解，且 n>=3 时 B[n-1] > 0
    int t;
    if (B[n - 1] != 0) {
        t = (m - A[n - 1] * a) / B[n - 1];
    } else {
        // 理论上不会出现，因为 n>=3 时 B[n-1] >= 1
        t = 0;
    }

    // 现在计算第 x 站开出时车上的人数
    // on[x] = on[1] + sum_{i=2}^{x} (up[i] - down[i])
    // 注意 down[i] = up[i-1] (i>=3)，down[2] = up[2] = t
    // 所以 on[x] = a + (up[2] - down[2]) + sum_{i=3}^{x} (up[i] - up[i-1])
    //            = a + 0 + (up[x] - up[2]) = a + up[x] - t
    // 验证：on[2] = a + up[2] - t = a，正确
    // on[3] = a + up[3] - t = a + (a+t) - t = 2a，正确吗？我们验证一下：
    // 第3站上车 a+t，下车 up[2]=t，所以 on[3] = on[2] + (a+t) - t = a + a = 2a，正确
    // 所以公式 on[x] = a + up[x] - t 成立

    int up_x = A[x] * a + B[x] * t;
    int ans = a + up_x - t;
    cout << ans << endl;

    return 0;
}