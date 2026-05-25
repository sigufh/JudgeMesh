#include <iostream>
#include <vector>
#include <algorithm>
#include <string>

using namespace std;

struct Student {
    string name;
    int year, month, day;
    int index; // 记录输入顺序
};

bool compare(const Student &a, const Student &b) {
    // 年龄大的在前，即出生日期早的在前
    if (a.year != b.year) return a.year < b.year;
    if (a.month != b.month) return a.month < b.month;
    if (a.day != b.day) return a.day < b.day;
    // 如果生日相同，输入靠后的先输出，即index大的在前
    return a.index > b.index;
}

int main() {
    int n;
    cin >> n;
    vector<Student> students(n);
    for (int i = 0; i < n; ++i) {
        cin >> students[i].name >> students[i].year >> students[i].month >> students[i].day;
        students[i].index = i;
    }
    sort(students.begin(), students.end(), compare);
    for (const auto &s : students) {
        cout << s.name << endl;
    }
    return 0;
}