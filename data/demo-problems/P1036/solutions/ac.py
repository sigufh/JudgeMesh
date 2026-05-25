import math

def is_prime(num):
    if num < 2:
        return False
    for i in range(2, int(math.isqrt(num)) + 1):
        if num % i == 0:
            return False
    return True

def count_prime_sums(nums, k, start, depth, current_sum):
    if depth == k:
        return 1 if is_prime(current_sum) else 0
    count = 0
    for i in range(start, len(nums)):
        count += count_prime_sums(nums, k, i + 1, depth + 1, current_sum + nums[i])
    return count

def main():
    n, k = map(int, input().split())
    nums = list(map(int, input().split()))
    print(count_prime_sums(nums, k, 0, 0, 0))

if __name__ == "__main__":
    main()