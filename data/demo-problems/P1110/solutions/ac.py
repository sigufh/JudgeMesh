import sys
import bisect

def solve():
    input = sys.stdin.readline
    n, m = map(int, input().split())
    a = [0] + list(map(int, input().split()))  # 1-indexed

    # 每个位置插入的元素列表
    ins = [[] for _ in range(n + 1)]

    # 相邻差值的最小值（MIN_GAP）
    from sortedcontainers import SortedList
    gaps = SortedList()
    for i in range(1, n):
        gaps.add(abs(a[i] - a[i + 1]))

    # 所有元素的有序集合，用于 MIN_SORT_GAP
    all_vals = SortedList(a[1:])

    # 排序后相邻差值的最小值
    sort_gaps = SortedList()
    for i in range(len(all_vals) - 1):
        sort_gaps.add(all_vals[i + 1] - all_vals[i])

    out_lines = []
    for _ in range(m):
        parts = input().split()
        if parts[0] == 'INSERT':
            i = int(parts[1])
            k = int(parts[2])

            # 确定插入位置的前一个元素
            if not ins[i]:
                prev_val = a[i]
            else:
                prev_val = ins[i][-1]

            # 确定插入位置的后一个元素
            if i == n:
                next_val = None
            else:
                if not ins[i + 1]:
                    next_val = a[i + 1]
                else:
                    next_val = ins[i + 1][0]

            # 更新 gaps
            if i < n:
                old_gap = abs(prev_val - next_val)
                gaps.remove(old_gap)
                gaps.add(abs(prev_val - k))
                gaps.add(abs(k - next_val))
            else:
                gaps.add(abs(prev_val - k))

            # 更新 all_vals 和 sort_gaps
            # 插入 k 到 all_vals
            idx = all_vals.bisect_left(k)
            # 获取前驱和后继
            pred = all_vals[idx - 1] if idx > 0 else None
            succ = all_vals[idx] if idx < len(all_vals) else None

            # 删除旧的相邻差值
            if pred is not None and succ is not None:
                old_diff = succ - pred
                sort_gaps.remove(old_diff)
            # 插入新的相邻差值
            if pred is not None:
                sort_gaps.add(k - pred)
            if succ is not None:
                sort_gaps.add(succ - k)

            all_vals.add(k)

            # 记录插入
            ins[i].append(k)

        elif parts[0] == 'MIN_GAP':
            out_lines.append(str(gaps[0]))
        else:  # MIN_SORT_GAP
            out_lines.append(str(sort_gaps[0]))

    sys.stdout.write('\n'.join(out_lines))

if __name__ == '__main__':
    solve()