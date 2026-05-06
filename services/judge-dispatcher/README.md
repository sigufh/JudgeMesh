# judge-dispatcher

> D 负责的判题派发器。端口默认 `8084`。

## 职责范围

- 监听 `submit.queue`
- 做 leader 选主
- 从配置的 worker 列表里挑选健康 worker
- 把 `JudgeTask` 转发到 worker 的 `/judge`
- 提供分发状态和混沌演示接口

## 运行模式

### `memory`

- 默认模式
- 单机 bootstrap 用
- 进程内直接视为 leader
- 适合本地 smoke test 和没有 etcd 的场景

### `etcd`

- 集群模式
- 通过 `jetcd` 在 `judgemesh.dispatcher.etcd.leader-key` 下竞争 leader
- leader 通过 lease 保活
- lease 丢失后会自动让出并重新竞选

切换方式：

```yaml
judgemesh:
  dispatcher:
    mode: etcd
```

## 默认配置

- 服务名：`judge-dispatcher`
- 端口：`8084`
- Rabbit 队列：`submit.queue`
- worker 健康检查：`/health`
- worker 判题接口：`/judge`
- 默认 worker 地址：`http://judge-worker:8090`
- etcd leader key：`/judgemesh/dispatcher/leader`

## HTTP 接口

### 状态

- `GET /admin/dispatcher/status`
- 返回调度器当前模式、是否 leader、leaderId、最近一次派发时间、worker 列表、当前 inflight

### 混沌演示

- `POST /admin/dispatcher/chaos/kill-self`
- 当前实现会延迟后退出 JVM
- 这是演示入口，不应放到生产灰度流程里

## MQ 约定

- 消费队列：`submit.queue`
- 消息体：`JudgeTask`
- 监听逻辑只有 leader 节点生效，follower 节点不会消费判题任务

## Worker 转发

`JudgeTask` 通过 HTTP POST 转发到 worker 的 `/judge`。

选择策略：

- 先按当前 inflight 数排序
- 再对每个 worker 调 `GET {worker}/health`
- 第一个健康 worker 获得任务
- 请求成功后会登记 inflight，超时后自动释放

## leader 选主细节

- `etcd` 模式下，服务启动后会先申请 lease
- 申请成功后再参与竞选
- 通过定时 keepalive 维持 lease
- keepalive 失败会触发让位并重试

## 对接时不要漏的点

- `submit.queue` 必须和 `submit-service` 完全一致
- worker 地址列表要与实际部署一致，不要只靠默认值
- worker 的健康检查路径必须返回 2xx，否则不会被派发
- `judge-dispatcher` 只负责派发，不负责解析题目、评测源码、落库提交结果
- `JudgeTask` / `JudgeResult` 是 A/D 共管协议，字段改动必须先同步
- 设计文档里提到的 worker 端口和本地 bootstrap 默认端口可能不一致，部署时以实际 worker 配置为准

## 目前实现边界

- 本地模式不依赖 etcd，适合快速启动
- 集群模式已经接入 etcd client 和 leader 竞选
- worker 端如果还没上线，dispatch 会在健康检查阶段失败并重试
- 该服务不做题目拉取，不做比赛榜计算，不做判题回调

