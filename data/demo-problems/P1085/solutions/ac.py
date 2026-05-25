max_unhappy = 0
day = 0
for i in range(1, 8):
    school, extra = map(int, input().split())
    total = school + extra
    if total > 8 and total > max_unhappy:
        max_unhappy = total
        day = i
print(day)