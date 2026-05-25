def main():
    import sys
    data = sys.stdin.read().strip().split()
    if not data:
        return
    w = int(data[0])
    n = int(data[1])
    prices = list(map(int, data[2:2+n]))
    prices.sort()
    left = 0
    right = n - 1
    groups = 0
    while left <= right:
        if prices[left] + prices[right] <= w:
            left += 1
        right -= 1
        groups += 1
    print(groups)

if __name__ == "__main__":
    main()