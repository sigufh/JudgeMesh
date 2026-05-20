#!/usr/bin/env python3
"""Import the JudgeMesh demo problem catalog into problem-service."""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path


OPS = [
    ("A+B", "Read two integers and print their sum.", "math", lambda n: ("1 2\n", "3\n")),
    ("Maximum", "Read three integers and print the maximum.", "branch", lambda n: ("2 7 4\n", "7\n")),
    ("Reverse", "Read a word and print it reversed.", "string", lambda n: ("mesh\n", "hsem\n")),
    ("Parity", "Read one integer and print odd or even.", "branch", lambda n: ("7\n", "odd\n")),
    ("Array Sum", "Read n followed by n integers and print their sum.", "array", lambda n: ("5\n1 2 3 4 5\n", "15\n")),
]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--service", default="http://localhost:8082", help="problem-service base URL")
    parser.add_argument("--dir", default="data/demo-problems", help="directory containing optional problems.json")
    parser.add_argument("--token", default="", help="optional Bearer token")
    parser.add_argument("--setter-id", type=int, default=1001)
    parser.add_argument("--limit", type=int, default=50)
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--skip-existing", action="store_true", default=True)
    args = parser.parse_args()

    problems = load_catalog(Path(args.dir), args.limit, args.setter_id)
    existing = existing_titles(args.service, args.token) if args.skip_existing and not args.dry_run else set()
    imported = 0
    skipped = 0
    for problem in problems:
        if problem["title"] in existing:
            skipped += 1
            continue
        if args.dry_run:
            print(json.dumps(problem, ensure_ascii=False))
        else:
            created = post_json(args.service.rstrip("/") + "/api/problem", problem, args.token)
            problem_id = (created.get("data") or created).get("id")
            print(f"imported {problem_id}: {problem['title']}")
        imported += 1
    print(f"done imported={imported} skipped={skipped}")
    return 0


def load_catalog(root: Path, limit: int, setter_id: int) -> list[dict]:
    catalog = root / "problems.json"
    if catalog.exists():
        data = json.loads(catalog.read_text(encoding="utf-8"))
        return data[:limit]
    return [generated_problem(i, setter_id) for i in range(1, limit + 1)]


def generated_problem(i: int, setter_id: int) -> dict:
    name, desc, tag, case = OPS[(i - 1) % len(OPS)]
    input_text, output_text = case(i)
    difficulty = "EASY" if i <= 25 else "MEDIUM" if i <= 45 else "HARD"
    time_limit = 1000 if difficulty == "EASY" else 1500 if difficulty == "MEDIUM" else 2500
    return {
        "title": f"Demo {i:02d} - {name}",
        "description": f"## {name}\n\n{desc}\n\nThis deterministic seed problem is used for local demos, smoke tests, and load tests.",
        "timeLimitMs": time_limit,
        "memoryLimitMb": 256,
        "difficulty": difficulty,
        "setterId": setter_id,
        "published": True,
        "tags": ["demo", tag, difficulty.lower()],
        "testCases": [
            {"caseIndex": 1, "input": input_text, "expectedOutput": output_text, "score": 50},
            {"caseIndex": 2, "input": variant_input(name), "expectedOutput": variant_output(name), "score": 50},
        ],
    }


def variant_input(name: str) -> str:
    return {
        "A+B": "10 32\n",
        "Maximum": "-1 -5 -3\n",
        "Reverse": "judge\n",
        "Parity": "8\n",
        "Array Sum": "4\n9 8 7 6\n",
    }[name]


def variant_output(name: str) -> str:
    return {
        "A+B": "42\n",
        "Maximum": "-1\n",
        "Reverse": "egduj\n",
        "Parity": "even\n",
        "Array Sum": "30\n",
    }[name]


def existing_titles(service: str, token: str) -> set[str]:
    params = urllib.parse.urlencode({"includeDraft": "true", "size": "500"})
    try:
        response = get_json(service.rstrip("/") + "/api/problem/list?" + params, token)
    except urllib.error.URLError:
        return set()
    rows = response.get("data") or response
    return {row.get("title", "") for row in rows}


def get_json(url: str, token: str) -> dict:
    request = urllib.request.Request(url, headers=headers(token))
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.loads(response.read().decode("utf-8"))


def post_json(url: str, payload: dict, token: str) -> dict:
    body = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(url, data=body, method="POST", headers=headers(token))
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        sys.stderr.write(exc.read().decode("utf-8") + "\n")
        raise


def headers(token: str) -> dict[str, str]:
    result = {"Content-Type": "application/json"}
    if token:
        result["Authorization"] = "Bearer " + token
    return result


if __name__ == "__main__":
    raise SystemExit(main())
