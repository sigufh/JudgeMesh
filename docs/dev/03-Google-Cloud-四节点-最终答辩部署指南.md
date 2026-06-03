# Google Cloud 四节点最终答辩部署指南

本文档对应最终答辩推荐入口：

- [infra/k8s/overlays/final-defense](/D:/JudgeMesh/infra/k8s/overlays/final-defense/kustomization.yaml:1)
- [Google Cloud CLI 命令清单](/D:/JudgeMesh/docs/dev/04-Google-Cloud-CLI-命令清单.md:1)

这不是再做一套新的大改架构，而是把当前仓库收敛成一套最稳妥的答辩版组织方式。

## 1. 结论先行

最终答辩建议固定使用下面这套组织方式：

- `2` 台 `app` 节点
- `2` 台 `judge` 节点
- `judge-worker` 保持 `1 Pod = 1 并发`
- 高峰期通过 `KEDA` 在 `judge` 节点上横向加 `worker Pod`
- `gateway / user-service / problem-service / submit-service / frontend` 做双副本
- `judge-dispatcher` 固定 `3` 副本，用 `etcd` 选主
- `MySQL / Redis / RabbitMQ / MinIO / Nacos` 保持本期单实例，但必须配合持久化、备份、DLQ 和恢复预案

这样做的原因：

- 和需求书一致。本期本来就允许中间件单实例，重点是分布式判题、弹性、熔断降级和可观测性。
- 比临时把所有中间件强行改成未验证集群更稳。
- 对四节点预算最友好，答辩展示效果也最好。

## 2. 现在这个集群要不要重配

你当前已经有运行中的 GKE 集群：

- 集群名：`judgemesh-gke`
- 区域：`asia-east1-b`

因此现在不需要重新执行 `gcloud init`，也不需要重建集群。

只需要做下面几件事：

1. 获取集群凭据
2. 检查并补节点标签/污点
3. 检查 KEDA 是否已安装
4. 应用最终答辩 overlay

## 3. 你自己执行的 gcloud / kubectl 命令

### 3.1 连接现有集群

```bash
gcloud container clusters get-credentials judgemesh-gke --zone asia-east1-b
kubectl get nodes -o wide
```

### 3.2 规划节点角色

先看节点名：

```bash
kubectl get nodes
```

然后手工指定：

- 任选 `2` 台做 `app`
- 剩下 `2` 台做 `judge`

执行：

```bash
kubectl label node APP_NODE_1 role=app --overwrite
kubectl label node APP_NODE_2 role=app --overwrite
kubectl label node JUDGE_NODE_1 role=judge --overwrite
kubectl label node JUDGE_NODE_2 role=judge --overwrite

kubectl taint node JUDGE_NODE_1 role=judge:NoSchedule --overwrite
kubectl taint node JUDGE_NODE_2 role=judge:NoSchedule --overwrite
```

检查结果：

```bash
kubectl get nodes --show-labels
```

### 3.3 安装或检查 KEDA

```bash
kubectl get crd scaledobjects.keda.sh
kubectl get pods -n keda
```

如果还没安装：

```bash
scripts/install-keda.sh
```

### 3.4 应用最终答辩版

```bash
kubectl kustomize infra/k8s/overlays/final-defense > NUL
kubectl apply -k infra/k8s/overlays/final-defense
```

检查：

```bash
kubectl get pods -A -o wide
kubectl get hpa -n judgemesh
kubectl get scaledobject -n judgemesh
kubectl get pdb -A
```

## 4. worker 到底要不要一台节点多个实例

最终建议是：

- 默认保持 `JUDGE_MAX_CONCURRENCY=1`
- 优先通过“多 Pod 横向扩容”解决并发，而不是一个 Pod 内开很多并发
- 当只有 `2` 台 `judge` 节点时，允许一台节点同时跑多个 `worker Pod`

原因很直接：

- 判题进程带沙箱、编译和运行负载，单 Pod 单并发最稳。
- 同节点多 Pod 比单 Pod 多并发更容易限额、观测和恢复。
- KEDA 已经接入，队列堆积时会自动拉起更多 worker。

当前仓库里已经补上的动态扩容机制：

- `judge-worker` 使用 `KEDA ScaledObject`
- 最小副本：`3`
- 最大副本：`8`
- 触发条件：`submit.queue` 队列长度达到阈值
- Dispatcher 已支持基于 headless service 的 worker 动态发现

所以结论是：

- 有动态扩容机制
- 也支持一台节点多个 worker Pod
- 不建议把 `JUDGE_MAX_CONCURRENCY` 调大作为主扩容手段

## 5. 现在的熔断降级策略是否够用

目前答辩链路上已经补到可以讲清楚：

1. `submit-service`
   - RabbitMQ 不可用时，支持直接走 dispatcher 内部派发降级路径
   - direct dispatch 失败时会返回明确 `503`

2. `judge-worker`
   - 单 Pod 达到并发上限时，`/judge` 返回 `503 worker saturated`
   - 健康接口会暴露 `inflight` 和 `maxConcurrency`

3. `judge-dispatcher`
   - worker 失败会短时黑名单
   - 支持重试和 DLQ
   - 支持多副本选主
   - 支持 worker 动态发现

4. `gateway`
   - 已改成 Redis 共享限流
   - Redis 不可用时回退到本地限流

对于最终答辩，这套降级链路已经够用。接下来不要再做高风险大改，重点是把演示和文档讲顺。

## 6. 压测怎么做

答辩时建议分三层：

### 6.1 功能冒烟

```bash
BASE_URL=http://EXTERNAL_IP scripts/smoke-test.sh
```

### 6.2 submit 接口持续压测

先取 JWT：

```bash
TOKEN="$(BASE_URL=http://EXTERNAL_IP scripts/gen-jwt.sh)"
```

再压：

```bash
TOKEN="$TOKEN" PROBLEM_ID=1 USER_ID=1002 wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
```

建议答辩准备三档：

```bash
TOKEN="$TOKEN" PROBLEM_ID=1 USER_ID=1002 wrk -t2 -c16 -d30s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
TOKEN="$TOKEN" PROBLEM_ID=1 USER_ID=1002 wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
TOKEN="$TOKEN" PROBLEM_ID=1 USER_ID=1002 wrk -t6 -c50 -d120s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
```

### 6.3 分布式判题演示

先转发 dispatcher：

```bash
kubectl -n judgemesh port-forward deploy/judge-dispatcher 8084:8084
```

再跑：

```bash
BASE_URL=http://EXTERNAL_IP DISPATCHER_URL=http://127.0.0.1:8084 node scripts/demo-distributed-load.mjs --total 60 --concurrency 30
```

答辩时重点展示：

- `workerCounts`
- `statusCounts`
- `latencyMs p50 / p95 / max`
- `kubectl get pods -n judgemesh -o wide`
- `kubectl get hpa -n judgemesh`
- `kubectl get scaledobject -n judgemesh`

## 7. 还要补考虑哪些问题

最终答辩必须主动提这几项，不然老师一问就会暴露：

1. 备份恢复
   - MySQL 每日备份到 MinIO
   - Redis 排行榜可从 MySQL submission 重建
   - MinIO 测试用例和头像目录要持久化

2. 失败任务治理
   - RabbitMQ DLQ 需要人工检查和重放流程

3. 配置治理
   - Nacos 关键配置要能导出和回滚

4. Secret 治理
   - 仓库里的 secret template 只能答辩用
   - 真实部署必须替换默认密码和 token

5. 故障演示
   - 至少准备删一个 `judge-worker` Pod 的自动恢复演示
   - 可选准备 `mq-down.yaml` 的降级演示

## 8. 故障演示建议命令

删除一个 worker，看自动恢复：

```bash
kubectl get pods -n judgemesh -l app=judge-worker
kubectl delete pod -n judgemesh JUDGE_WORKER_POD
kubectl get pods -n judgemesh -w
```

模拟 MQ 故障：

```bash
kubectl apply -f infra/chaos/mq-down.yaml
kubectl get pods -n judgemesh-infra
```

恢复后删除故障实验：

```bash
kubectl delete -f infra/chaos/mq-down.yaml
```

## 9. 你现在最该怎么做

直接按这个顺序执行：

1. `gcloud container clusters get-credentials judgemesh-gke --zone asia-east1-b`
2. 给四个节点打 `2 app + 2 judge` 的标签和污点
3. `scripts/install-keda.sh`
4. `kubectl apply -k infra/k8s/overlays/final-defense`
5. 用 `scripts/smoke-test.sh` 验证主链路
6. 用 `wrk` 和 `demo-distributed-load.mjs` 准备答辩数据

这个方案是当前仓库下最快、最全、风险最低的最终答辩组织方式。
