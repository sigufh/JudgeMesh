#include <iostream>
#include <cmath>
using namespace std;

int main() {
    int N;
    cin >> N;
    
    // 找到第N项所在的对角线编号k
    // 第k条对角线有k个元素，前k条对角线共有k*(k+1)/2个元素
    int k = (int)ceil((sqrt(1 + 8.0 * N) - 1) / 2);
    
    // 前k-1条对角线的元素总数
    int prev = (k - 1) * k / 2;
    
    // 在当前对角线上的位置（从1开始）
    int pos = N - prev;
    
    int numerator, denominator;
    if (k % 2 == 1) {
        // 奇数对角线：从下往上，分子递减，分母递增
        numerator = k - pos + 1;
        denominator = pos;
    } else {
        // 偶数对角线：从上往下，分子递增，分母递减
        numerator = pos;
        denominator = k - pos + 1;
    }
    
    cout << numerator << "/" << denominator << endl;
    
    return 0;
}