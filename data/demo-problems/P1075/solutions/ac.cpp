#include <iostream>
#include <cmath>

int main() {
    long long n;
    std::cin >> n;
    long long p = 0;
    for (long long i = 2; i * i <= n; ++i) {
        if (n % i == 0) {
            p = n / i;
            break;
        }
    }
    std::cout << p << std::endl;
    return 0;
}