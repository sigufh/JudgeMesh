def main():
    import sys
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    n = int(data[0])
    a = list(map(int, data[1:1+n]))
    L = int(data[1+n])
    R = int(data[2+n])
    
    total = sum(a)
    if total < L * n or total > R * n:
        print(-1)
        return
    
    need = 0
    extra = 0
    for x in a:
        if x < L:
            need += L - x
        elif x > R:
            extra += x - R
    
    print(max(need, extra))

if __name__ == "__main__":
    main()