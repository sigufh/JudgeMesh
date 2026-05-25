#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    int k, N;
    cin >> k >> N;
    vector<long long> seq;
    seq.push_back(1); // k^0
    long long power = k;
    while (seq.size() < N) {
        int sz = seq.size();
        seq.push_back(power);
        for (int i = 0; i < sz; ++i) {
            seq.push_back(seq[i] + power);
            if (seq.size() >= N) break;
        }
        power *= k;
    }
    sort(seq.begin(), seq.end());
    cout << seq[N-1] << endl;
    return 0;
}