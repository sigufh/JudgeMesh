import sys

def solve():
    M, S, T = map(int, sys.stdin.readline().split())
    
    max_dist = 0
    min_time = T + 1
    
    # 枚举使用闪烁的次数
    for blink_count in range(T + 1):
        mana_needed = blink_count * 10
        rest_time = 0
        if mana_needed > M:
            mana_deficit = mana_needed - M
            rest_time = (mana_deficit + 3) // 4  # 向上取整
        total_time = blink_count + rest_time
        if total_time > T:
            continue
        
        dist = blink_count * 60
        remain_time = T - total_time
        dist += remain_time * 17
        
        if dist >= S:
            # 计算实际需要的最短时间
            needed_dist = S
            time_used = 0
            current_mana = M
            current_dist = 0
            
            # 贪心模拟，优先使用闪烁
            while current_dist < S and time_used < T:
                if current_mana >= 10:
                    current_dist += 60
                    current_mana -= 10
                    time_used += 1
                else:
                    mana_short = 10 - current_mana
                    rest_needed = (mana_short + 3) // 4
                    if time_used + rest_needed + 1 <= T:
                        # 休息然后闪烁
                        time_used += rest_needed
                        current_mana += rest_needed * 4
                        current_dist += 60
                        current_mana -= 10
                        time_used += 1
                    else:
                        # 只能跑步
                        run_time = T - time_used
                        current_dist += run_time * 17
                        time_used = T
                        break
            if current_dist >= S:
                min_time = min(min_time, time_used)
        else:
            max_dist = max(max_dist, dist)
    
    if min_time <= T:
        print("Yes")
        print(min_time)
    else:
        print("No")
        print(max_dist)

if __name__ == "__main__":
    solve()