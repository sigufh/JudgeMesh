import math

def main():
    x0, y0 = map(int, input().split())
    if y0 % x0 != 0:
        print(0)
        return
    
    n = y0 // x0
    count = 0
    
    for i in range(1, int(math.isqrt(n)) + 1):
        if n % i == 0:
            a = i
            b = n // i
            if math.gcd(a, b) == 1:
                if a == b:
                    count += 1
                else:
                    count += 2
                    
    print(count)

if __name__ == "__main__":
    main()