#include <iostream>
#include <vector>
#include <string>
using namespace std;

const string target = "yizhong";
const int dx[8] = {0, 0, 1, -1, 1, -1, 1, -1};
const int dy[8] = {1, -1, 0, 0, 1, -1, -1, 1};

int main() {
    int n;
    cin >> n;
    vector<string> grid(n);
    for (int i = 0; i < n; ++i) {
        cin >> grid[i];
    }

    vector<vector<bool>> mark(n, vector<bool>(n, false));

    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            if (grid[i][j] != 'y') continue;
            for (int dir = 0; dir < 8; ++dir) {
                int x = i, y = j;
                bool ok = true;
                for (int k = 0; k < 7; ++k) {
                    if (x < 0 || x >= n || y < 0 || y >= n || grid[x][y] != target[k]) {
                        ok = false;
                        break;
                    }
                    x += dx[dir];
                    y += dy[dir];
                }
                if (ok) {
                    x = i; y = j;
                    for (int k = 0; k < 7; ++k) {
                        mark[x][y] = true;
                        x += dx[dir];
                        y += dy[dir];
                    }
                }
            }
        }
    }

    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            if (mark[i][j]) cout << grid[i][j];
            else cout << '*';
        }
        cout << endl;
    }

    return 0;
}