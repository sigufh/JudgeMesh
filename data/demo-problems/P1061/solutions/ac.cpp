#include <iostream>
#include <string>
#include <algorithm>

using namespace std;

int main() {
    int s, t, w;
    cin >> s >> t >> w;
    string jam;
    cin >> jam;

    // 将字母序号转换为字符
    char min_char = 'a' + s - 1;
    char max_char = 'a' + t - 1;

    int count = 0;
    while (count < 5) {
        // 从右向左找到第一个可以增加的位
        int pos = w - 1;
        while (pos >= 0 && jam[pos] == max_char - (w - 1 - pos)) {
            pos--;
        }
        if (pos < 0) break; // 没有下一个了

        // 当前位增加1
        jam[pos]++;
        // 后面的位依次递增
        for (int i = pos + 1; i < w; i++) {
            jam[i] = jam[i - 1] + 1;
        }
        cout << jam << endl;
        count++;
    }

    return 0;
}