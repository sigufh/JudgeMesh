#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="${EMAIL:-student@judgemesh.local}"
PASSWORD="${PASSWORD:-Student@12345}"

need() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "missing required command: $1" >&2
    exit 127
  }
}

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  if [[ -n "$body" ]]; then
    curl -fsS -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' -d "$body"
  else
    curl -fsS -X "$method" "$BASE_URL$path"
  fi
}

need curl
need node

echo "== health =="
request GET /actuator/health >/dev/null
echo "gateway health ok"

echo "== public problems =="
PROBLEMS="$(request GET /api/problem/list)"
PROBLEM_ID="$(printf '%s' "$PROBLEMS" | node -e 'let s="";process.stdin.on("data",d=>s+=d);process.stdin.on("end",()=>{const r=JSON.parse(s);const p=(r.data||r)[0]; if(!p) process.exit(2); console.log(p.id);});')"
echo "problem id: $PROBLEM_ID"

echo "== login =="
LOGIN_BODY="{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"
LOGIN="$(request POST /api/auth/login "$LOGIN_BODY")"
TOKEN="$(printf '%s' "$LOGIN" | node -e 'let s="";process.stdin.on("data",d=>s+=d);process.stdin.on("end",()=>{const r=JSON.parse(s); console.log((r.data||r).token);});')"
test -n "$TOKEN"
echo "login ok"

echo "== submit =="
SUBMIT_BODY="{\"problemId\":$PROBLEM_ID,\"language\":\"PYTHON\",\"code\":\"a,b=map(int,input().split())\\nprint(a+b)\\n\"}"
curl -fsS -X POST "$BASE_URL/api/submit" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d "$SUBMIT_BODY" >/dev/null
echo "submission accepted"
