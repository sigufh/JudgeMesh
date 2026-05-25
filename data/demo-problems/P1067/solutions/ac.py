n = int(input())
coeff = list(map(int, input().split()))

first = True
for i in range(n + 1):
    exp = n - i
    a = coeff[i]
    if a == 0:
        continue
    
    if first:
        if a < 0:
            print("-", end="")
        first = False
    else:
        if a > 0:
            print("+", end="")
        else:
            print("-", end="")
    
    abs_a = abs(a)
    if exp == 0:
        print(abs_a, end="")
    elif exp == 1:
        if abs_a != 1:
            print(abs_a, end="")
        print("x", end="")
    else:
        if abs_a != 1:
            print(abs_a, end="")
        print(f"x^{exp}", end="")

if first:
    print("0")
else:
    print()