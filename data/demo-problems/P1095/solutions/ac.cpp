#include <iostream>
#include <algorithm>
using namespace std;

int main() {
    int M, S, T;
    cin >> M >> S >> T;
    
    int max_dist = 0;
    int min_time = T + 1;
    
    // 枚举使用闪烁的次数
    for (int blink_count = 0; blink_count <= T; blink_count++) {
        int mana_needed = blink_count * 10;
        int rest_time = 0;
        if (mana_needed > M) {
            int mana_deficit = mana_needed - M;
            rest_time = (mana_deficit + 3) / 4; // 向上取整
        }
        int total_time = blink_count + rest_time;
        if (total_time > T) continue;
        
        int dist = blink_count * 60;
        int remain_time = T - total_time;
        dist += remain_time * 17;
        
        if (dist >= S) {
            // 计算实际需要的最短时间
            int needed_dist = S;
            int time_used = 0;
            int current_mana = M;
            int current_dist = 0;
            
            // 贪心模拟，优先使用闪烁
            while (current_dist < S && time_used < T) {
                if (current_mana >= 10) {
                    current_dist += 60;
                    current_mana -= 10;
                    time_used++;
                } else {
                    int mana_short = 10 - current_mana;
                    int rest_needed = (mana_short + 3) / 4;
                    if (time_used + rest_needed + 1 <= T) {
                        // 休息然后闪烁
                        time_used += rest_needed;
                        current_mana += rest_needed * 4;
                        current_dist += 60;
                        current_mana -= 10;
                        time_used++;
                    } else {
                        // 只能跑步
                        int run_time = T - time_used;
                        current_dist += run_time * 17;
                        time_used = T;
                        break;
                    }
                }
            }
            if (current_dist >= S) {
                min_time = min(min_time, time_used);
            }
        } else {
            max_dist = max(max_dist, dist);
        }
    }
    
    if (min_time <= T) {
        cout << "Yes" << endl;
        cout << min_time << endl;
    } else {
        cout << "No" << endl;
        cout << max_dist << endl;
    }
    
    return 0;
}