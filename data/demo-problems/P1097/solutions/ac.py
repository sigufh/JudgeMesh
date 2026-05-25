import sys
from collections import Counter

def main():
    data = sys.stdin.read().split()
    if not data:
        return
    n = int(data[0])
    nums = list(map(int, data[1:1+n]))
    freq = Counter(nums)
    for num in sorted(freq):
        print(num, freq[num])

if __name__ == "__main__":
    main()