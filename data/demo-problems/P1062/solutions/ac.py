k, N = map(int, input().split())
seq = [1]
power = k
while len(seq) < N:
    sz = len(seq)
    seq.append(power)
    for i in range(sz):
        seq.append(seq[i] + power)
        if len(seq) >= N:
            break
    power *= k
seq.sort()
print(seq[N-1])