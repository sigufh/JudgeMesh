import sys

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    m = int(next(it))
    n = int(next(it))
    a = [[0] * n for _ in range(m)]
    for i in range(m):
        for j in range(n):
            a[i][j] = int(next(it))

    # 计算画布大小
    max_h = 0
    for i in range(m):
        for j in range(n):
            h = a[i][j]
            total_h = 1 + 3 * h + 2 * (m - 1 - i)
            if total_h > max_h:
                max_h = total_h
    K = max_h
    L = 1 + 4 * n + 2 * m

    # 初始化画布
    canvas = [['.'] * L for _ in range(K)]

    # 绘制每个格子
    for i in range(m):
        for j in range(n):
            h = a[i][j]
            x = 2 * (m - 1 - i)
            y = 4 * j + 2 * (m - 1 - i)
            for k in range(h):
                cx = x + 3 * k
                cy = y
                # 顶面
                canvas[cx][cy] = '+'
                canvas[cx][cy + 1] = '-'
                canvas[cx][cy + 2] = '-'
                canvas[cx][cy + 3] = '-'
                canvas[cx][cy + 4] = '+'
                # 前面
                canvas[cx + 1][cy] = '|'
                canvas[cx + 1][cy + 1] = ' '
                canvas[cx + 1][cy + 2] = ' '
                canvas[cx + 1][cy + 3] = ' '
                canvas[cx + 1][cy + 4] = '|'
                canvas[cx + 1][cy + 5] = '/'
                # 侧面
                canvas[cx + 2][cy] = '|'
                canvas[cx + 2][cy + 1] = ' '
                canvas[cx + 2][cy + 2] = ' '
                canvas[cx + 2][cy + 3] = ' '
                canvas[cx + 2][cy + 4] = '|'
                canvas[cx + 2][cy + 5] = ' '
                canvas[cx + 2][cy + 6] = '+'
                # 底面
                canvas[cx + 3][cy] = '+'
                canvas[cx + 3][cy + 1] = '-'
                canvas[cx + 3][cy + 2] = '-'
                canvas[cx + 3][cy + 3] = '-'
                canvas[cx + 3][cy + 4] = '+'
                canvas[cx + 3][cy + 5] = ' '
                canvas[cx + 3][cy + 6] = '|'
                # 右侧面
                canvas[cx + 4][cy + 1] = '/'
                canvas[cx + 4][cy + 2] = ' '
                canvas[cx + 4][cy + 3] = ' '
                canvas[cx + 4][cy + 4] = ' '
                canvas[cx + 4][cy + 5] = '|'
                canvas[cx + 4][cy + 6] = '+'
                # 最后一行
                canvas[cx + 5][cy + 2] = '+'
                canvas[cx + 5][cy + 3] = '-'
                canvas[cx + 5][cy + 4] = '-'
                canvas[cx + 5][cy + 5] = '-'
                canvas[cx + 5][cy + 6] = '+'

    # 输出（从最后一行开始）
    for i in range(K - 1, -1, -1):
        print(''.join(canvas[i]))

if __name__ == "__main__":
    main()