def exgcd(a, b):
    if b == 0:
        return a, 1, 0
    d, x1, y1 = exgcd(b, a % b)
    x = y1
    y = x1 - (a // b) * y1
    return d, x, y

def main():
    a, b = map(int, input().split())
    d, x, y = exgcd(a, b)
    # 根据题意，d 一定为 1
    x = (x % b + b) % b
    print(x)

if __name__ == "__main__":
    main()