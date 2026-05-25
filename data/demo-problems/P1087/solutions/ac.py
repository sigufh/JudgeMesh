def get_type(s: str) -> str:
    has0 = '0' in s
    has1 = '1' in s
    if has0 and has1:
        return 'F'
    if has0:
        return 'B'
    return 'I'

def postorder(s: str) -> str:
    if not s:
        return ''
    if len(s) == 1:
        return get_type(s)
    mid = len(s) // 2
    left = s[:mid]
    right = s[mid:]
    return postorder(left) + postorder(right) + get_type(s)

def main():
    N = int(input().strip())
    s = input().strip()
    print(postorder(s))

if __name__ == "__main__":
    main()