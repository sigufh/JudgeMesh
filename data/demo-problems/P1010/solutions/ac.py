def dfs(n):
    if n == 0:
        return "0"
    if n == 1:
        return "2(0)"
    if n == 2:
        return "2"
    res = []
    for i in range(15, -1, -1):
        if n & (1 << i):
            if i == 0:
                res.append("2(0)")
            elif i == 1:
                res.append("2")
            else:
                res.append("2(" + dfs(i) + ")")
    return "+".join(res)

n = int(input())
print(dfs(n))