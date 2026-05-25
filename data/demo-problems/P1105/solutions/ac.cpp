#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

struct Platform {
    int id, h, l, r;
};

int main() {
    int N;
    cin >> N;
    vector<Platform> platforms(N);
    for (int i = 0; i < N; ++i) {
        cin >> platforms[i].h >> platforms[i].l >> platforms[i].r;
        platforms[i].id = i + 1;
    }

    // 按高度从高到低排序，高度相同按编号从小到大
    sort(platforms.begin(), platforms.end(), [](const Platform& a, const Platform& b) {
        if (a.h != b.h) return a.h > b.h;
        return a.id < b.id;
    });

    vector<int> left_ans(N + 1, 0), right_ans(N + 1, 0);

    for (int i = 0; i < N; ++i) {
        int cur_id = platforms[i].id;
        int cur_h = platforms[i].h;
        int cur_l = platforms[i].l;
        int cur_r = platforms[i].r;

        // 找左边缘下方平台
        int best_left = 0, best_left_h = -1;
        for (int j = i + 1; j < N; ++j) {
            if (platforms[j].h < cur_h && platforms[j].l < cur_l && platforms[j].r > cur_l) {
                if (platforms[j].h > best_left_h) {
                    best_left_h = platforms[j].h;
                    best_left = platforms[j].id;
                } else if (platforms[j].h == best_left_h && platforms[j].id < best_left) {
                    best_left = platforms[j].id;
                }
            }
        }
        left_ans[cur_id] = best_left;

        // 找右边缘下方平台
        int best_right = 0, best_right_h = -1;
        for (int j = i + 1; j < N; ++j) {
            if (platforms[j].h < cur_h && platforms[j].l < cur_r && platforms[j].r > cur_r) {
                if (platforms[j].h > best_right_h) {
                    best_right_h = platforms[j].h;
                    best_right = platforms[j].id;
                } else if (platforms[j].h == best_right_h && platforms[j].id < best_right) {
                    best_right = platforms[j].id;
                }
            }
        }
        right_ans[cur_id] = best_right;
    }

    for (int i = 1; i <= N; ++i) {
        cout << left_ans[i] << " " << right_ans[i] << endl;
    }

    return 0;
}