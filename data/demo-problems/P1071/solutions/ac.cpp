#include <iostream>
#include <string>
#include <map>
#include <set>

using namespace std;

int main() {
    string encrypted, original, toDecrypt;
    cin >> encrypted >> original >> toDecrypt;

    map<char, char> encryptMap; // 原文字母 -> 密字
    map<char, char> decryptMap; // 密字 -> 原文字母
    set<char> usedCipher;       // 已经使用的密字

    bool failed = false;

    for (size_t i = 0; i < encrypted.size(); ++i) {
        char e = encrypted[i];
        char o = original[i];

        if (encryptMap.count(o)) {
            if (encryptMap[o] != e) {
                failed = true;
                break;
            }
        } else {
            if (usedCipher.count(e)) {
                failed = true;
                break;
            }
            encryptMap[o] = e;
            decryptMap[e] = o;
            usedCipher.insert(e);
        }
    }

    if (!failed) {
        // 检查是否所有26个字母都出现了
        if (encryptMap.size() != 26) {
            failed = true;
        }
    }

    if (failed) {
        cout << "Failed" << endl;
    } else {
        string result;
        for (char c : toDecrypt) {
            result += decryptMap[c];
        }
        cout << result << endl;
    }

    return 0;
}