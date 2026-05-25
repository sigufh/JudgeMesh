// ce.cpp
// 编译错误：缺少分号
#include <iostream>
int main() {
    std::cout << "Hello"  // 故意少写分号
    return 0;
}
