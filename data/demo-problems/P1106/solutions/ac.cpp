#include <iostream>
#include <string>
#include <algorithm>

using namespace std;

int main() {
    string n;
    int k;
    cin >> n >> k;
    
    string result;
    int to_remove = k;
    
    for (char digit : n) {
        while (!result.empty() && to_remove > 0 && result.back() > digit) {
            result.pop_back();
            to_remove--;
        }
        result.push_back(digit);
    }
    
    // 如果还有需要删除的数字，从末尾删除
    result.resize(result.size() - to_remove);
    
    // 去除前导零
    int start = 0;
    while (start < result.size() && result[start] == '0') {
        start++;
    }
    
    if (start == result.size()) {
        cout << "0" << endl;
    } else {
        cout << result.substr(start) << endl;
    }
    
    return 0;
}