#include <iostream>
#include <string>
#include <vector>
#include <map>
#include <set>
#include <algorithm>
#include <sstream>
using namespace std;

int M, N, P;
vector<string> names;
map<string, int> nameToId;
vector<vector<string>> testimonies; // 每个人的证词列表
vector<string> days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

// 判断证词在给定假设下是否为真
bool isTrue(const string &testimony, int guiltyId, int day, const vector<int> &isLiar) {
    // 解析证词
    string speaker, content;
    size_t pos = testimony.find(": ");
    speaker = testimony.substr(0, pos);
    content = testimony.substr(pos + 2);
    int speakerId = nameToId[speaker];
    
    // 如果说话者是撒谎者，则证词为假，否则为真
    bool speakerIsLiar = isLiar[speakerId];
    
    // 判断证词本身的真假
    bool statementTruth;
    if (content == "I am guilty.") {
        statementTruth = (speakerId == guiltyId);
    } else if (content == "I am not guilty.") {
        statementTruth = (speakerId != guiltyId);
    } else if (content.find(" is guilty.") != string::npos) {
        string name = content.substr(0, content.find(" is guilty."));
        if (nameToId.find(name) == nameToId.end()) return false; // 无效名字
        int id = nameToId[name];
        statementTruth = (id == guiltyId);
    } else if (content.find(" is not guilty.") != string::npos) {
        string name = content.substr(0, content.find(" is not guilty."));
        if (nameToId.find(name) == nameToId.end()) return false;
        int id = nameToId[name];
        statementTruth = (id != guiltyId);
    } else if (content.find("Today is ") == 0) {
        string dayName = content.substr(9);
        // 去掉末尾的点
        if (dayName.back() == '.') dayName.pop_back();
        auto it = find(days.begin(), days.end(), dayName);
        if (it == days.end()) return false; // 无效星期
        int mentionedDay = it - days.begin();
        statementTruth = (mentionedDay == day);
    } else {
        return false; // 无效证词
    }
    
    // 如果说话者是说谎者，则证词真假取反
    return speakerIsLiar ? !statementTruth : statementTruth;
}

// 检查给定假设是否一致
bool check(int guiltyId, int day, const vector<int> &isLiar) {
    for (int i = 0; i < M; i++) {
        for (const string &t : testimonies[i]) {
            if (!isTrue(t, guiltyId, day, isLiar)) {
                return false;
            }
        }
    }
    return true;
}

int main() {
    cin >> M >> N >> P;
    names.resize(M);
    for (int i = 0; i < M; i++) {
        cin >> names[i];
        nameToId[names[i]] = i;
    }
    cin.ignore(); // 忽略换行
    testimonies.resize(M);
    for (int i = 0; i < P; i++) {
        string line;
        getline(cin, line);
        // 找到说话者
        size_t pos = line.find(": ");
        string speaker = line.substr(0, pos);
        int id = nameToId[speaker];
        testimonies[id].push_back(line);
    }
    
    vector<string> possibleGuilty;
    
    // 枚举罪犯
    for (int guilty = 0; guilty < M; guilty++) {
        // 枚举星期
        for (int day = 0; day < 7; day++) {
            // 枚举说谎者集合
            // 由于M<=20，可以用位掩码枚举所有子集
            for (int mask = 0; mask < (1 << M); mask++) {
                // 计算说谎者数量
                int liarCount = 0;
                vector<int> isLiar(M, 0);
                for (int i = 0; i < M; i++) {
                    if (mask & (1 << i)) {
                        isLiar[i] = 1;
                        liarCount++;
                    }
                }
                if (liarCount != N) continue;
                
                if (check(guilty, day, isLiar)) {
                    possibleGuilty.push_back(names[guilty]);
                    goto nextGuilty; // 找到一个即可，跳出循环
                }
            }
        }
        nextGuilty:;
    }
    
    // 去重
    sort(possibleGuilty.begin(), possibleGuilty.end());
    possibleGuilty.erase(unique(possibleGuilty.begin(), possibleGuilty.end()), possibleGuilty.end());
    
    if (possibleGuilty.empty()) {
        cout << "Impossible" << endl;
    } else if (possibleGuilty.size() > 1) {
        cout << "Cannot Determine" << endl;
    } else {
        cout << possibleGuilty[0] << endl;
    }
    
    return 0;
}