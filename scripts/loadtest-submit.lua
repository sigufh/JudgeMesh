local token = os.getenv("TOKEN") or ""
local problem_id = os.getenv("PROBLEM_ID") or "1"
local user_id = os.getenv("USER_ID") or "1002"

wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"
if token ~= "" then
  wrk.headers["Authorization"] = "Bearer " .. token
end

local codes = {
  'a,b=map(int,input().split())\\nprint(a+b)\\n',
  'print(input()[::-1])\\n',
  'n=int(input())\\nprint("odd" if n%2 else "even")\\n'
}

request = function()
  local code = codes[math.random(#codes)]
  local body = string.format(
    '{"userId":%s,"problemId":%s,"language":"PYTHON","code":"%s"}',
    user_id,
    problem_id,
    code
  )
  return wrk.format(nil, "/api/submit", nil, body)
end
