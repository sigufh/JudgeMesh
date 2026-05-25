#include <iostream>
#include <string>
#include <vector>
#include <cmath>

using namespace std;

int main() {
    string input;
    string all;
    while (getline(cin, input)) {
        all += input;
    }
    
    // 找到E的位置，忽略之后的内容
    size_t epos = all.find('E');
    if (epos != string::npos) {
        all = all.substr(0, epos);
    }
    
    // 11分制
    int w = 0, l = 0;
    vector<pair<int, int>> res11;
    for (char c : all) {
        if (c == 'W') w++;
        else if (c == 'L') l++;
        if ((w >= 11 || l >= 11) && abs(w - l) >= 2) {
            res11.push_back({w, l});
            w = 0; l = 0;
        }
    }
    res11.push_back({w, l}); // 最后一局未结束的
    
    // 21分制
    w = 0; l = 0;
    vector<pair<int, int>> res21;
    for (char c : all) {
        if (c == 'W') w++;
        else if (c == 'L') l++;
        if ((w >= 21 || l >= 21) && abs(w - l) >= 2) {
            res21.push_back({w, l});
            w = 0; l = 0;
        }
    }
    res21.push_back({w, l});
    
    // 输出11分制
    for (auto& p : res11) {
        cout << p.first << ":" << p.second << endl;
    }
    cout << endl;
    // 输出21分制
    for (auto& p : res21) {
        cout << p.first << ":" << p.second << endl;
    }
    
    return 0;
}