import sys
import bisect

def main():
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    N = int(data[0])
    C = int(data[1])
    a = list(map(int, data[2:2+N]))
    a.sort()
    ans = 0
    for B in a:
        A = B + C
        left = bisect.bisect_left(a, A)
        right = bisect.bisect_right(a, A)
        ans += (right - left)
    print(ans)

if __name__ == "__main__":
    main()