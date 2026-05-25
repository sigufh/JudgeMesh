s = input().strip()
total = 0
j = 1
for ch in s[:-1]:
    if ch != '-':
        total += int(ch) * j
        j += 1
mod = total % 11
check = 'X' if mod == 10 else str(mod)
if check == s[-1]:
    print("Right")
else:
    print(s[:-1] + check)