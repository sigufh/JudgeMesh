#include <iostream>
#include <string>
using namespace std;

void preorder(string in, string post) {
    if (in.empty()) return;
    char root = post.back();
    cout << root;
    int pos = in.find(root);
    string left_in = in.substr(0, pos);
    string right_in = in.substr(pos + 1);
    string left_post = post.substr(0, left_in.size());
    string right_post = post.substr(left_in.size(), right_in.size());
    preorder(left_in, left_post);
    preorder(right_in, right_post);
}

int main() {
    string inorder, postorder;
    cin >> inorder >> postorder;
    preorder(inorder, postorder);
    cout << endl;
    return 0;
}