#include <iostream>
#include <string>
#include <queue>
#include <unordered_map>
#include <vector>
using namespace std;

int main() {
    string A, B;
    cin >> A >> B;
    vector<pair<string, string>> rules;
    string a, b;
    while (cin >> a >> b) {
        rules.push_back({a, b});
    }

    if (A == B) {
        cout << 0 << endl;
        return 0;
    }

    queue<pair<string, int>> q;
    unordered_map<string, int> dist;
    q.push({A, 0});
    dist[A] = 0;

    while (!q.empty()) {
        auto [cur, steps] = q.front();
        q.pop();
        if (steps >= 10) continue;
        for (auto& rule : rules) {
            string from = rule.first;
            string to = rule.second;
            size_t pos = 0;
            while ((pos = cur.find(from, pos)) != string::npos) {
                string next = cur;
                next.replace(pos, from.length(), to);
                if (next == B) {
                    cout << steps + 1 << endl;
                    return 0;
                }
                if (!dist.count(next) || dist[next] > steps + 1) {
                    dist[next] = steps + 1;
                    q.push({next, steps + 1});
                }
                pos += 1; // 移动一位，允许重叠匹配
            }
        }
    }

    cout << "NO ANSWER!" << endl;
    return 0;
}