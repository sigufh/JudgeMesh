def char_to_val(c):
    if '0' <= c <= '9':
        return ord(c) - ord('0')
    return ord(c) - ord('A') + 10

def val_to_char(v):
    if v < 10:
        return chr(ord('0') + v)
    return chr(ord('A') + v - 10)

def is_palindrome(s):
    return s == s[::-1]

def add_in_base(a, b, base):
    res = []
    carry = 0
    i, j = len(a) - 1, len(b) - 1
    while i >= 0 or j >= 0 or carry:
        total = carry
        if i >= 0:
            total += char_to_val(a[i])
            i -= 1
        if j >= 0:
            total += char_to_val(b[j])
            j -= 1
        carry = total // base
        res.append(val_to_char(total % base))
    return ''.join(reversed(res))

def main():
    N = int(input().strip())
    M = input().strip()
    for step in range(1, 31):
        rev = M[::-1]
        M = add_in_base(M, rev, N)
        if is_palindrome(M):
            print(f"STEP={step}")
            return
    print("Impossible!")

if __name__ == "__main__":
    main()