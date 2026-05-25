def main():
    n = input().strip()
    k = int(input().strip())
    
    stack = []
    to_remove = k
    
    for digit in n:
        while stack and to_remove > 0 and stack[-1] > digit:
            stack.pop()
            to_remove -= 1
        stack.append(digit)
    
    # 如果还有需要删除的数字，从末尾删除
    if to_remove > 0:
        stack = stack[:-to_remove]
    
    # 去除前导零
    result = ''.join(stack).lstrip('0')
    
    if result == '':
        print('0')
    else:
        print(result)

if __name__ == "__main__":
    main()