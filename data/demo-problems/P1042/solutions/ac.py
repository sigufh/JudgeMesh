import sys

def main():
    data = sys.stdin.read()
    # 找到E的位置，忽略之后的内容
    e_idx = data.find('E')
    if e_idx != -1:
        data = data[:e_idx]
    
    # 过滤掉非W和L的字符（虽然题目说E之后忽略，但可能有其他字符在E之前？按题意只保留W和L）
    # 实际上根据提示，E之后可能出现非WL字符，我们已经截断了，所以这里只保留W和L
    matches = [c for c in data if c in ('W', 'L')]
    
    def simulate(limit):
        w = l = 0
        res = []
        for c in matches:
            if c == 'W':
                w += 1
            else:
                l += 1
            if (w >= limit or l >= limit) and abs(w - l) >= 2:
                res.append((w, l))
                w = l = 0
        res.append((w, l))
        return res
    
    res11 = simulate(11)
    res21 = simulate(21)
    
    for w, l in res11:
        print(f"{w}:{l}")
    print()
    for w, l in res21:
        print(f"{w}:{l}")

if __name__ == "__main__":
    main()