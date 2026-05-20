 -- wrk script for submit-service / gateway submit path.
  --
  -- Examples:
  --   wrk -t4 -c50 -d5m -s scripts/loadtest-submit.lua http://127.0.0.1:8083
  --   TOKEN="$(BASE_URL=http://localhost:8080 scripts/gen-jwt.sh)" wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://localhost:8080
  --
  -- Optional environment variables:
  --   TOKEN=jwt USER_ID=1001 PROBLEM_ID=1 CONTEST_ID=100001 LANGUAGE=python

  local counter = 0

  local token = os.getenv("TOKEN") or ""
  local user_id = os.getenv("USER_ID") or "1001"
  local problem_id = tonumber(os.getenv("PROBLEM_ID") or "1")
  local contest_id = os.getenv("CONTEST_ID")
  local language = os.getenv("LANGUAGE") or "python"

  wrk.method = "POST"
  wrk.headers["Content-Type"] = "application/json"
  wrk.headers["X-User-Id"] = user_id
  if token ~= "" then
    wrk.headers["Authorization"] = "Bearer " .. token
  end

  local code_samples = {
    {
      language = "python",
      code = 'a,b=map(int,input().split())\\nprint(a+b)\\n'
    },
    {
      language = "python",
      code = 'print(input()[::-1])\\n'
    },
    {
      language = "cpp",
      code = [[
  #include <bits/stdc++.h>
  using namespace std;
  int main() {
    long long a, b;
    if (!(cin >> a >> b)) return 0;
    cout << (a + b) << "\n";
    return 0;
  }
  ]]
    }
  }

  local function json_string(value)
    return string.format("%q", value)
      latency:percentile(95) / 1000,
      latency:percentile(99) / 1000))
  end