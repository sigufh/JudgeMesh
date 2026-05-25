#include <iostream>
#include <vector>
using namespace std;

int main() {
    int N, M;
    cin >> N >> M;
    vector<vector<int>> stairs(N, vector<int>(M));
    vector<vector<int>> signs(N, vector<int>(M));
    for (int i = 0; i < N; ++i) {
        for (int j = 0; j < M; ++j) {
            cin >> stairs[i][j] >> signs[i][j];
        }
    }
    int start;
    cin >> start;

    int ans = 0;
    const int MOD = 20123;
    for (int i = 0; i < N; ++i) {
        ans = (ans + signs[i][start]) % MOD;
        int x = signs[i][start];
        int cnt = 0;
        for (int j = 0; j < M; ++j) {
            if (stairs[i][j] == 1) cnt++;
        }
        int steps = (x - 1) % cnt + 1;
        int pos = start;
        while (true) {
            if (stairs[i][pos] == 1) {
                steps--;
                if (steps == 0) break;
            }
            pos = (pos + 1) % M;
        }
        start = pos;
    }
    cout << ans % MOD << endl;
    return 0;
}