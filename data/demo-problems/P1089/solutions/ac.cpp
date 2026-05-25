#include <iostream>
using namespace std;

int main() {
    int budget[12];
    for (int i = 0; i < 12; i++) {
        cin >> budget[i];
    }
    
    int cash = 0;      // 津津手中的现金
    int saved = 0;     // 存在妈妈那里的钱（整百的总和）
    
    for (int month = 0; month < 12; month++) {
        cash += 300;   // 月初妈妈给钱
        if (cash < budget[month]) {
            // 钱不够用
            cout << -(month + 1) << endl;
            return 0;
        }
        cash -= budget[month];  // 扣除预算
        // 存整百的钱
        int deposit = (cash / 100) * 100;
        saved += deposit;
        cash -= deposit;
    }
    
    // 年末，妈妈还钱，加上20%
    int total = cash + saved + saved * 20 / 100;
    cout << total << endl;
    
    return 0;
}