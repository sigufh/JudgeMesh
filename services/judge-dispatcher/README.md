# judge-dispatcher

D owns the judge dispatcher. Default port: `8084`.

Full integration guide: [docs/dev/D-提交比赛调度对接文档.md](../../docs/dev/D-提交比赛调度对接文档.md)

## Responsibilities

- Consume `JudgeTask` messages from `submit.queue`.
- Elect a single active dispatcher leader in `etcd` mode.
- Select healthy workers, forward tasks to `POST /judge`, and track inflight counts.
- Requeue failed dispatches with incremented `retry_count`.
- Move tasks to `submit.queue.dlq` after max retry, and move malformed payloads directly to DLQ.
- Temporarily blacklist failed workers.
- Expose admin status and Prometheus metrics.

## Modes

- `memory`: local bootstrap mode; the process acts as leader.
- `etcd`: production mode; uses jetcd election under `/judgemesh/dispatcher/leader`.

```yaml
judgemesh:
  dispatcher:
    mode: etcd
```

## Key Endpoints

- `GET /admin/dispatcher/status`
- `POST /admin/dispatcher/chaos/kill-self`

## Queue Contract

- Consume queue: `submit.queue`.
- Dead-letter queue: `submit.queue.dlq`.
- Message body: `JudgeTask`.
- Only the leader should consume and dispatch tasks.

## Worker Selection

- Filters out temporarily blacklisted workers.
- Sorts candidates by current inflight count.
- Calls `GET {worker}/health`.
- Forwards task with `POST {worker}/judge`.
- Releases inflight after `judgemesh.dispatcher.worker.timeout-seconds`.

## Metrics

- `oj_dispatch_total{worker,result}`
- `oj_dispatch_retry_total{outcome}`
- `oj_dispatcher_is_leader`

## Integration Checks

- `submit.queue` must match submit-service config exactly.
- Worker health path must return 2xx.
- Worker endpoint list must match deployment addresses.
- `JudgeTask` / `JudgeResult` are shared A/D contracts; field changes require both owners to review.
