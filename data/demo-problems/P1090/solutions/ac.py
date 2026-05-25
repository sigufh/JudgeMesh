import sys
import heapq

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    n = int(data[0])
    a = list(map(int, data[1:1+n]))
    heapq.heapify(a)
    ans = 0
    while len(a) > 1:
        x = heapq.heappop(a)
        y = heapq.heappop(a)
        s = x + y
        ans += s
        heapq.heappush(a, s)
    print(ans)

if __name__ == "__main__":
    main()