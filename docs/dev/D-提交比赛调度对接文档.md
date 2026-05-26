# D 模块对接文档: submit-service + judge-dispatcher

维护人: `@Nier291`  
适用对象: 前端、judge-worker、gateway、user-service、problem-service、基础设施/压测同学  
代码范围: `services/submit-service/`, `services/judge-dispatcher/`, `services/api/.../message/`

## 1. 服务边界

| 服务 | 端口 | 主要职责 | 依赖 |
| ---- | ---- | -------- | ---- |
| `submit-service` | `8083` | 提交、比赛、排行榜、WebSocket、判题回调、Seata 提交事务入口 | MySQL, Redis, RabbitMQ, problem-service, user-service |
| `judge-dispatcher` | `8084` | 消费 `submit.queue`、etcd 选主、worker 选择、任务派发、重试/DLQ | RabbitMQ, etcd, judge-worker |

核心链路:

```text
frontend/gateway
  -> POST /api/submit
  -> submit-service 写 submission
  -> RabbitMQ submit.queue
  -> judge-dispatcher leader
  -> POST {worker}/judge
  -> worker POST /api/submit/internal/result
  -> submit-service 更新状态、排行榜、WebSocket 推送
```

## 2. 前端 / Gateway 对接

所有 HTTP 响应统一包在 `ApiResponse<T>`:

```json
{
  "code": "0",
  "message": "OK",
  "data": {},
  "timestamp": "2026-05-15T03:00:00Z"
}
```

用户身份从请求头读取:

```http
X-User-Id: 1001
```

当前网关未接入时，服务默认 `X-User-Id=1`，仅用于本地调试。

### 2.1 创建提交

```http
POST /api/submit
Content-Type: application/json
X-User-Id: 1001
```

```json
{
  "problemId": 1,
  "contestId": null,
  "language": "cpp",
  "code": "#include <bits/stdc++.h>\nusing namespace std;\nint main(){cout<<\"OK\\n\";}",
  "timeLimitMs": 1000,
  "memoryLimitMb": 256,
  "testcaseManifestUrl": "http://problem-service/api/problems/1/testcase/manifest"
}
```

字段约定:

| 字段 | 必填 | 说明 |
| ---- | ---- | ---- |
| `problemId` | 是 | 题目 ID |
| `contestId` | 否 | 为空表示普通提交；非空表示比赛提交 |
| `language` | 是 | `c`, `cpp`, `java`, `python` |
| `code` | 是 | 源码；长度上限由 `judgemesh.submission.code-length-limit` 控制 |
| `timeLimitMs` | 否 | 本地调试覆盖值；正常由 problem-service 提供 |
| `memoryLimitMb` | 否 | 本地调试覆盖值；正常由 problem-service 提供 |
| `testcaseManifestUrl` | 否 | 本地调试覆盖值；正常由 problem-service 提供 |

比赛提交额外校验:

- 比赛必须已开始且未结束。
- 用户必须已经报名。
- 题目必须属于该比赛。

### 2.2 查询提交

```http
GET /api/submit/{id}
GET /api/submit/mine
```

`SubmissionDTO` 关键字段:

```json
{
  "id": 1000001,
  "userId": 1001,
  "problemId": 1,
  "contestId": null,
  "language": "cpp",
  "status": "pending",
  "score": 0,
  "timeUsedMs": null,
  "memoryUsedKb": null,
  "judgeMessage": null,
  "judgedByWorker": null,
  "submittedAt": "2026-05-15T03:00:00Z",
  "judgedAt": null
}
```

状态枚举:

```text
pending, judging, ac, wa, tle, mle, re, ce, se
```

### 2.3 比赛接口

```http
POST /api/contest
PUT /api/contest/{id}
GET /api/contest/{id}
GET /api/contest/list
POST /api/contest/{id}/register
GET /api/contest/{id}/rank
```

创建/更新比赛:

```json
{
  "title": "Spring Sprint Contest",
  "description": "demo contest",
  "startTime": "2026-05-15T10:00:00Z",
  "endTime": "2026-05-15T12:00:00Z",
  "freezeBeforeMin": 30,
  "problemIds": [1, 2, 3]
}
```

比赛状态:

```text
upcoming, running, frozen, ended
```

排行榜响应:

```json
{
  "contestId": 100001,
  "status": "running",
  "frozen": false,
  "frozenAt": "2026-05-15T11:30:00Z",
  "entries": [
    {
      "rank": 1,
      "userId": 1001,
      "solved": 2,
      "penaltyMinutes": 45,
      "score": 199955,
      "lastAcceptedAt": "2026-05-15T10:40:00Z"
    }
  ]
}
```

### 2.4 WebSocket

提交状态:

```text
ws://{host}:8083/ws/submission/{submissionId}
```

比赛榜:

```text
ws://{host}:8083/ws/contest/{contestId}/rank
```

连接建立后服务端会立即推一次当前全量 DTO。断线重连时前端重新订阅同一个 `submissionId` 或 `contestId` 即可补全状态。

## 3. Worker 对接

### 3.1 Dispatcher -> Worker

```http
POST /judge
Content-Type: application/json
```

消息体来自 `JudgeTask`:

```json
{
  "submit_id": 1000001,
  "problem_id": 1,
  "source": "#include <bits/stdc++.h>\nint main(){return 0;}",
  "language": "cpp",
  "time_limit_ms": 1000,
  "memory_limit_mb": 256,
  "testcase_manifest_url": "http://problem-service/api/problems/1/testcase/manifest",
  "testcases": [],
  "callback_url": "http://submit-service:8083/api/submit/internal/result",
  "retry_count": 0
}
```

Worker 必须提供健康检查:

```http
GET /health
```

返回任意 `2xx` 即视为健康。

### 3.2 Worker -> submit-service 回调

```http
POST /api/submit/internal/result
Content-Type: application/json
```

消息体来自 `JudgeResult`:

```json
{
  "submit_id": 1000001,
  "status": "ac",
  "message": "Accepted",
  "cases": [
    {
      "name": "sample-1",
      "status": "ac",
      "time_ms": 12,
      "memory_kb": 2048,
      "stderr": ""
    }
  ],
  "time_used_ms": 12,
  "memory_used_kb": 2048,
  "worker_id": "judge-worker-0",
  "worker_version": "dev"
}
```

回调注意:

- `submit_id` 必填。
- `status` 必须使用小写 wire value: `ac`, `wa`, `tle`, `mle`, `re`, `ce`, `se`。
- submit-service 以回调状态更新 DB、排行榜和 WebSocket。

## 4. RabbitMQ 契约

| 队列 | 生产者 | 消费者 | 内容 |
| ---- | ------ | ------ | ---- |
| `submit.queue` | submit-service / dispatcher retry | judge-dispatcher leader | `JudgeTask` JSON |
| `submit.queue.dlq` | judge-dispatcher | 运维排查 | 达到最大重试或非法 payload |

Dispatcher 行为:

- 只有 leader 节点派发任务。
- worker 健康检查失败或 `/judge` 调用失败时，worker 进入短时黑名单。
- 失败任务 `retry_count + 1` 后重新投递 `submit.queue`。
- `retry_count >= max-retry` 后投递 `submit.queue.dlq`。
- 非法 JSON 直接投递 `submit.queue.dlq`。

## 5. 配置项

### submit-service

| 配置 / 环境变量 | 默认值 | 说明 |
| --------------- | ------ | ---- |
| `MYSQL_HOST` | `127.0.0.1` | `submits_db` MySQL 地址 |
| `REDIS_HOST` | `127.0.0.1` | 排行榜镜像和提交去重 |
| `RABBITMQ_HOST` | `127.0.0.1` | `submit.queue` |
| `judgemesh.mq.queue` | `submit.queue` | 判题任务队列 |
| `judgemesh.submission.callback-url` | `http://submit-service:8083/api/submit/internal/result` | worker 回调地址 |
| `SUBMIT_DEDUCT_SCORE_ENABLED` | `false` | 是否调用 user-service 扣分并进入 Seata 演示链路 |
| `SUBMIT_COST` | `1` | 每次提交扣分值 |

### judge-dispatcher

| 配置 / 环境变量 | 默认值 | 说明 |
| --------------- | ------ | ---- |
| `DISPATCHER_MODE` | `memory` | `memory` 本地模式，`etcd` 多副本选主 |
| `ETCD_ENDPOINTS` | `http://127.0.0.1:2379` | etcd 地址，多个地址用逗号/空格分隔 |
| `RABBITMQ_HOST` | `127.0.0.1` | RabbitMQ 地址 |
| `judgemesh.dispatcher.worker.timeout-seconds` | `30` | worker 请求读超时和 inflight 释放时间 |
| `judgemesh.dispatcher.worker.max-retry` | `3` | 最大重试次数 |
| `judgemesh.dispatcher.worker.blacklist-seconds` | `30` | 失败 worker 黑名单时长 |
| `judgemesh.dispatcher.worker.endpoints` | `http://judge-worker:8090` | worker 地址列表 |
| `judgemesh.dispatcher.mq.submit-queue` | `submit.queue` | 消费队列 |
| `judgemesh.dispatcher.mq.dead-letter-queue` | `submit.queue.dlq` | 死信队列 |

## 6. 部署对接

K8s base 已提供:

```text
infra/k8s/base/submit-service
infra/k8s/base/judge-dispatcher
```

dev/prod overlay 已包含:

```text
infra/k8s/overlays/dev/kustomization.yaml
infra/k8s/overlays/prod/kustomization.yaml
```

生产建议:

- `submit-service` 副本数至少 2。
- `judge-dispatcher` 副本数至少 3，并设置 `DISPATCHER_MODE=etcd`。
- worker Service 名称和端口要与 dispatcher 的 `worker.endpoints` 一致。
- RabbitMQ、MySQL、Redis、etcd 的 Service DNS 要与环境变量一致。

## 7. 指标对接

submit-service:

```text
oj_submission_total{language,status}
oj_submit_latency_seconds{language,status}
oj_submission_pending
```

judge-dispatcher:

```text
oj_dispatch_total{worker,result}
oj_dispatch_retry_total{outcome}
oj_dispatcher_is_leader
```

Prometheus scrape:

```text
/actuator/prometheus
```

建议 Grafana 面板:

- 提交 QPS: `rate(oj_submission_total[1m])`
- 判题完成延迟 P95/P99: `histogram_quantile(...)`，按实际 Micrometer bucket 指标展开。
- dispatcher 是否 leader: `oj_dispatcher_is_leader`
- dispatcher 重试速率: `rate(oj_dispatch_retry_total[1m])`
- pending 堆积: `oj_submission_pending`

## 8. Seata 对接

submit-service 已在 `SubmissionService.submit()` 上加 `@GlobalTransactional(name = "submit-create")`。

默认不开启扣分:

```yaml
judgemesh:
  submission:
    deduct-score-enabled: false
```

完整演示需要:

- user-service 实现 `POST /api/users/deduct?userId={id}&amount={amount}`。
- user-service 分支事务可用，并有 `undo_log`。
- submit-service 和 user-service 指向同一个 Seata Server / tx-service-group。
- 开启 `SUBMIT_DEDUCT_SCORE_ENABLED=true`。

## 9. 压测

脚本:

```text
scripts/loadtest-submit.lua
```

本地提交服务压测:

```bash
wrk -t4 -c50 -d5m -s scripts/loadtest-submit.lua http://127.0.0.1:8083
```

指定用户/题目/语言:

```bash
USER_ID=1001 PROBLEM_ID=1 LANGUAGE=cpp wrk -t4 -c50 -d5m -s scripts/loadtest-submit.lua http://127.0.0.1:8083
```

比赛提交:

```bash
USER_ID=1001 PROBLEM_ID=1 CONTEST_ID=100001 LANGUAGE=cpp wrk -t4 -c50 -d5m -s scripts/loadtest-submit.lua http://127.0.0.1:8083
```

## 10. 联调检查清单

- 前端提交时带 `X-User-Id`。
- 前端 WebSocket 断线后按 `submissionId` / `contestId` 重新订阅。
- problem-service 返回题目时包含时间、内存、manifest URL；否则 submit-service 会用本地默认值兜底。
- worker `/health` 返回 `2xx`。
- worker `/judge` 接收 `JudgeTask` JSON 字段名使用 snake_case。
- worker 回调 `status` 使用小写 wire value。
- RabbitMQ 中 `submit.queue` 和 `submit.queue.dlq` 可见。
- 多副本 dispatcher 在 `etcd` 模式下只有一个 leader。
- Prometheus 能抓到两个服务的 `/actuator/prometheus`。
