#include <iostream>
#include <cmath>
using namespace std;

int main() {
    double H, S1, V, L, K, n;
    cin >> H >> S1 >> V >> L >> K >> n;
    
    double g = 10.0;
    double eps = 1e-4;
    
    // 小球下落时间
    double t1 = sqrt(2 * H / g);
    double t2 = sqrt(2 * (H - K) / g);
    
    // 小车在t1时刻的位置范围
    double car_left_t1 = S1 - V * t1;
    double car_right_t1 = S1 + L - V * t1;
    
    // 小车在t2时刻的位置范围
    double car_left_t2 = S1 - V * t2;
    double car_right_t2 = S1 + L - V * t2;
    
    // 小球能被接住的条件：小球初始位置x满足
    // 在t1时刻：car_left_t1 <= x <= car_right_t1
    // 在t2时刻：car_right_t2 >= x >= car_left_t2
    // 综合：x >= max(car_left_t1, car_left_t2) 且 x <= min(car_right_t1, car_right_t2)
    double left = max(car_left_t1, car_left_t2);
    double right = min(car_right_t1, car_right_t2);
    
    // 考虑eps
    left -= eps;
    right += eps;
    
    // 小球位置为0,1,2,...,n-1
    int start = ceil(left);
    int end = floor(right);
    
    if (start < 0) start = 0;
    if (end >= n) end = n - 1;
    
    int ans = 0;
    if (start <= end) ans = end - start + 1;
    
    cout << ans << endl;
    return 0;
}