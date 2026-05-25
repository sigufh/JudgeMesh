#include <iostream>
#include <set>
#include <vector>
#include <algorithm>

int main() {
    int N;
    std::cin >> N;
    std::set<int> unique_numbers;
    for (int i = 0; i < N; ++i) {
        int num;
        std::cin >> num;
        unique_numbers.insert(num);
    }
    std::vector<int> sorted_numbers(unique_numbers.begin(), unique_numbers.end());
    std::sort(sorted_numbers.begin(), sorted_numbers.end());
    std::cout << sorted_numbers.size() << std::endl;
    for (size_t i = 0; i < sorted_numbers.size(); ++i) {
        if (i > 0) std::cout << " ";
        std::cout << sorted_numbers[i];
    }
    std::cout << std::endl;
    return 0;
}