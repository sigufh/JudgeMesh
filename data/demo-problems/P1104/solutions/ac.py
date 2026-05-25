n = int(input())
students = []
for i in range(n):
    name, y, m, d = input().split()
    students.append((name, int(y), int(m), int(d), i))

# 年龄从大到小：出生日期早的在前
# 如果生日相同，输入靠后的（index大的）先输出
students.sort(key=lambda x: (x[1], x[2], x[3], -x[4]))

for s in students:
    print(s[0])