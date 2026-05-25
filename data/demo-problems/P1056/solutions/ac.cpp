#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    int M, N, K, L, D;
    cin >> M >> N >> K >> L >> D;
    
    vector<int> row_count(M, 0);  // row_count[i] 表示第i行和第i+1行之间隔开的交头接耳对数
    vector<int> col_count(N, 0);  // col_count[j] 表示第j列和第j+1列之间隔开的交头接耳对数
    
    for (int i = 0; i < D; i++) {
        int x1, y1, x2, y2;
        cin >> x1 >> y1 >> x2 >> y2;
        if (x1 == x2) {
            // 左右相邻，纵向通道
            int col = min(y1, y2);
            col_count[col]++;
        } else if (y1 == y2) {
            // 前后相邻，横向通道
            int row = min(x1, x2);
            row_count[row]++;
        }
    }
    
    // 选择K个横向通道
    vector<pair<int, int>> row_candidates;
    for (int i = 1; i < M; i++) {
        row_candidates.push_back({row_count[i], i});
    }
    sort(row_candidates.begin(), row_candidates.end(), [](const pair<int,int>& a, const pair<int,int>& b) {
        if (a.first != b.first) return a.first > b.first;
        return a.second < b.second;
    });
    vector<int> row_selected;
    for (int i = 0; i < K; i++) {
        row_selected.push_back(row_candidates[i].second);
    }
    sort(row_selected.begin(), row_selected.end());
    
    // 选择L个纵向通道
    vector<pair<int, int>> col_candidates;
    for (int j = 1; j < N; j++) {
        col_candidates.push_back({col_count[j], j});
    }
    sort(col_candidates.begin(), col_candidates.end(), [](const pair<int,int>& a, const pair<int,int>& b) {
        if (a.first != b.first) return a.first > b.first;
        return a.second < b.second;
    });
    vector<int> col_selected;
    for (int i = 0; i < L; i++) {
        col_selected.push_back(col_candidates[i].second);
    }
    sort(col_selected.begin(), col_selected.end());
    
    // 输出
    for (int i = 0; i < K; i++) {
        if (i > 0) cout << " ";
        cout << row_selected[i];
    }
    cout << endl;
    for (int i = 0; i < L; i++) {
        if (i > 0) cout << " ";
        cout << col_selected[i];
    }
    cout << endl;
    
    return 0;
}