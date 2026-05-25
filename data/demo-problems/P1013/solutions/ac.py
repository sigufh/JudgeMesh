import sys

def solve():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    n = int(data[0])
    idx = 1
    table = [[[] for _ in range(n)] for _ in range(n)]
    for i in range(n):
        for j in range(n):
            table[i][j].append(data[idx])
            idx += 1
    letters = [table[0][i][0] for i in range(1, n)]
    m = len(letters)
    val = {}
    used = [False] * 10

    def check(base):
        for i in range(1, n):
            for j in range(1, n):
                a = val[letters[i-1]]
                b = val[letters[j-1]]
                s = a + b
                if s < base:
                    expected = None
                    for k, v in val.items():
                        if v == s:
                            expected = k
                            break
                    if expected is None:
                        return False
                    if table[i][j][0] != expected:
                        return False
                else:
                    high = s // base
                    low = s % base
                    h = l = None
                    for k, v in val.items():
                        if v == high:
                            h = k
                        if v == low:
                            l = k
                    if h is None or l is None:
                        return False
                    if table[i][j][0] != h + l:
                        return False
        return True

    def dfs(pos, base):
        if pos == m:
            return check(base)
        c = letters[pos]
        for d in range(base):
            if not used[d]:
                val[c] = d
                used[d] = True
                if dfs(pos + 1, base):
                    return True
                used[d] = False
                del val[c]
        return False

    for base in range(2, 11):
        val.clear()
        used = [False] * 10
        if dfs(0, base):
            out = []
            for c in letters:
                out.append(f"{c}={val[c]}")
            print(" ".join(out))
            print(base)
            return
    print("ERROR!")

if __name__ == "__main__":
    solve()