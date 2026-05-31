# JudgeMesh Metrics, Logs, and Traces Conventions

This document is the contract for service owners integrating with Prometheus,
Grafana, SkyWalking, and Loki. It defines metric names, allowed labels, log
fields, trace propagation, and acceptance criteria.

## 1. Global Rules

- Metric names owned by JudgeMesh must use the `oj_` prefix.
- Metric names use lower-case English words separated by `_`.
- Java services expose Prometheus metrics at `/actuator/prometheus`.
- The Go judge worker exposes Prometheus metrics at `/metrics`.
- All production Deployments must have readiness probes, liveness probes, and
  Prometheus scrape coverage through PodMonitor or ServiceMonitor.
- Do not put high-cardinality values in metric labels.

Allowed low-cardinality labels:

- `service`
- `application`
- `language`
- `status`
- `queue`
- `worker`
- `result`
- `verdict`
- `endpoint`
- `uri`
- `stage`
- `reason`

Forbidden metric labels:

- `userId`
- `submissionId`
- `traceId`
- IP address
- raw URL
- source code
- problem title
- request body

## 2. Metric Types

- Use Counter for accumulated events such as requests, submissions, retries,
  failures, and judge outcomes.
- Use Gauge for instantaneous state such as queue depth, in-flight work, worker
  availability, and leader state.
- Use Histogram or Timer for latency distributions. Time units must be seconds.
- Use bytes for memory or payload sizes.
- Do not publish a percentage as a Gauge when it can be computed from numerator
  and denominator counters in PromQL.

## 3. Required Service Metrics

### gateway

- `http_server_requests_seconds_count{application="gateway",uri,status}`
- `http_server_requests_seconds_bucket{application="gateway",uri,status,le}`
- `spring_cloud_gateway_requests_seconds_count{routeId,outcome}`
- `oj_gateway_auth_fail_total{reason}`
- `oj_gateway_rate_limit_total{route,result}`

### user-service

- `http_server_requests_seconds_count{application="user-service",uri,status}`
- `http_server_requests_seconds_bucket{application="user-service",uri,status,le}`
- `oj_business_error_total{service="user-service",code}`

### problem-service

- `http_server_requests_seconds_count{application="problem-service",uri,status}`
- `http_server_requests_seconds_bucket{application="problem-service",uri,status,le}`
- `oj_dependency_call_duration_seconds{service="problem-service",target,operation}`
- `oj_business_error_total{service="problem-service",code}`

### submit-service

- `http_server_requests_seconds_count{application="submit-service",uri,status}`
- `oj_submission_total{language,status}`
- `oj_submission_pending{queue}`
- `oj_submit_pipeline_duration_seconds{stage}`
- `oj_ws_push_total{channel,result}`
- `oj_mq_publish_total{exchange,result}`

### judge-dispatcher

- `http_server_requests_seconds_count{application="judge-dispatcher",uri,status}`
- `oj_dispatch_total{worker,result}`
- `oj_dispatch_retry_total{reason}`
- `oj_dispatch_worker_blacklist_total{worker,reason}`
- `oj_dispatch_leader{pod}`
- `oj_dispatch_etcd_lease_remaining_seconds{pod}`

### judge-worker

- `oj_worker_up`
- `oj_judge_total{language,verdict}`
- `oj_judge_duration_seconds_bucket{language,le}`
- `oj_judge_inflight`
- `oj_judge_sandbox_fail_total{stage}`
- `oj_worker_pull_artifact_duration_seconds_bucket{artifact,le}`

## 4. Middleware Metrics

Prometheus must scrape these middleware targets in the Kubernetes demo:

- RabbitMQ: `rabbitmq_queue_messages_ready`, `rabbitmq_queue_messages_unacked`,
  `rabbitmq_up`
- Redis exporter: `redis_up`, `redis_memory_used_bytes`
- etcd: `etcd_server_has_leader`, `etcd_server_leader_changes_seen_total`
- Nacos: `/nacos/actuator/prometheus`
- Kubernetes: `kube_pod_container_status_restarts_total`,
  `container_cpu_usage_seconds_total`, `container_memory_working_set_bytes`

## 5. Java Example

```java
Counter.builder("oj_submission_total")
    .tag("language", language)
    .tag("status", status)
    .register(registry)
    .increment();
```

```java
Timer.builder("oj_submit_pipeline_duration_seconds")
    .tag("stage", "publish_mq")
    .register(registry)
    .record(duration);
```

## 6. Go Example

```go
promauto.NewCounterVec(
    prometheus.CounterOpts{
        Name: "oj_judge_total",
        Help: "Judge tasks handled by language and final verdict.",
    },
    []string{"language", "verdict"},
)
```

```go
promauto.NewHistogramVec(
    prometheus.HistogramOpts{
        Name: "oj_judge_duration_seconds",
        Buckets: []float64{0.1, 0.5, 1, 2, 5, 10, 30},
    },
    []string{"language"},
)
```

## 7. Label Value Rules

- `status`, `result`, and `verdict` must use stable enum values such as `ac`,
  `wa`, `tle`, `re`, `ce`, `se`, `success`, `failed`, and `timeout`.
- `endpoint` or `uri` must be a route template such as `/api/submit/{id}`, not a
  raw URL with IDs.
- `worker` may be the pod name because judge-worker replica count is bounded.
- Normalize label values to lower case unless they come from Spring/Micrometer.

## 8. Log Rules

All services should write structured JSON logs.

Required fields:

- `timestamp`
- `level`
- `service`
- `traceId`
- `message`

Conditional fields:

- `userId`
- `submissionId`
- `contestId`
- `problemId`
- `workerId`

Never log:

- password
- JWT
- database DSN with credentials
- access key or secret key
- full request body
- source code

Keep a single log event below 8 KB unless explicitly debugging locally.

Example:

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

## 9. Trace Rules

- Every inbound HTTP request must preserve an existing trace ID or generate one.
- gateway, submit-service, judge-dispatcher, judge-worker, and callback handling
  must log the same trace ID for a single submission path.
- MQ messages must carry trace context in headers or payload extension fields.
- Java services should run with the SkyWalking agent in cluster demos.
- Go worker must at least include trace context in logs and callback metadata.

## 10. Grafana Dashboard Acceptance

The four required dashboards are:

- `oj-live.json`: QPS, submit rate, accepted ratio, judge p95/p99, workers,
  current in-flight work, queue backlog, and error rate.
- `judge-pipeline.json`: submit API, RabbitMQ queue, dispatcher availability,
  worker availability, judge verdicts, and judge latency.
- `governance.json`: HTTP errors, route latency, gateway route metrics, Nacos,
  etcd, dispatcher health, JVM heap, and restarts.
- `infra.json`: node and pod resources, PVC usage, restarts, pending pods,
  RabbitMQ, Redis, and etcd health.

Dashboards are loaded by ConfigMaps with label `grafana_dashboard=1`.

## 11. Pull Request Acceptance

A service change that claims observability support must satisfy all checks:

- `/actuator/prometheus` or `/metrics` can be scraped by Prometheus.
- At least one business Counter and one latency Histogram/Timer are present.
- Logs include `service` and `traceId`.
- No high-cardinality labels are introduced.
- The relevant dashboard panel shows data after a demo smoke run.
- Empty dashboards or "metrics exist in code but Prometheus cannot scrape them"
  are not accepted.
