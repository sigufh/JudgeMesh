import sys

def next_permutation(arr):
    # 找到第一个升序对
    i = len(arr) - 2
    while i >= 0 and arr[i] >= arr[i + 1]:
        i -= 1
    if i >= 0:
        # 找到右侧第一个大于 arr[i] 的元素
        j = len(arr) - 1
        while arr[j] <= arr[i]:
            j -= 1
        arr[i], arr[j] = arr[j], arr[i]
    # 反转 i 之后的部分
    left, right = i + 1, len(arr) - 1
    while left < right:
        arr[left], arr[right] = arr[right], arr[left]
        left += 1
        right -= 1

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    N = int(data[0])
    M = int(data[1])
    perm = list(map(int, data[2:2+N]))
    for _ in range(M):
        next_permutation(perm)
    print(' '.join(map(str, perm)))

if __name__ == "__main__":
    main()