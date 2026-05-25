import sys

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    N = int(data[0])
    idx = 1
    best_name = ""
    best_money = -1
    total_money = 0
    
    for _ in range(N):
        name = data[idx]; idx += 1
        avg_score = int(data[idx]); idx += 1
        class_score = int(data[idx]); idx += 1
        is_leader = data[idx]; idx += 1
        is_west = data[idx]; idx += 1
        papers = int(data[idx]); idx += 1
        
        money = 0
        if avg_score > 80 and papers >= 1:
            money += 8000
        if avg_score > 85 and class_score > 80:
            money += 4000
        if avg_score > 90:
            money += 2000
        if avg_score > 85 and is_west == 'Y':
            money += 1000
        if class_score > 80 and is_leader == 'Y':
            money += 850
        
        total_money += money
        if money > best_money:
            best_money = money
            best_name = name
    
    print(best_name)
    print(best_money)
    print(total_money)

if __name__ == "__main__":
    main()