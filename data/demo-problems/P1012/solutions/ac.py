def main():
    n = int(input())
    nums = input().split()
    nums.sort(key=lambda x: x * 10, reverse=True)  # 简单比较方法，但更严谨的应该用自定义比较
    # 更严谨的自定义排序
    from functools import cmp_to_key
    def cmp(a, b):
        if a + b > b + a:
            return -1
        elif a + b < b + a:
            return 1
        else:
            return 0
    nums.sort(key=cmp_to_key(cmp))
    print(''.join(nums))

if __name__ == "__main__":
    main()