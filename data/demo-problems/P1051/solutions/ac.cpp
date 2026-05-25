#include <iostream>
#include <string>
using namespace std;

int main() {
    int N;
    cin >> N;
    
    string best_name;
    int best_money = -1;
    int total_money = 0;
    
    for (int i = 0; i < N; ++i) {
        string name;
        int avg_score, class_score, papers;
        char is_leader, is_west;
        cin >> name >> avg_score >> class_score >> is_leader >> is_west >> papers;
        
        int money = 0;
        if (avg_score > 80 && papers >= 1) money += 8000;
        if (avg_score > 85 && class_score > 80) money += 4000;
        if (avg_score > 90) money += 2000;
        if (avg_score > 85 && is_west == 'Y') money += 1000;
        if (class_score > 80 && is_leader == 'Y') money += 850;
        
        total_money += money;
        if (money > best_money) {
            best_money = money;
            best_name = name;
        }
    }
    
    cout << best_name << endl;
    cout << best_money << endl;
    cout << total_money << endl;
    
    return 0;
}