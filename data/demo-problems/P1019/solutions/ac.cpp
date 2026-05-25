#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
using namespace std;

int n;
vector<string> words;
vector<int> used;
int max_len = 0;

// 计算两个单词的重叠长度，返回重叠长度，如果无法连接返回0
int overlap(const string& a, const string& b) {
    int len_a = a.size();
    int len_b = b.size();
    // 重叠长度从1到min(len_a, len_b)-1，因为不能包含
    for (int i = 1; i < min(len_a, len_b); i++) {
        if (a.substr(len_a - i) == b.substr(0, i)) {
            return i;
        }
    }
    return 0;
}

void dfs(string current) {
    max_len = max(max_len, (int)current.size());
    for (int i = 0; i < n; i++) {
        if (used[i] >= 2) continue;
        int ov = overlap(current, words[i]);
        if (ov > 0) {
            used[i]++;
            dfs(current + words[i].substr(ov));
            used[i]--;
        }
    }
}

int main() {
    cin >> n;
    words.resize(n);
    used.resize(n, 0);
    for (int i = 0; i < n; i++) {
        cin >> words[i];
    }
    char start;
    cin >> start;
    for (int i = 0; i < n; i++) {
        if (words[i][0] == start) {
            used[i]++;
            dfs(words[i]);
            used[i]--;
        }
    }
    cout << max_len << endl;
    return 0;
}