import sys
import math

def main():
    n = int(sys.stdin.readline().strip())
    p = 0
    for i in range(2, int(math.isqrt(n)) + 1):
        if n % i == 0:
            p = n // i
            break
    print(p)

if __name__ == "__main__":
    main()