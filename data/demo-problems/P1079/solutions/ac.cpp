#include <iostream>
#include <string>
#include <cctype>
using namespace std;

int main() {
    string key, cipher;
    cin >> key >> cipher;
    string plain;
    int keyLen = key.length();
    for (int i = 0; i < cipher.length(); ++i) {
        char c = cipher[i];
        char k = key[i % keyLen];
        bool isUpper = isupper(c);
        char cLower = tolower(c);
        char kLower = tolower(k);
        int shift = kLower - 'a';
        int pLower = (cLower - 'a' - shift + 26) % 26 + 'a';
        char p = isUpper ? toupper(pLower) : pLower;
        plain.push_back(p);
    }
    cout << plain << endl;
    return 0;
}