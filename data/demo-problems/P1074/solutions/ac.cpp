#include <bits/stdc++.h>
using namespace std;

const int score[9][9] = {
    {6,6,6,6,6,6,6,6,6},
    {6,7,7,7,7,7,7,7,6},
    {6,7,8,8,8,8,8,7,6},
    {6,7,8,9,9,9,8,7,6},
    {6,7,8,9,10,9,8,7,6},
    {6,7,8,9,9,9,8,7,6},
    {6,7,8,8,8,8,8,7,6},
    {6,7,7,7,7,7,7,7,6},
    {6,6,6,6,6,6,6,6,6}
};

int a[9][9];
int row[9][10], col[9][10], block[3][3][10];
int ans = -1;

int getBlock(int i, int j) {
    return i / 3 * 3 + j / 3;
}

void dfs(int x, int y, int sum) {
    if (x == 9) {
        ans = max(ans, sum);
        return;
    }
    if (y == 9) {
        dfs(x + 1, 0, sum);
        return;
    }
    if (a[x][y] != 0) {
        dfs(x, y + 1, sum + a[x][y] * score[x][y]);
        return;
    }
    for (int num = 1; num <= 9; num++) {
        if (!row[x][num] && !col[y][num] && !block[x/3][y/3][num]) {
            row[x][num] = col[y][num] = block[x/3][y/3][num] = 1;
            dfs(x, y + 1, sum + num * score[x][y]);
            row[x][num] = col[y][num] = block[x/3][y/3][num] = 0;
        }
    }
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(0);
    memset(row, 0, sizeof(row));
    memset(col, 0, sizeof(col));
    memset(block, 0, sizeof(block));
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            cin >> a[i][j];
            if (a[i][j] != 0) {
                int num = a[i][j];
                row[i][num] = 1;
                col[j][num] = 1;
                block[i/3][j/3][num] = 1;
            }
        }
    }
    dfs(0, 0, 0);
    cout << ans << endl;
    return 0;
}