# JudgeMesh 指标与日志规范

本文档是 B/C/D 各服务接入 Prometheus、Grafana、SkyWalking、Loki 的统一规范。目标不是解释概念，而是直接约束“指标叫什么、标签怎么打、日志怎么写、哪些必须接”。

## 1. 基本约定

- 指标前缀统一为 `oj_`。
- 指标名统一使用英文小写加下划线。
- 标签只允许低基数维度: `service`、`language`、`status`、`queue`、`worker`、`result`、`endpoint`。
- 禁止将 `userId`、`submissionId`、`traceId`、IP、题目标题等高基数字段作为指标标签。
- Java 服务统一暴露 `/actuator/prometheus`。
- Go worker 统一暴露 `/metrics`。
- 所有生产 Deployment 必须带 scrape annotation、readinessProbe、livenessProbe。

## 2. 指标类型约束

- Counter: 只表示累计事件数，如请求总量、重试总量、失败总量。
- Gauge: 只表示瞬时值，如排队数、当前并发、leader 状态。
- Timer/Histogram: 只表示时延或体积分布，单位统一使用 seconds 或 bytes。
- 百分比不要直接做 Gauge；应先上报分子/分母，再在 PromQL 中计算。

## 3. 各服务必备指标

### gateway

- `oj_http_server_requests_total{service="gateway",endpoint,status}`
- `oj_http_server_request_duration_seconds{service="gateway",endpoint}`
- `oj_gateway_auth_fail_total{reason}`
- `oj_gateway_rate_limit_total{route,result}`

### user-service / problem-service

- `oj_http_server_requests_total{service,endpoint,status}`
- `oj_http_server_request_duration_seconds{service,endpoint}`
- `oj_dependency_call_duration_seconds{service,target,operation}`
- `oj_business_error_total{service,code}`

### submit-service

- `oj_submission_total{language,status}`
- `oj_submission_pending{queue}`
- `oj_submit_pipeline_duration_seconds{stage}`
- `oj_ws_push_total{channel,result}`
- `oj_mq_publish_total{exchange,result}`

### judge-dispatcher

- `oj_dispatch_total{worker,result}`
- `oj_dispatch_retry_total{reason}`
- `oj_dispatch_worker_blacklist_total{worker,reason}`
- `oj_dispatch_leader{pod}`
- `oj_dispatch_etcd_lease_remaining_seconds{pod}`

### judge-worker

- `oj_judge_total{language,verdict}`
- `oj_judge_duration_seconds{language}`
- `oj_judge_inflight`
- `oj_judge_sandbox_fail_total{stage}`
- `oj_worker_pull_artifact_duration_seconds{artifact}`

## 4. 命名与标签示例

Java:

```java
Counter.builder("oj_submission_total")
    .tag("language", language)
    .tag("status", status)
    .register(registry)
    .increment();
```

Go:

```go
promauto.NewHistogramVec(
    prometheus.HistogramOpts{
        Name: "oj_judge_duration_seconds",
        Buckets: []float64{0.1, 0.5, 1, 2, 5, 10, 30},
    },
    []string{"language"},
)
```

约束:

- `status` / `result` / `verdict` 使用稳定枚举值，例如 `accepted`、`failed`、`timeout`。
- `endpoint` 记录模板路由，如 `/api/submit/{id}`，不要记录原始 URL。
- `worker` 允许记录 pod 名，因为 judge-worker 副本数有限。

## 5. 日志规范

- 日志统一输出 JSON。
- 必备字段: `timestamp`、`level`、`service`、`traceId`。
- 条件字段: `userId`、`submissionId`、`contestId` 仅在业务上下文存在时填写。
- 禁止打印密码、JWT、数据库 DSN、AK/SK、完整请求体。
- 单条日志建议控制在 8KB 内。

建议结构:

```json
{
  "timestamp": "2026-05-13T12:00:00Z",
  "level": "INFO",
  "service": "submit-service",
  "traceId": "trace-123",
  "submissionId": 42,
  "message": "submission accepted"
}
```

## 6. Trace 规范

- 所有 HTTP 入站请求必须保留或生成 `traceId`。
- 网关、submit-service、judge-dispatcher、回写接口必须能在日志里打印同一条链路的 `traceId`。
- 异步 MQ 场景至少要把 `traceId` 放进 message header 或业务 payload 扩展字段，避免链路断裂。
- Java 服务默认走 SkyWalking agent；Go worker 至少保证日志里可关联上游 traceId。

## 7. Grafana 看板口径

- `oj-live.json`: QPS、AC 率、判题 P95/P99、当前 worker 数、当前 leader、队列积压。
- `judge-pipeline.json`: submit -> MQ -> dispatcher -> worker -> callback 各阶段吞吐/时延。
- `governance.json`: 网关请求、错误率、熔断/限流、配置中心与注册中心可用性。
- `infra.json`: 节点资源、Pod 重启、PVC 使用率、RabbitMQ/Redis/etcd/Nacos 健康度。

## 8. 接入验收

每个服务合并前至少满足:

- `/actuator/prometheus` 或 `/metrics` 可被 Prometheus 抓取。
- 至少 1 个业务 Counter + 1 个时延 Histogram/Timer 已接入。
- 日志里能搜到 `service` 和 `traceId`。
- 大盘里出现该服务对应的数据曲线，不接受“代码已埋点但 Prometheus 没抓到”的状态。
