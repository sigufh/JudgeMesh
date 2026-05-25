import sys

def main():
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    it = iter(data)
    n = int(next(it))
    king_left = int(next(it))
    king_right = int(next(it))
    ministers = []
    for _ in range(n):
        l = int(next(it))
        r = int(next(it))
        ministers.append((l, r, l * r))
    # 按乘积排序
    ministers.sort(key=lambda x: x[2])
    
    # 当前乘积，初始为国王左手数
    product = king_left
    max_reward = 0
    
    for l, r, _ in ministers:
        # 计算当前大臣奖励
        reward = product // r
        if reward > max_reward:
            max_reward = reward
        # 更新乘积
        product *= l
    
    print(max_reward)

if __name__ == "__main__":
    main()