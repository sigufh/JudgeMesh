# submit-service

D owns the submit, contest, ranking and WebSocket service. Default port: `8083`.

Full integration guide: [docs/dev/D-提交比赛调度对接文档.md](../../docs/dev/D-提交比赛调度对接文档.md)

## Responsibilities

- Accept code submissions, build `JudgeTask`, and publish to `submit.queue`.
- Persist submissions, contests, contest problems and registrations in `submits_db`.
- Receive worker callbacks at `/api/submit/internal/result` and update submission status.
- Maintain global and contest leaderboards, with Redis ZSet mirroring when Redis is available.
- Push submission status and contest rank changes over WebSocket.
- Expose Prometheus metrics for submit throughput, submit latency and pending submissions.

## Key Endpoints

- `POST /api/submit`
- `GET /api/submit/{id}`
- `GET /api/submit/mine`
- `POST /api/submit/internal/result`
- `POST /api/contest`
- `PUT /api/contest/{id}`
- `GET /api/contest/{id}`
- `GET /api/contest/list`
- `POST /api/contest/{id}/register`
- `GET /api/contest/{id}/rank`
- `GET /api/rank/global`
- `WS /ws/submission/{submissionId}`
- `WS /ws/contest/{contestId}/rank`

## Runtime Notes

- Default MQ queue: `submit.queue`.
- Default callback URL: `http://submit-service:8083/api/submit/internal/result`.
- MySQL is used when a `JdbcTemplate` is available; local smoke tests can still fall back to the in-memory repository.
- `@GlobalTransactional` is present on submit creation. User balance deduction is gated by `judgemesh.submission.deduct-score-enabled` and defaults to `false` until the user-service Seata path is ready.
- Redis keys: `rank:global`, `rank:contest:{contestId}`.

## Metrics

- `oj_submission_total{language,status}`
- `oj_submit_latency_seconds{language,status}`
- `oj_submission_pending`

## Integration Checks

- `X-User-Id` is required for ownership, contest registration and ranking.
- `language` must use wire values: `c`, `cpp`, `java`, `python`.
- `contestId` must refer to a running contest, the user must be registered, and the problem must belong to that contest.
- `JudgeTask` / `JudgeResult` are shared A/D contracts; field changes require both owners to review.
