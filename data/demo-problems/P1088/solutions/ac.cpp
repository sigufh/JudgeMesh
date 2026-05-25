#include <iostream>
#include <vector>
#include <algorithm>

using namespace std;

int main() {
    int N, M;
    cin >> N >> M;
    vector<int> perm(N);
    for (int i = 0; i < N; ++i) {
        cin >> perm[i];
    }
    // 执行 M 次下一个排列
    for (int i = 0; i < M; ++i) {
        next_permutation(perm.begin(), perm.end());
    }
    // 输出结果
    for (int i = 0; i < N; ++i) {
        if (i > 0) cout << " ";
        cout << perm[i];
    }
    cout << endl;
    return 0;
}