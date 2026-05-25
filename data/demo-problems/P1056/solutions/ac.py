import sys

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    M = int(next(it))
    N = int(next(it))
    K = int(next(it))
    L = int(next(it))
    D = int(next(it))
    
    row_count = [0] * (M + 1)  # 1-indexed, row_count[i] 表示第i行和第i+1行之间
    col_count = [0] * (N + 1)  # col_count[j] 表示第j列和第j+1列之间
    
    for _ in range(D):
        x1 = int(next(it))
        y1 = int(next(it))
        x2 = int(next(it))
        y2 = int(next(it))
        if x1 == x2:
            # 左右相邻，纵向通道
            col = min(y1, y2)
            col_count[col] += 1
        elif y1 == y2:
            # 前后相邻，横向通道
            row = min(x1, x2)
            row_count[row] += 1
    
    # 选择横向通道
    row_candidates = [(row_count[i], i) for i in range(1, M)]
    row_candidates.sort(key=lambda x: (-x[0], x[1]))
    row_selected = [row_candidates[i][1] for i in range(K)]
    row_selected.sort()
    
    # 选择纵向通道
    col_candidates = [(col_count[j], j) for j in range(1, N)]
    col_candidates.sort(key=lambda x: (-x[0], x[1]))
    col_selected = [col_candidates[i][1] for i in range(L)]
    col_selected.sort()
    
    # 输出
    if K > 0:
        print(' '.join(map(str, row_selected)))
    else:
        print()
    if L > 0:
        print(' '.join(map(str, col_selected)))
    else:
        print()

if __name__ == "__main__":
    main()