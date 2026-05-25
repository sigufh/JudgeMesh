#include <iostream>
using namespace std;

int main() {
    unsigned int n;
    cin >> n;
    unsigned int high = n >> 16;
    unsigned int low = n & 0xFFFF;
    unsigned int result = (low << 16) | high;
    cout << result << endl;
    return 0;
}