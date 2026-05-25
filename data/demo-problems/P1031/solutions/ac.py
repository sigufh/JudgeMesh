def main():
    N = int(input())
    A = list(map(int, input().split()))
    avg = sum(A) // N
    moves = 0
    for i in range(N - 1):
        if A[i] != avg:
            diff = A[i] - avg
            A[i + 1] += diff
            moves += 1
    print(moves)

if __name__ == "__main__":
    main()