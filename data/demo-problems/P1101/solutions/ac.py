def main():
    n = int(input())
    grid = [input().strip() for _ in range(n)]
    target = "yizhong"
    dx = [0, 0, 1, -1, 1, -1, 1, -1]
    dy = [1, -1, 0, 0, 1, -1, -1, 1]
    mark = [[False] * n for _ in range(n)]

    for i in range(n):
        for j in range(n):
            if grid[i][j] != 'y':
                continue
            for d in range(8):
                x, y = i, j
                ok = True
                for k in range(7):
                    if x < 0 or x >= n or y < 0 or y >= n or grid[x][y] != target[k]:
                        ok = False
                        break
                    x += dx[d]
                    y += dy[d]
                if ok:
                    x, y = i, j
                    for k in range(7):
                        mark[x][y] = True
                        x += dx[d]
                        y += dy[d]

    for i in range(n):
        row = []
        for j in range(n):
            if mark[i][j]:
                row.append(grid[i][j])
            else:
                row.append('*')
        print(''.join(row))

if __name__ == "__main__":
    main()