#include <iostream>
using namespace std;

int main() {
    int max_unhappy = 0; // 最大不高兴程度
    int day = 0;         // 最不高兴的那天
    for (int i = 1; i <= 7; i++) {
        int school, extra;
        cin >> school >> extra;
        int total = school + extra;
        if (total > 8 && total > max_unhappy) {
            max_unhappy = total;
            day = i;
        }
    }
    cout << day << endl;
    return 0;
}