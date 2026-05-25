m, n = map(int, input().split())
order = list(map(int, input().split()))
machine_for_job_step = []
for _ in range(n):
    machine_for_job_step.append(list(map(int, input().split())))
time_for_job_step = []
for _ in range(n):
    time_for_job_step.append(list(map(int, input().split())))

job_step_count = [0] * n
job_ready_time = [0] * n
machine_schedule = [[] for _ in range(m)]  # list of (start, end)

for job_id in order:
    job = job_id - 1
    step = job_step_count[job]
    job_step_count[job] += 1
    machine = machine_for_job_step[job][step] - 1
    time = time_for_job_step[job][step]

    earliest_start = job_ready_time[job]
    schedule = machine_schedule[machine]
    start_time = earliest_start
    placed = False
    if not schedule:
        start_time = earliest_start
        placed = True
    else:
        # check before first
        if earliest_start + time <= schedule[0][0]:
            start_time = earliest_start
            placed = True
        else:
            # check gaps
            for i in range(len(schedule) - 1):
                gap_start = max(earliest_start, schedule[i][1])
                if gap_start + time <= schedule[i+1][0]:
                    start_time = gap_start
                    placed = True
                    break
            if not placed:
                # after last
                start_time = max(earliest_start, schedule[-1][1])
                placed = True

    end_time = start_time + time
    # insert keeping sorted by start
    inserted = False
    for i in range(len(schedule)):
        if start_time < schedule[i][0]:
            schedule.insert(i, (start_time, end_time))
            inserted = True
            break
    if not inserted:
        schedule.append((start_time, end_time))

    job_ready_time[job] = end_time

max_time = 0
for sched in machine_schedule:
    if sched:
        max_time = max(max_time, sched[-1][1])
print(max_time)