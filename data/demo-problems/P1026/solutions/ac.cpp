#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <cstring>
using namespace std;

int main() {
    int p, k;
    cin >> p >> k;
    string str;
    for (int i = 0; i < p; ++i) {
        string line;
        cin >> line;
        str += line;
    }
    int s;
    cin >> s;
    vector<string> dict(s);
    for (int i = 0; i < s; ++i) {
        cin >> dict[i];
    }

    int n = str.length();
    // dp[i][j] 表示前i个字符分成j份的最大单词数
    vector<vector<int>> dp(n + 1, vector<int>(k + 1, -1e9));
    dp[0][0] = 0;

    // 预处理 sum[i][j] 表示从i到j的子串包含的单词数（单词可以重叠，但每个单词的第一个字母不能再用）
    // 这里采用贪心匹配：对于每个位置i，找以i开头的最短单词，如果匹配则计数+1，然后跳到该单词结尾的下一个位置
    // 但题目要求“选用一个单词之后，其第一个字母不能再用”，意味着每个位置只能作为一个单词的开头一次
    // 所以对于子串，我们只需统计有多少个位置可以作为某个单词的开头，且这些单词完全在子串内
    // 由于单词长度不超过子串长度，我们可以直接检查每个位置
    vector<vector<int>> sum(n, vector<int>(n, 0));
    for (int i = 0; i < n; ++i) {
        for (int j = i; j < n; ++j) {
            int cnt = 0;
            // 在子串[i,j]中，从每个位置l开始检查是否能匹配某个单词
            for (int l = i; l <= j; ++l) {
                for (const string& word : dict) {
                    int len = word.length();
                    if (l + len - 1 <= j && str.substr(l, len) == word) {
                        cnt++;
                        break; // 每个位置最多作为一个单词的开头，找到一个就跳出
                    }
                }
            }
            sum[i][j] = cnt;
        }
    }

    // DP
    for (int i = 1; i <= n; ++i) {
        for (int j = 1; j <= min(i, k); ++j) {
            // 枚举最后一份的起始位置
            for (int l = j - 1; l < i; ++l) {
                // 前l个字符分成j-1份，最后一份是[l, i-1]
                if (dp[l][j - 1] >= 0) {
                    dp[i][j] = max(dp[i][j], dp[l][j - 1] + sum[l][i - 1]);
                }
            }
        }
    }

    cout << dp[n][k] << endl;
    return 0;
}