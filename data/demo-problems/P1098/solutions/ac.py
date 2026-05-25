p1, p2, p3 = map(int, input().split())
s = input().strip()
ans = []
i = 0
while i < len(s):
    if s[i] != '-' or i == 0 or i == len(s) - 1:
        ans.append(s[i])
        i += 1
        continue
    left = s[i - 1]
    right = s[i + 1]
    if not ((left.isdigit() and right.isdigit()) or (left.islower() and right.islower())):
        ans.append(s[i])
        i += 1
        continue
    if right <= left:
        ans.append(s[i])
        i += 1
        continue
    if ord(right) == ord(left) + 1:
        i += 1
        continue
    fill = []
    for c in range(ord(left) + 1, ord(right)):
        ch = chr(c)
        if p1 == 2 and ch.islower():
            ch = ch.upper()
        elif p1 == 3:
            ch = '*'
        fill.append(ch * p2)
    if p3 == 2:
        fill.reverse()
    ans.append(''.join(fill))
    i += 1
print(''.join(ans))