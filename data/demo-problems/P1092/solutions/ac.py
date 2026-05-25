import sys
sys.setrecursionlimit(1000000)

def solve():
    n = int(sys.stdin.readline().strip())
    s1 = sys.stdin.readline().strip()
    s2 = sys.stdin.readline().strip()
    s3 = sys.stdin.readline().strip()
    val = [-1] * 26
    used = [False] * 26
    found = False

    def dfs(pos, carry):
        nonlocal found
        if found:
            return
        if pos == n:
            if carry == 0:
                found = True
                print(' '.join(str(val[i]) for i in range(n)))
            return
        idx1 = ord(s1[n - 1 - pos]) - ord('A')
        idx2 = ord(s2[n - 1 - pos]) - ord('A')
        idx3 = ord(s3[n - 1 - pos]) - ord('A')
        if val[idx1] != -1 and val[idx2] != -1 and val[idx3] != -1:
            s = val[idx1] + val[idx2] + carry
            if s % n == val[idx3]:
                dfs(pos + 1, s // n)
            return
        if val[idx1] != -1 and val[idx2] != -1:
            s = val[idx1] + val[idx2] + carry
            d = s % n
            if val[idx3] == -1 and not used[d]:
                val[idx3] = d
                used[d] = True
                dfs(pos + 1, s // n)
                val[idx3] = -1
                used[d] = False
            elif val[idx3] == d:
                dfs(pos + 1, s // n)
            return
        if val[idx1] != -1 and val[idx3] != -1:
            d = (val[idx3] - val[idx1] - carry) % n
            if val[idx2] == -1 and not used[d]:
                val[idx2] = d
                used[d] = True
                s = val[idx1] + val[idx2] + carry
                dfs(pos + 1, s // n)
                val[idx2] = -1
                used[d] = False
            elif val[idx2] == d:
                s = val[idx1] + val[idx2] + carry
                if s % n == val[idx3]:
                    dfs(pos + 1, s // n)
            return
        if val[idx2] != -1 and val[idx3] != -1:
            d = (val[idx3] - val[idx2] - carry) % n
            if val[idx1] == -1 and not used[d]:
                val[idx1] = d
                used[d] = True
                s = val[idx1] + val[idx2] + carry
                dfs(pos + 1, s // n)
                val[idx1] = -1
                used[d] = False
            elif val[idx1] == d:
                s = val[idx1] + val[idx2] + carry
                if s % n == val[idx3]:
                    dfs(pos + 1, s // n)
            return
        if val[idx1] != -1:
            for d2 in range(n):
                if used[d2]:
                    continue
                s = val[idx1] + d2 + carry
                d3 = s % n
                if val[idx3] == -1 and not used[d3] and d3 != d2:
                    val[idx2] = d2
                    val[idx3] = d3
                    used[d2] = used[d3] = True
                    dfs(pos + 1, s // n)
                    val[idx2] = val[idx3] = -1
                    used[d2] = used[d3] = False
                elif val[idx3] == d3 and not used[d2] and d2 != d3:
                    val[idx2] = d2
                    used[d2] = True
                    dfs(pos + 1, s // n)
                    val[idx2] = -1
                    used[d2] = False
            return
        if val[idx2] != -1:
            for d1 in range(n):
                if used[d1]:
                    continue
                s = d1 + val[idx2] + carry
                d3 = s % n
                if val[idx3] == -1 and not used[d3] and d3 != d1:
                    val[idx1] = d1
                    val[idx3] = d3
                    used[d1] = used[d3] = True
                    dfs(pos + 1, s // n)
                    val[idx1] = val[idx3] = -1
                    used[d1] = used[d3] = False
                elif val[idx3] == d3 and not used[d1] and d1 != d3:
                    val[idx1] = d1
                    used[d1] = True
                    dfs(pos + 1, s // n)
                    val[idx1] = -1
                    used[d1] = False
            return
        if val[idx3] != -1:
            for d1 in range(n):
                if used[d1]:
                    continue
                d2 = (val[idx3] - d1 - carry) % n
                if not used[d2] and d2 != d1:
                    val[idx1] = d1
                    val[idx2] = d2
                    used[d1] = used[d2] = True
                    s = d1 + d2 + carry
                    dfs(pos + 1, s // n)
                    val[idx1] = val[idx2] = -1
                    used[d1] = used[d2] = False
            return
        for d1 in range(n):
            if used[d1]:
                continue
            for d2 in range(n):
                if used[d2] or d2 == d1:
                    continue
                s = d1 + d2 + carry
                d3 = s % n
                if not used[d3] and d3 != d1 and d3 != d2:
                    val[idx1] = d1
                    val[idx2] = d2
                    val[idx3] = d3
                    used[d1] = used[d2] = used[d3] = True
                    dfs(pos + 1, s // n)
                    val[idx1] = val[idx2] = val[idx3] = -1
                    used[d1] = used[d2] = used[d3] = False

    dfs(0, 0)

if __name__ == "__main__":
    solve()