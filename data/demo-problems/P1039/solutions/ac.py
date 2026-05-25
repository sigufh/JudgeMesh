import sys
from itertools import combinations

def solve():
    input_data = sys.stdin.read().strip().splitlines()
    if not input_data:
        return
    first_line = input_data[0].split()
    M = int(first_line[0])
    N = int(first_line[1])
    P = int(first_line[2])
    
    names = []
    name_to_id = {}
    for i in range(1, M+1):
        name = input_data[i].strip()
        names.append(name)
        name_to_id[name] = i-1
    
    testimonies = [[] for _ in range(M)]
    for i in range(M+1, M+1+P):
        line = input_data[i].strip()
        colon_pos = line.find(": ")
        speaker = line[:colon_pos]
        if speaker in name_to_id:
            speaker_id = name_to_id[speaker]
            testimonies[speaker_id].append(line)
    
    days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    
    def is_true(testimony, guilty_id, day, is_liar):
        colon_pos = testimony.find(": ")
        speaker = testimony[:colon_pos]
        content = testimony[colon_pos+2:]
        speaker_id = name_to_id[speaker]
        speaker_is_liar = is_liar[speaker_id]
        
        if content == "I am guilty.":
            statement_truth = (speaker_id == guilty_id)
        elif content == "I am not guilty.":
            statement_truth = (speaker_id != guilty_id)
        elif content.endswith(" is guilty."):
            name = content[:-11]
            if name not in name_to_id:
                return False
            statement_truth = (name_to_id[name] == guilty_id)
        elif content.endswith(" is not guilty."):
            name = content[:-15]
            if name not in name_to_id:
                return False
            statement_truth = (name_to_id[name] != guilty_id)
        elif content.startswith("Today is "):
            day_name = content[9:]
            if day_name.endswith('.'):
                day_name = day_name[:-1]
            if day_name not in days:
                return False
            mentioned_day = days.index(day_name)
            statement_truth = (mentioned_day == day)
        else:
            return False
        
        return not statement_truth if speaker_is_liar else statement_truth
    
    def check(guilty_id, day, is_liar):
        for i in range(M):
            for t in testimonies[i]:
                if not is_true(t, guilty_id, day, is_liar):
                    return False
        return True
    
    possible_guilty = set()
    
    for guilty in range(M):
        found = False
        for day in range(7):
            # 枚举所有大小为N的说谎者子集
            for liars in combinations(range(M), N):
                is_liar = [1 if i in liars else 0 for i in range(M)]
                if check(guilty, day, is_liar):
                    possible_guilty.add(names[guilty])
                    found = True
                    break
            if found:
                break
    
    if not possible_guilty:
        print("Impossible")
    elif len(possible_guilty) > 1:
        print("Cannot Determine")
    else:
        print(possible_guilty.pop())

if __name__ == "__main__":
    solve()