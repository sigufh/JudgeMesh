#include <iostream>
#include <vector>
#include <algorithm>
#include <cmath>

using namespace std;

struct Peanut {
    int x, y, value;
    Peanut(int x, int y, int value) : x(x), y(y), value(value) {}
};

bool cmp(const Peanut &a, const Peanut &b) {
    return a.value > b.value;
}

int main() {
    int M, N, K;
    cin >> M >> N >> K;
    vector<Peanut> peanuts;
    for (int i = 1; i <= M; ++i) {
        for (int j = 1; j <= N; ++j) {
            int p;
            cin >> p;
            if (p > 0) {
                peanuts.emplace_back(i, j, p);
            }
        }
    }
    sort(peanuts.begin(), peanuts.end(), cmp);
    if (peanuts.empty()) {
        cout << 0 << endl;
        return 0;
    }
    int time = 0, ans = 0;
    int cur_x = 0, cur_y = peanuts[0].y;
    for (size_t i = 0; i < peanuts.size(); ++i) {
        int nx = peanuts[i].x, ny = peanuts[i].y;
        int dist = abs(nx - cur_x) + abs(ny - cur_y);
        int pick_time = 1;
        int back_time = nx;
        if (i == 0) {
            if (dist + pick_time + back_time <= K) {
                time += dist + pick_time;
                ans += peanuts[i].value;
                cur_x = nx;
                cur_y = ny;
            } else {
                break;
            }
        } else {
            if (time + dist + pick_time + back_time <= K) {
                time += dist + pick_time;
                ans += peanuts[i].value;
                cur_x = nx;
                cur_y = ny;
            } else {
                break;
            }
        }
    }
    cout << ans << endl;
    return 0;
}