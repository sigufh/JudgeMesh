def main():
    budgets = []
    for _ in range(12):
        budgets.append(int(input()))
    
    cash = 0      # 津津手中的现金
    saved = 0     # 存在妈妈那里的钱（整百的总和）
    
    for month in range(12):
        cash += 300   # 月初妈妈给钱
        if cash < budgets[month]:
            # 钱不够用
            print(-(month + 1))
            return
        cash -= budgets[month]  # 扣除预算
        # 存整百的钱
        deposit = (cash // 100) * 100
        saved += deposit
        cash -= deposit
    
    # 年末，妈妈还钱，加上20%
    total = cash + saved + saved * 20 // 100
    print(total)

if __name__ == "__main__":
    main()