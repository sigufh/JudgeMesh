#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

struct Student {
    int id;
    int score;
};

bool cmp(const Student &a, const Student &b) {
    if (a.score != b.score) return a.score > b.score;
    return a.id < b.id;
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<Student> students(n);
    for (int i = 0; i < n; ++i) {
        cin >> students[i].id >> students[i].score;
    }
    
    sort(students.begin(), students.end(), cmp);
    
    int interview_count = (int)(m * 1.5);
    int score_line = students[interview_count - 1].score;
    
    int actual_count = interview_count;
    while (actual_count < n && students[actual_count].score == score_line) {
        actual_count++;
    }
    
    cout << score_line << " " << actual_count << endl;
    for (int i = 0; i < actual_count; ++i) {
        cout << students[i].id << " " << students[i].score << endl;
    }
    
    return 0;
}