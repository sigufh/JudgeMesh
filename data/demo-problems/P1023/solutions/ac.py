import sys

def solve():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    expected_price = int(next(it))
    cost = int(next(it))
    cost_sales = int(next(it))
    
    points = [(cost, cost_sales)]
    while True:
        price = int(next(it))
        sales = int(next(it))
        if price == -1 and sales == -1:
            break
        points.append((price, sales))
    
    decrease = int(next(it))
    
    # 生成销量映射
    sales_map = {}
    for i in range(len(points) - 1):
        p1, s1 = points[i]
        p2, s2 = points[i+1]
        if p1 == p2:
            sales_map[p1] = s1
        else:
            for p in range(p1, p2 + 1):
                sales_map[p] = s1 + (s2 - s1) * (p - p1) // (p2 - p1)
    
    max_price = points[-1][0]
    max_sales = points[-1][1]
    
    # 扩展高价部分
    p = max_price + 1
    while True:
        s = max_sales - decrease * (p - max_price)
        if s <= 0:
            break
        sales_map[p] = s
        p += 1
    
    prices = sorted(sales_map.keys())
    
    best_abs = float('inf')
    best_val = 0
    
    # 尝试补贴/税收范围
    for subsidy in range(-100000, 100001):
        max_profit = -float('inf')
        best_price = -1
        for p in prices:
            s = sales_map[p]
            profit = (p - cost + subsidy) * s
            if profit > max_profit:
                max_profit = profit
                best_price = p
        
        # 检查预期价格是否达到最大利润
        profit_expected = (expected_price - cost + subsidy) * sales_map[expected_price]
        if abs(profit_expected - max_profit) < 1e-9:
            # 确认预期价格是最大利润价格之一
            is_max = False
            for p in prices:
                profit = (p - cost + subsidy) * sales_map[p]
                if abs(profit - max_profit) < 1e-9 and p == expected_price:
                    is_max = True
                    break
            if is_max:
                if abs(subsidy) < best_abs:
                    best_abs = abs(subsidy)
                    best_val = subsidy
                elif abs(subsidy) == best_abs and subsidy > best_val:
                    best_val = subsidy
    
    if best_abs == float('inf'):
        print("NO SOLUTION")
    else:
        print(best_val)

if __name__ == "__main__":
    solve()