-- wrk script for submit-service / gateway submit path.
--
-- Examples:
--   wrk -t4 -c50 -d5m -s scripts/loadtest-submit.lua http://127.0.0.1:8083
--   TOKEN=... wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://127.0.0.1:8080
--
-- Optional environment variables:
--   TOKEN USER_ID=1001 PROBLEM_ID=1 CONTEST_ID=100001 LANGUAGE=cpp

local counter = 0

local token = os.getenv("TOKEN") or ""
local user_id = os.getenv("USER_ID") or "1002"
local problem_id = tonumber(os.getenv("PROBLEM_ID") or "1001")
local contest_id = os.getenv("CONTEST_ID")
local language = os.getenv("LANGUAGE") or "python"

local function source_code(i)
  if language == "cpp" or language == "c++" then
    return [[
#include <bits/stdc++.h>
using namespace std;
int main() {
  long long a, b;
  if (!(cin >> a >> b)) return 0;
  cout << (a + b) << "\n";
  return 0;
}
// loadtest submission ]] .. tostring(i) .. "\n"
  end

  local codes = {
    'a,b=map(int,input().split())\nprint(a+b)\n',
    'print(input()[::-1])\n',
    'n=int(input())\nprint("odd" if n%2 else "even")\n'
  }
  return codes[(i % #codes) + 1]
end

request = function()
  counter = counter + 1
  local body = {
    '"userId":' .. user_id,
    '"problemId":' .. problem_id,
    '"language":"' .. language .. '"',
    '"code":' .. string.format("%q", source_code(counter)),
    '"timeLimitMs":1000',
    '"memoryLimitMb":256',
    '"testcaseManifestUrl":"http://problem-service/api/problems/' .. problem_id .. '/testcase/manifest"'
  }
  if contest_id ~= nil and contest_id ~= "" then
    table.insert(body, '"contestId":' .. tonumber(contest_id))
  end

  local headers = {
    ["Content-Type"] = "application/json",
    ["X-User-Id"] = user_id
  }
  if token ~= "" then
    headers["Authorization"] = "Bearer " .. token
  end

  return wrk.format("POST", "/api/submit", headers, "{" .. table.concat(body, ",") .. "}")
end

done = function(summary, latency, requests)
  io.write(string.format("requests=%d errors=%d timeout=%d status=%d\n",
    summary.requests,
    summary.errors.connect + summary.errors.read + summary.errors.write,
    summary.errors.timeout,
    summary.errors.status))
  io.write(string.format("latency_p50_ms=%.2f latency_p95_ms=%.2f latency_p99_ms=%.2f\n",
    latency:percentile(50) / 1000,
    latency:percentile(95) / 1000,
    latency:percentile(99) / 1000))
end
