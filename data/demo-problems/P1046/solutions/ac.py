apples = list(map(int, input().split()))
height = int(input())
reach = height + 30
count = sum(1 for apple in apples if apple <= reach)
print(count)