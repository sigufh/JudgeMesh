#include <iostream>
using namespace std;

int main() {
    int apples[10];
    for (int i = 0; i < 10; i++) {
        cin >> apples[i];
    }
    int height;
    cin >> height;
    int reach = height + 30; // 加上板凳高度
    int count = 0;
    for (int i = 0; i < 10; i++) {
        if (apples[i] <= reach) {
            count++;
        }
    }
    cout << count << endl;
    return 0;
}