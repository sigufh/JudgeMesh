#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

int main() {
    int m, n;
    cin >> m >> n;
    vector<vector<int>> a(m, vector<int>(n));
    for (int i = 0; i < m; ++i) {
        for (int j = 0; j < n; ++j) {
            cin >> a[i][j];
        }
    }

    // 计算输出图像的大小
    int max_h = 0;
    for (int i = 0; i < m; ++i) {
        for (int j = 0; j < n; ++j) {
            int h = a[i][j];
            // 每个积木在垂直方向贡献的高度
            int total_h = 1 + 3 * h + 2 * (m - 1 - i);
            max_h = max(max_h, total_h);
        }
    }
    int K = max_h;
    int L = 1 + 4 * n + 2 * m;

    // 初始化画布为 '.'
    vector<string> canvas(K, string(L, '.'));

    // 绘制每个格子
    for (int i = 0; i < m; ++i) {
        for (int j = 0; j < n; ++j) {
            int h = a[i][j];
            // 计算左下角坐标
            int x = 2 * (m - 1 - i);
            int y = 4 * j + 2 * (m - 1 - i);
            // 从下往上绘制每个积木
            for (int k = 0; k < h; ++k) {
                int cur_x = x + 3 * k;
                int cur_y = y;
                // 绘制一个积木
                // 顶面
                canvas[cur_x][cur_y] = '+';
                canvas[cur_x][cur_y + 1] = '-';
                canvas[cur_x][cur_y + 2] = '-';
                canvas[cur_x][cur_y + 3] = '-';
                canvas[cur_x][cur_y + 4] = '+';
                // 前面
                canvas[cur_x + 1][cur_y] = '|';
                canvas[cur_x + 1][cur_y + 1] = ' ';
                canvas[cur_x + 1][cur_y + 2] = ' ';
                canvas[cur_x + 1][cur_y + 3] = ' ';
                canvas[cur_x + 1][cur_y + 4] = '|';
                canvas[cur_x + 1][cur_y + 5] = '/';
                // 侧面
                canvas[cur_x + 2][cur_y] = '|';
                canvas[cur_x + 2][cur_y + 1] = ' ';
                canvas[cur_x + 2][cur_y + 2] = ' ';
                canvas[cur_x + 2][cur_y + 3] = ' ';
                canvas[cur_x + 2][cur_y + 4] = '|';
                canvas[cur_x + 2][cur_y + 5] = ' ';
                canvas[cur_x + 2][cur_y + 6] = '+';
                // 底面
                canvas[cur_x + 3][cur_y] = '+';
                canvas[cur_x + 3][cur_y + 1] = '-';
                canvas[cur_x + 3][cur_y + 2] = '-';
                canvas[cur_x + 3][cur_y + 3] = '-';
                canvas[cur_x + 3][cur_y + 4] = '+';
                canvas[cur_x + 3][cur_y + 5] = ' ';
                canvas[cur_x + 3][cur_y + 6] = '|';
                // 右侧面
                canvas[cur_x + 4][cur_y + 1] = '/';
                canvas[cur_x + 4][cur_y + 2] = ' ';
                canvas[cur_x + 4][cur_y + 3] = ' ';
                canvas[cur_x + 4][cur_y + 4] = ' ';
                canvas[cur_x + 4][cur_y + 5] = '|';
                canvas[cur_x + 4][cur_y + 6] = '+';
                // 最后一行
                canvas[cur_x + 5][cur_y + 2] = '+';
                canvas[cur_x + 5][cur_y + 3] = '-';
                canvas[cur_x + 5][cur_y + 4] = '-';
                canvas[cur_x + 5][cur_y + 5] = '-';
                canvas[cur_x + 5][cur_y + 6] = '+';
            }
        }
    }

    // 输出结果
    for (int i = K - 1; i >= 0; --i) {
        cout << canvas[i] << endl;
    }

    return 0;
}