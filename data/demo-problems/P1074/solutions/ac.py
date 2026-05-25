import sys
sys.setrecursionlimit(1000000)

score = [
    [6,6,6,6,6,6,6,6,6],
    [6,7,7,7,7,7,7,7,6],
    [6,7,8,8,8,8,8,7,6],
    [6,7,8,9,9,9,8,7,6],
    [6,7,8,9,10,9,8,7,6],
    [6,7,8,9,9,9,8,7,6],
    [6,7,8,8,8,8,8,7,6],
    [6,7,7,7,7,7,7,7,6],
    [6,6,6,6,6,6,6,6,6]
]

a = [list(map(int, input().split())) for _ in range(9)]
row = [[False]*10 for _ in range(9)]
col = [[False]*10 for _ in range(9)]
block = [[[False]*10 for _ in range(3)] for _ in range(3)]

for i in range(9):
    for j in range(9):
        if a[i][j] != 0:
            num = a[i][j]
            row[i][num] = True
            col[j][num] = True
            block[i//3][j//3][num] = True

ans = -1

def dfs(x, y, cur_sum):
    global ans
    if x == 9:
        ans = max(ans, cur_sum)
        return
    if y == 9:
        dfs(x+1, 0, cur_sum)
        return
    if a[x][y] != 0:
        dfs(x, y+1, cur_sum + a[x][y] * score[x][y])
        return
    for num in range(1, 10):
        if not row[x][num] and not col[y][num] and not block[x//3][y//3][num]:
            row[x][num] = col[y][num] = block[x//3][y//3][num] = True
            dfs(x, y+1, cur_sum + num * score[x][y])
            row[x][num] = col[y][num] = block[x//3][y//3][num] = False

dfs(0, 0, 0)
print(ans)