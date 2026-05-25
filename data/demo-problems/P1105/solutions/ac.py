import sys

def solve():
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    it = iter(data)
    N = int(next(it))
    platforms = []
    for i in range(N):
        h = int(next(it))
        l = int(next(it))
        r = int(next(it))
        platforms.append((h, l, r, i + 1))
    
    # 按高度降序，高度相同按编号升序
    platforms.sort(key=lambda x: (-x[0], x[3]))
    
    left_ans = [0] * (N + 1)
    right_ans = [0] * (N + 1)
    
    for i in range(N):
        cur_h, cur_l, cur_r, cur_id = platforms[i]
        
        # 左边缘
        best_left = 0
        best_left_h = -1
        for j in range(i + 1, N):
            h_j, l_j, r_j, id_j = platforms[j]
            if h_j < cur_h and l_j < cur_l < r_j:
                if h_j > best_left_h:
                    best_left_h = h_j
                    best_left = id_j
                elif h_j == best_left_h and id_j < best_left:
                    best_left = id_j
        left_ans[cur_id] = best_left
        
        # 右边缘
        best_right = 0
        best_right_h = -1
        for j in range(i + 1, N):
            h_j, l_j, r_j, id_j = platforms[j]
            if h_j < cur_h and l_j < cur_r < r_j:
                if h_j > best_right_h:
                    best_right_h = h_j
                    best_right = id_j
                elif h_j == best_right_h and id_j < best_right:
                    best_right = id_j
        right_ans[cur_id] = best_right
    
    out_lines = []
    for i in range(1, N + 1):
        out_lines.append(f"{left_ans[i]} {right_ans[i]}")
    sys.stdout.write("\n".join(out_lines))

if __name__ == "__main__":
    solve()