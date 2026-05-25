import math

def multiply(a, b):
    res = [0] * 500
    for i in range(500):
        if a[i] == 0:
            continue
        for j in range(500 - i):
            res[i + j] += a[i] * b[j]
            if res[i + j] >= 10:
                res[i + j + 1] += res[i + j] // 10
                res[i + j] %= 10
    return res

def power(p):
    base = [0] * 500
    base[0] = 2
    result = [0] * 500
    result[0] = 1
    while p > 0:
        if p & 1:
            result = multiply(result, base)
        base = multiply(base, base)
        p >>= 1
    return result

def main():
    P = int(input().strip())
    
    # 计算位数
    digits = int(P * math.log10(2)) + 1
    print(digits)
    
    # 计算2^P - 1的最后500位
    num = power(P)
    # 减1
    num[0] -= 1
    for i in range(500):
        if num[i] < 0:
            num[i] += 10
            num[i + 1] -= 1
        else:
            break
    
    # 输出最后500位，每行50位，高位补0
    for i in range(499, -1, -1):
        print(num[i], end='')
        if i % 50 == 0:
            print()

if __name__ == "__main__":
    main()