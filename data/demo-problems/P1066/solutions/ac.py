import sys

def add(a, b):
    res = []
    carry = 0
    max_len = max(len(a), len(b))
    for i in range(max_len):
        da = a[i] if i < len(a) else 0
        db = b[i] if i < len(b) else 0
        s = da + db + carry
        res.append(s % 10)
        carry = s // 10
    if carry:
        res.append(carry)
    return res

def sub(a, b):
    res = []
    borrow = 0
    for i in range(len(a)):
        da = a[i]
        db = b[i] if i < len(b) else 0
        diff = da - db - borrow
        if diff < 0:
            diff += 10
            borrow = 1
        else:
            borrow = 0
        res.append(diff)
    while len(res) > 1 and res[-1] == 0:
        res.pop()
    return res

def ge(a, b):
    if len(a) != len(b):
        return len(a) > len(b)
    for i in range(len(a)-1, -1, -1):
        if a[i] != b[i]:
            return a[i] > b[i]
    return True

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    k = int(data[0])
    w = int(data[1])

    max_digit = (1 << k) - 1
    max_len = (w + k - 1) // k

    # 高精度组合数
    C = [[[0] for _ in range(max_len + 1)] for _ in range(max_digit + 1)]
    for i in range(max_digit + 1):
        C[i][0] = [1]
        limit = min(i, max_len)
        for j in range(1, limit + 1):
            C[i][j] = add(C[i-1][j-1], C[i-1][j])

    ans = [0]
    first_len = w % k

    if first_len == 0:
        # 所有段都是完整 k 位
        for length in range(2, max_len + 1):
            if length <= max_digit:
                ans = add(ans, C[max_digit][length])
    else:
        # 有不足段
        # 完整段：段数从 2 到 max_len-1
        for length in range(2, max_len):
            if length <= max_digit:
                ans = add(ans, C[max_digit][length])
        # 不足段：段数为 max_len，最高位受限
        max_first = (1 << first_len) - 1
        for i in range(1, max_first + 1):
            n = max_digit - i
            m = max_len - 1
            if n >= m:
                ans = add(ans, C[n][m])

    # 输出
    sys.stdout.write(''.join(map(str, reversed(ans))) + '\n')

if __name__ == "__main__":
    main()