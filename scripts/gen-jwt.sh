#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="${EMAIL:-student@judgemesh.local}"
PASSWORD="${PASSWORD:-Student@12345}"

command -v curl >/dev/null 2>&1 || { echo "missing curl" >&2; exit 127; }
command -v node >/dev/null 2>&1 || { echo "missing node" >&2; exit 127; }

body="{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"
response="$(curl -fsS -X POST "$BASE_URL/api/auth/login" -H 'Content-Type: application/json' -d "$body")"
printf '%s' "$response" | node -e '
let s="";
process.stdin.on("data", d => s += d);
process.stdin.on("end", () => {
  const r = JSON.parse(s);
  const token = (r.data || r).token;
  if (!token) process.exit(2);
  console.log(token);
});
'
