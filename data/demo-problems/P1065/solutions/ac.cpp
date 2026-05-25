#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

struct Operation {
    int job;
    int step;
    int machine;
    int time;
    int start;
    int end;
};

int main() {
    int m, n;
    cin >> m >> n;
    vector<int> order(m * n);
    for (int i = 0; i < m * n; ++i) {
        cin >> order[i];
    }
    vector<vector<int>> machine_for_job_step(n, vector<int>(m));
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < m; ++j) {
            cin >> machine_for_job_step[i][j];
        }
    }
    vector<vector<int>> time_for_job_step(n, vector<int>(m));
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < m; ++j) {
            cin >> time_for_job_step[i][j];
        }
    }

    vector<int> job_step_count(n, 0);
    vector<int> job_ready_time(n, 0);
    vector<vector<pair<int, int>>> machine_schedule(m); // (start, end)

    vector<Operation> ops;
    for (int idx = 0; idx < m * n; ++idx) {
        int job = order[idx] - 1;
        int step = job_step_count[job];
        job_step_count[job]++;
        int machine = machine_for_job_step[job][step] - 1;
        int time = time_for_job_step[job][step];

        int earliest_start = job_ready_time[job];
        // find earliest gap on machine
        int start_time = earliest_start;
        auto& schedule = machine_schedule[machine];
        // sort schedule by start time (should already be sorted if we insert in order)
        // but we can just scan
        bool placed = false;
        if (schedule.empty()) {
            start_time = earliest_start;
            placed = true;
        } else {
            // check gap before first
            if (earliest_start + time <= schedule[0].first) {
                start_time = earliest_start;
                placed = true;
            } else {
                // check gaps between
                for (size_t i = 0; i < schedule.size() - 1; ++i) {
                    int gap_start = max(earliest_start, schedule[i].second);
                    if (gap_start + time <= schedule[i+1].first) {
                        start_time = gap_start;
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    // after last
                    start_time = max(earliest_start, schedule.back().second);
                    placed = true;
                }
            }
        }

        int end_time = start_time + time;
        // insert into schedule
        auto it = schedule.begin();
        while (it != schedule.end() && it->first < start_time) ++it;
        schedule.insert(it, {start_time, end_time});

        job_ready_time[job] = end_time;
        ops.push_back({job, step, machine, time, start_time, end_time});
    }

    int max_time = 0;
    for (int i = 0; i < m; ++i) {
        if (!machine_schedule[i].empty()) {
            max_time = max(max_time, machine_schedule[i].back().second);
        }
    }
    cout << max_time << endl;
    return 0;
}