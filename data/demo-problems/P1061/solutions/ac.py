def main():
    s, t, w = map(int, input().split())
    jam = list(input().strip())

    min_char = chr(ord('a') + s - 1)
    max_char = chr(ord('a') + t - 1)

    count = 0
    while count < 5:
        # 从右向左找到第一个可以增加的位
        pos = w - 1
        while pos >= 0 and jam[pos] == chr(ord(max_char) - (w - 1 - pos)):
            pos -= 1
        if pos < 0:
            break

        # 当前位增加1
        jam[pos] = chr(ord(jam[pos]) + 1)
        # 后面的位依次递增
        for i in range(pos + 1, w):
            jam[i] = chr(ord(jam[i - 1]) + 1)

        print(''.join(jam))
        count += 1

if __name__ == "__main__":
    main()