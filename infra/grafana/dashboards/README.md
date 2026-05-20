# Grafana 大盘

当前目录包含 4 份 JSON(对齐 docs/design/07-可观测性.md):

- `oj-live.json` — 业务总览(QPS、用户在线、提交速率)
- `judge-pipeline.json` — 判题流水线(MQ 队列长度、worker 利用率、判题耗时)
- `governance.json` — 服务治理(限流命中、熔断状态、Seata 事务)
- `infra.json` — 基础设施(节点 CPU/Mem、Pod 状态、Redis/MQ 连接)

部署方式:打 `grafana_dashboard=1` label 的 ConfigMap,Grafana sidecar 自动加载(见 ../helm/values/prometheus.yaml)。
