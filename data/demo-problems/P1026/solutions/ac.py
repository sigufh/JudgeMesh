def solve():
    import sys
    input = sys.stdin.read
    data = input().split()
    if not data:
        return
    idx = 0
    p = int(data[idx]); idx += 1
    k = int(data[idx]); idx += 1
    str_list = []
    for _ in range(p):
        str_list.append(data[idx]); idx += 1
    s = int(data[idx]); idx += 1
    dict_words = []
    for _ in range(s):
        dict_words.append(data[idx]); idx += 1
    
    string = ''.join(str_list)
    n = len(string)
    
    # 预处理 sum[i][j] 表示子串 string[i:j+1] 中包含的单词数
    sum_matrix = [[0] * n for _ in range(n)]
    for i in range(n):
        for j in range(i, n):
            cnt = 0
            for l in range(i, j + 1):
                for word in dict_words:
                    length = len(word)
                    if l + length - 1 <= j and string[l:l+length] == word:
                        cnt += 1
                        break
            sum_matrix[i][j] = cnt
    
    # dp[i][j] 表示前i个字符分成j份的最大单词数
    dp = [[-10**9] * (k + 1) for _ in range(n + 1)]
    dp[0][0] = 0
    
    for i in range(1, n + 1):
        for j in range(1, min(i, k) + 1):
            for l in range(j - 1, i):
                if dp[l][j - 1] >= 0:
                    dp[i][j] = max(dp[i][j], dp[l][j - 1] + sum_matrix[l][i - 1])
    
    print(dp[n][k])

if __name__ == "__main__":
    solve()