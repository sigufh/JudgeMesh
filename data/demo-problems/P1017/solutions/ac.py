def convert_to_negative_base(n, R):
    if n == 0:
        return "0"
    result = []
    while n != 0:
        remainder = n % R
        n //= R
        if remainder < 0:
            remainder -= R
            n += 1
        if remainder < 10:
            result.append(str(remainder))
        else:
            result.append(chr(ord('A') + remainder - 10))
    result.reverse()
    return ''.join(result)

def main():
    n, R = map(int, input().split())
    result = convert_to_negative_base(n, R)
    print(f"{result}={n}(base{R})")

if __name__ == "__main__":
    main()