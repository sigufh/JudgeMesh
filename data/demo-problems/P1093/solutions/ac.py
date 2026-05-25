n = int(input())
students = []
for i in range(1, n + 1):
    chinese, math, english = map(int, input().split())
    total = chinese + math + english
    students.append((i, chinese, total))

students.sort(key=lambda x: (-x[2], -x[1], x[0]))

for i in range(min(5, n)):
    print(students[i][0], students[i][2])