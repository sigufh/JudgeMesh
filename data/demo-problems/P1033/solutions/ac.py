import math

def main():
    H, S1, V, L, K, n = map(float, input().split())
    n = int(n)
    g = 10.0
    eps = 1e-4

    # 下落时间
    t1 = math.sqrt(2 * H / g)
    t2 = math.sqrt(2 * (H - K) / g)

    # 小车在t1时刻的位置范围
    car_left_t1 = S1 - V * t1
    car_right_t1 = S1 + L - V * t1

    # 小车在t2时刻的位置范围
    car_left_t2 = S1 - V * t2
    car_right_t2 = S1 + L - V * t2

    # 小球能被接住的条件
    left = max(car_left_t1, car_left_t2)
    right = min(car_right_t1, car_right_t2)

    left -= eps
    right += eps

    start = math.ceil(left)
    end = math.floor(right)

    if start < 0:
        start = 0
    if end >= n:
        end = n - 1

    ans = 0
    if start <= end:
        ans = end - start + 1

    print(ans)

if __name__ == "__main__":
    main()