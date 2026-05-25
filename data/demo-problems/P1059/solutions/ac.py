def main():
    N = int(input())
    numbers = list(map(int, input().split()))
    unique_sorted = sorted(set(numbers))
    print(len(unique_sorted))
    print(' '.join(map(str, unique_sorted)))

if __name__ == "__main__":
    main()