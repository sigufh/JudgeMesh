import sys

def trim_spaces(s):
    return ''.join(c for c in s if not c.isspace())

def check_brackets(s):
    cnt = 0
    for c in s:
        if c == '(':
            cnt += 1
        elif c == ')':
            cnt -= 1
            if cnt < 0:
                return False
    return cnt == 0

def precedence(op):
    if op in ('+', '-'):
        return 1
    if op == '*':
        return 2
    if op == '^':
        return 3
    return 0

def infix_to_postfix(s):
    output = []
    ops = []
    i = 0
    while i < len(s):
        if s[i].isdigit():
            num = ''
            while i < len(s) and s[i].isdigit():
                num += s[i]
                i += 1
            output.append(num)
        elif s[i] == 'a':
            output.append('a')
            i += 1
        elif s[i] == '(':
            ops.append('(')
            i += 1
        elif s[i] == ')':
            while ops and ops[-1] != '(':
                output.append(ops.pop())
            if ops:
                ops.pop()  # pop '('
            i += 1
        else:  # operator
            op = s[i]
            while ops and ops[-1] != '(' and (
                (op != '^' and precedence(ops[-1]) >= precedence(op)) or
                (op == '^' and precedence(ops[-1]) > precedence(op))
            ):
                output.append(ops.pop())
            ops.append(op)
            i += 1
    while ops:
        output.append(ops.pop())
    return output

def eval_postfix(post, a_val):
    stack = []
    for token in post:
        if token == 'a':
            stack.append(a_val)
        elif token.isdigit():
            stack.append(int(token))
        else:
            b = stack.pop()
            a = stack.pop()
            if token == '+':
                stack.append(a + b)
            elif token == '-':
                stack.append(a - b)
            elif token == '*':
                stack.append(a * b)
            elif token == '^':
                stack.append(a ** b)
    return stack[0]

def equivalent(e1, e2):
    s1 = trim_spaces(e1)
    s2 = trim_spaces(e2)
    if not check_brackets(s1) or not check_brackets(s2):
        return False
    post1 = infix_to_postfix(s1)
    post2 = infix_to_postfix(s2)
    test_vals = [1, 2, 3, 5, 7, 10, 100]
    for a_val in test_vals:
        if eval_postfix(post1, a_val) != eval_postfix(post2, a_val):
            return False
    return True

def main():
    lines = sys.stdin.read().splitlines()
    if not lines:
        return
    expr = lines[0].strip()
    n = int(lines[1].strip())
    options = [line.strip() for line in lines[2:2+n]]
    result = []
    for i, opt in enumerate(options):
        if equivalent(expr, opt):
            result.append(chr(ord('A') + i))
    print(''.join(result))

if __name__ == '__main__':
    main()