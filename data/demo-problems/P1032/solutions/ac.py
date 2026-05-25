from collections import deque

def main():
    A, B = input().split()
    rules = []
    try:
        while True:
            line = input().split()
            if not line:
                break
            rules.append((line[0], line[1]))
    except EOFError:
        pass

    if A == B:
        print(0)
        return

    q = deque()
    q.append((A, 0))
    dist = {A: 0}

    while q:
        cur, steps = q.popleft()
        if steps >= 10:
            continue
        for fr, to in rules:
            pos = 0
            while True:
                pos = cur.find(fr, pos)
                if pos == -1:
                    break
                nxt = cur[:pos] + to + cur[pos+len(fr):]
                if nxt == B:
                    print(steps + 1)
                    return
                if nxt not in dist or dist[nxt] > steps + 1:
                    dist[nxt] = steps + 1
                    q.append((nxt, steps + 1))
                pos += 1

    print("NO ANSWER!")

if __name__ == "__main__":
    main()