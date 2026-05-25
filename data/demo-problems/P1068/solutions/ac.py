import sys

def main():
    data = sys.stdin.read().strip().split()
    if not data:
        return
    it = iter(data)
    n = int(next(it))
    m = int(next(it))
    students = []
    for _ in range(n):
        k = int(next(it))
        s = int(next(it))
        students.append((k, s))
    
    students.sort(key=lambda x: (-x[1], x[0]))
    
    interview_count = int(m * 1.5)
    score_line = students[interview_count - 1][1]
    
    actual_count = interview_count
    while actual_count < n and students[actual_count][1] == score_line:
        actual_count += 1
    
    print(f"{score_line} {actual_count}")
    for i in range(actual_count):
        print(f"{students[i][0]} {students[i][1]}")

if __name__ == "__main__":
    main()