eq = input().strip()

coeff = 0
constant = 0
sign = 1  # 1 for left side, -1 for right side
current_sign = 1  # 1 for positive, -1 for negative
num = 0
has_num = False
var = ''

i = 0
while i < len(eq):
    c = eq[i]
    if c == '=':
        if has_num:
            constant += sign * current_sign * num
            has_num = False
            num = 0
        sign = -1
        current_sign = 1
    elif c == '+' or c == '-':
        if has_num:
            constant += sign * current_sign * num
            has_num = False
            num = 0
        if c == '+':
            current_sign = 1
        else:
            current_sign = -1
    elif c.isdigit():
        num = num * 10 + int(c)
        has_num = True
    elif c.islower():
        var = c
        if not has_num:
            num = 1
        coeff += sign * current_sign * num
        has_num = False
        num = 0
    i += 1

if has_num:
    constant += sign * current_sign * num

# Equation: coeff * var + constant = 0  => var = -constant / coeff
result = -constant / coeff
if result == 0.0:
    result = 0.0
print(f"{result:.3f}")