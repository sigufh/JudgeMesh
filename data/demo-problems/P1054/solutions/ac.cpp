#include <iostream>
#include <string>
#include <cctype>
#include <stack>
#include <vector>
#include <algorithm>
#include <cstdlib>
#include <cstring>
#include <cmath>
using namespace std;

// 去除空格
string trimSpaces(const string& s) {
    string res;
    for (char c : s) if (!isspace(c)) res += c;
    return res;
}

// 检查括号匹配
bool checkBrackets(const string& s) {
    int cnt = 0;
    for (char c : s) {
        if (c == '(') cnt++;
        else if (c == ')') {
            cnt--;
            if (cnt < 0) return false;
        }
    }
    return cnt == 0;
}

// 运算符优先级
int precedence(char op) {
    if (op == '+' || op == '-') return 1;
    if (op == '*') return 2;
    if (op == '^') return 3;
    return 0;
}

// 中缀转后缀
vector<string> infixToPostfix(const string& s) {
    vector<string> output;
    stack<char> ops;
    int i = 0;
    while (i < s.size()) {
        if (isdigit(s[i])) {
            string num;
            while (i < s.size() && isdigit(s[i])) num += s[i++];
            output.push_back(num);
        } else if (s[i] == 'a') {
            output.push_back("a");
            i++;
        } else if (s[i] == '(') {
            ops.push('(');
            i++;
        } else if (s[i] == ')') {
            while (!ops.empty() && ops.top() != '(') {
                output.push_back(string(1, ops.top()));
                ops.pop();
            }
            if (!ops.empty()) ops.pop(); // pop '('
            i++;
        } else { // operator
            char op = s[i];
            while (!ops.empty() && ops.top() != '(' &&
                   ((op != '^' && precedence(ops.top()) >= precedence(op)) ||
                    (op == '^' && precedence(ops.top()) > precedence(op)))) {
                output.push_back(string(1, ops.top()));
                ops.pop();
            }
            ops.push(op);
            i++;
        }
    }
    while (!ops.empty()) {
        output.push_back(string(1, ops.top()));
        ops.pop();
    }
    return output;
}

// 计算后缀表达式，给定a的值
long long evalPostfix(const vector<string>& post, long long a_val) {
    stack<long long> st;
    for (const string& token : post) {
        if (token == "a") {
            st.push(a_val);
        } else if (isdigit(token[0])) {
            st.push(stoll(token));
        } else {
            long long b = st.top(); st.pop();
            long long a = st.top(); st.pop();
            if (token == "+") st.push(a + b);
            else if (token == "-") st.push(a - b);
            else if (token == "*") st.push(a * b);
            else if (token == "^") {
                long long res = 1;
                for (int i = 0; i < b; i++) res *= a;
                st.push(res);
            }
        }
    }
    return st.top();
}

// 判断两个表达式是否等价
bool equivalent(const string& e1, const string& e2) {
    string s1 = trimSpaces(e1);
    string s2 = trimSpaces(e2);
    if (!checkBrackets(s1) || !checkBrackets(s2)) return false;
    vector<string> post1 = infixToPostfix(s1);
    vector<string> post2 = infixToPostfix(s2);
    // 测试几个a值
    long long test_vals[] = {1, 2, 3, 5, 7, 10, 100};
    for (long long a_val : test_vals) {
        if (evalPostfix(post1, a_val) != evalPostfix(post2, a_val))
            return false;
    }
    return true;
}

int main() {
    string expr;
    getline(cin, expr);
    int n;
    cin >> n;
    cin.ignore(); // 忽略换行
    vector<string> options(n);
    for (int i = 0; i < n; i++) {
        getline(cin, options[i]);
    }
    string result;
    for (int i = 0; i < n; i++) {
        if (equivalent(expr, options[i])) {
            result += char('A' + i);
        }
    }
    cout << result << endl;
    return 0;
}