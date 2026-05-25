#include <bits/stdc++.h>
using namespace std;

int main() {
    int expected_price;
    cin >> expected_price;
    
    int cost, cost_sales;
    cin >> cost >> cost_sales;
    
    vector<pair<int, int>> points;
    points.push_back({cost, cost_sales});
    
    int price, sales;
    while (cin >> price >> sales) {
        if (price == -1 && sales == -1) break;
        points.push_back({price, sales});
    }
    
    int decrease;
    cin >> decrease;
    
    // 扩展价格范围到足够大，确保覆盖所有可能的最优价格
    int max_price = points.back().first;
    int max_sales = points.back().second;
    
    // 生成所有价格对应的销量
    map<int, int> sales_map;
    for (int i = 0; i < points.size() - 1; i++) {
        int p1 = points[i].first, s1 = points[i].second;
        int p2 = points[i+1].first, s2 = points[i+1].second;
        for (int p = p1; p <= p2; p++) {
            if (p1 == p2) sales_map[p] = s1;
            else {
                sales_map[p] = s1 + (s2 - s1) * (p - p1) / (p2 - p1);
            }
        }
    }
    
    // 添加最高价之后的销量
    for (int p = max_price + 1; ; p++) {
        int s = max_sales - decrease * (p - max_price);
        if (s <= 0) break;
        sales_map[p] = s;
    }
    
    // 收集所有可能的价格
    vector<int> prices;
    for (auto& kv : sales_map) {
        prices.push_back(kv.first);
    }
    
    // 尝试不同的税收/补贴
    int best_abs = INT_MAX;
    int best_val = 0;
    
    // 税收/补贴的范围，可以适当扩大
    for (int subsidy = -100000; subsidy <= 100000; subsidy++) {
        // 计算每个价格的总利润
        double max_profit = -1e18;
        int best_price = -1;
        
        for (int p : prices) {
            int s = sales_map[p];
            double profit = (p - cost + subsidy) * (double)s;
            if (profit > max_profit) {
                max_profit = profit;
                best_price = p;
            }
        }
        
        // 检查是否在预期价格取得最大利润
        double profit_at_expected = (expected_price - cost + subsidy) * (double)sales_map[expected_price];
        if (abs(profit_at_expected - max_profit) < 1e-9) {
            // 检查是否唯一最大，或者预期价格是其中之一
            // 需要确保预期价格是最大利润价格之一
            bool is_max = false;
            for (int p : prices) {
                double profit = (p - cost + subsidy) * (double)sales_map[p];
                if (abs(profit - max_profit) < 1e-9 && p == expected_price) {
                    is_max = true;
                    break;
                }
            }
            if (is_max) {
                if (abs(subsidy) < best_abs) {
                    best_abs = abs(subsidy);
                    best_val = subsidy;
                } else if (abs(subsidy) == best_abs && subsidy > best_val) {
                    best_val = subsidy;
                }
            }
        }
    }
    
    if (best_abs == INT_MAX) {
        cout << "NO SOLUTION" << endl;
    } else {
        cout << best_val << endl;
    }
    
    return 0;
}