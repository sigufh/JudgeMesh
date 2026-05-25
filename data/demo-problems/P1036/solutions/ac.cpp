#include <iostream>
#include <vector>
#include <cmath>
using namespace std;

bool is_prime(int num) {
    if (num < 2) return false;
    for (int i = 2; i <= sqrt(num); ++i) {
        if (num % i == 0) return false;
    }
    return true;
}

int count_prime_sums(const vector<int>& nums, int k, int start, int depth, int current_sum) {
    if (depth == k) {
        return is_prime(current_sum) ? 1 : 0;
    }
    int count = 0;
    for (int i = start; i < nums.size(); ++i) {
        count += count_prime_sums(nums, k, i + 1, depth + 1, current_sum + nums[i]);
    }
    return count;
}

int main() {
    int n, k;
    cin >> n >> k;
    vector<int> nums(n);
    for (int i = 0; i < n; ++i) {
        cin >> nums[i];
    }
    cout << count_prime_sums(nums, k, 0, 0, 0) << endl;
    return 0;
}