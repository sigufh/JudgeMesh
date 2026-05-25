import sys

def f(x):
    return a * x**3 + b * x**2 + c * x + d

def find_root(l, r):
    while r - l > 1e-4:
        mid = (l + r) / 2
        if f(mid) == 0:
            return mid
        if f(l) * f(mid) < 0:
            r = mid
        else:
            l = mid
    return (l + r) / 2

def main():
    global a, b, c, d
    a, b, c, d = map(float, sys.stdin.readline().split())
    roots = []
    for i in range(-100, 101):
        l = i
        r = i + 1.0
        if f(l) == 0:
            roots.append(l)
        elif f(l) * f(r) < 0:
            roots.append(find_root(l, r))
        if len(roots) == 3:
            break
    print(" ".join(f"{root:.2f}" for root in roots))

if __name__ == "__main__":
    main()