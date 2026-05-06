# submit-service

> D 负责的提交、比赛、排行榜、WebSocket 聚合服务。端口默认 `8083`。

## 职责范围

- 接收代码提交，生成 `JudgeTask`，投递到 `submit.queue`
- 接收判题回调，更新提交状态
- 管理比赛创建、报名、冻结榜和比赛榜
- 维护全局榜单与比赛榜单，并在可用时同步到 Redis
- 向前端推送提交状态和比赛榜单变更

## 运行约束

- 默认服务名：`submit-service`
- 默认端口：`8083`
- 默认 MQ 队列：`submit.queue`
- 默认回调地址：`http://submit-service:8083/api/submit/internal/result`
- 默认 Redis key：
  - 全局榜：`rank:global`
  - 比赛榜：`rank:contest:{contestId}`
  - 比赛通知前缀：`pubsub:contest:{contestId}`

## 依赖输入

- Gateway 注入的用户头：`X-User-Id`
- 可选题目信息来自 `problem-service`
- 判题结果来自 `judge-worker` 的回调
- `judge-dispatcher` 消费 `submit.queue` 后把任务转发给 worker

## HTTP 接口

所有接口都返回统一封装 `ApiResponse<T>`。

### 提交

- `POST /api/submit`
- Header：`X-User-Id`，默认值 `1`
- Body：`SubmitCreateRequest`

字段：

- `problemId`：题目 ID，必填
- `contestId`：比赛 ID，可空
- `language`：`c` / `cpp` / `java` / `python`
- `code`：源码，必填
- `timeLimitMs`：可选覆盖值
- `memoryLimitMb`：可选覆盖值
- `testcaseManifestUrl`：可选覆盖值

返回：

- `SubmissionDTO`
- 关键状态：`pending`、`judging`、`ac`、`wa`、`tle`、`mle`、`re`、`ce`、`se`

### 查询提交

- `GET /api/submit/{id}`
- `GET /api/submit/mine`
- `mine` 同样依赖 `X-User-Id`

### 判题回调

- `POST /api/submit/internal/result`
- 由 worker 回调调用，不应直接暴露给前端
- Body：`JudgeResult`

回调字段重点：

- `submit_id`
- `status`
- `message`
- `time_used_ms`
- `memory_used_kb`
- `worker_id`
- `worker_version`

### 比赛

- `POST /api/contest`
- `PUT /api/contest/{id}`
- `GET /api/contest/{id}`
- `GET /api/contest/list`
- `POST /api/contest/{id}/register`
- `GET /api/contest/{id}/rank`

比赛创建/更新字段：

- `title`
- `description`
- `startTime`
- `endTime`
- `freezeBeforeMin`
- `problemIds`

### 全局榜

- `GET /api/rank/global`

## WebSocket

- 提交状态推送：`/ws/submission/{submissionId}`
- 比赛榜推送：`/ws/contest/{contestId}/rank`

连接注意事项：

- 允许来源由 `judgemesh.websocket.allowed-origins` 控制，默认 `*`
- 连接建立后服务端会广播最新的提交 DTO 或比赛榜 DTO
- 前端如果断线重连，应重新订阅目标 `submissionId` / `contestId`

## 判题链路

1. 前端提交代码到 `POST /api/submit`
2. `submit-service` 先做代码长度限制和重复提交限流
3. 服务解析题目信息，组装 `JudgeTask`
4. `JudgeTask` 入队 `submit.queue`
5. `judge-dispatcher` 消费任务并投递给 worker
6. worker 回调 `POST /api/submit/internal/result`
7. `submit-service` 更新提交记录、榜单和 WebSocket 推送

## Redis 约定

- 如果 Redis 可用，`LeaderboardService` 会把当前榜单镜像到 ZSet
- 全局榜使用 `rank:global`
- 比赛榜使用 `rank:contest:{contestId}`
- 这些 key 的 score 是内部计算后的榜单分值，不是前端展示分数

## 重要限制

- 当前实现采用内存仓库做本地 bootstrap，方便 smoke test 和无数据库启动
- 生产接入时应切到真实持久层，并确保 `submit-service` 的提交记录、比赛信息、榜单镜像具备一致性
- 比赛冻结后，榜单推送仍会同步，但前端要按冻结状态展示
- `JudgeTask` / `JudgeResult` 属于 A/D 共管协议，字段变化必须一起改

## 对接时不要漏的点

- `X-User-Id` 不是可选装饰，而是提交归属、比赛报名、榜单统计的核心输入
- `language` 必须用小写 wire value，不要传枚举名
- `code` 有长度上限，默认在配置里控制
- `contestId` 为空时走全局提交，非空时必须校验报名和题目归属
- worker 回调失败会直接影响提交状态更新和排行榜刷新
- 如果 worker/dispatcher 还没完全联通，`submit-service` 会退化到只保存提交，不会假设后端判题一定成功

