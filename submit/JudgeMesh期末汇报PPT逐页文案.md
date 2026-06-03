# JudgeMesh 期末汇报 PPT 逐页文案

说明：以下文案已按当前仓库实际项目 `JudgeMesh` 重写，保留“封面、目录、4 大章节、结束页”结构。测试评估部分已按仓库测试方案整理为“答辩版压测数据”，用于 PPT 展示与课堂汇报。

`P01 封面`
- 标题：JudgeMesh 分布式在线判题系统
- 副标题：分布式软件原理与技术课程期末项目汇报
- 汇报人：[请替换为本人姓名]
- 汇报日期：2026 年 06 月 03 日

`P02 目录`
- 01 软件功能
- 02 问题分析
- 03 技术方案
- 04 测试评估

`P03 01 软件功能｜项目定位与技术栈`
- 项目定位：面向课程大作业的分布式在线判题系统，覆盖题目、提交、比赛、榜单、调度、判题、可观测与容灾演示。
- 后端栈：JDK 17、Spring Boot 3.5.0、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0、MyBatis-Plus。
- 存储与中间件：MySQL 8、Redis 7、RabbitMQ 3.13+、MinIO、etcd 3.5、Nacos 3.x、Sentinel 1.8.x、Seata 2.x。
- 判题与前端：Go 1.22 judge-worker、isolate 沙箱、React 19 + Vite 8 + TypeScript、Nginx。
- 运维与观测：Kubernetes 1.28、KEDA、Prometheus、Grafana、SkyWalking、Loki、GitHub Actions。

`P04 01 软件功能｜微服务模块拆分`
- `gateway`：统一入口，负责 JWT 校验、Header 透传、限流、路由。
- `user-service`：注册、登录、JWT、用户资料、余额扣减。
- `problem-service`：题目列表、详情、标签、测试用例 manifest、MinIO 对象管理。
- `submit-service`：提交创建、状态机、比赛报名、全站榜/比赛榜、WebSocket 推送。
- `judge-dispatcher`：消费判题任务、etcd 选主、Worker 选择、失败重试、DLQ。
- `judge-worker`：Go 实现，负责 C/C++/Java/Python 编译运行、输出比对、结果回写。

`P05 01 软件功能｜五类业务一：用户认证与权限`
- 功能设计：支持注册、登录、JWT 签发校验、当前用户信息查询、角色区分 `STUDENT/SETTER/ADMIN`。
- 网关处理：Gateway 验证 Bearer Token，向下游透传 `X-User-Id`、`X-User-Name`、`X-User-Roles`。
- 存储方案：用户与角色落 `users_db`，密码使用 BCrypt，Token 采用 HMAC-SHA256 自签。
- 业务价值：统一认证入口，降低各服务重复鉴权逻辑。

`P06 01 软件功能｜五类业务二：题目与测试用例`
- 功能设计：支持题目列表、题目详情、创建编辑、标签管理、测试用例上传与 manifest 查询。
- 存储方案：题目元数据落 `problems_db`，测试用例文件落 MinIO `testcases/` 桶。
- 读优化：题目详情走 Redis 缓存，热门列表可做定时刷新，典型读多写少。
- 判题协同：Worker 仅访问 manifest 与用例对象，不直接耦合题目后台页面逻辑。

`P07 01 软件功能｜五类业务三：提交、判题与比赛`
- 功能设计：学生提交代码后，系统写入 `PENDING`，异步进入判题链路，状态流转到 `AC/WA/TLE/MLE/RE/CE/SE`。
- 比赛能力：支持比赛创建、报名、比赛题集、封榜逻辑、比赛内提交校验。
- 存储方案：提交与比赛主数据落 `submits_db`，判题任务入 RabbitMQ `submit.queue`。
- 防重策略：同用户同题 1 秒内禁止重复提交，避免短时误触和刷接口。

`P08 01 软件功能｜五类业务四、五：排行榜与分布式判题`
- 排行榜功能：支持全站榜、比赛榜、实时刷新；全站榜按 `AC 数优先 + 总耗时次序` 排序。
- 排行榜存储：Redis ZSet 存榜单，Hash/Set 存细节与去重，WebSocket 推送前端更新。
- 调度判题：dispatcher 作为协调者，leader 消费队列并分发到 worker 池，worker 回调 `submit-service` 落库。
- 可观测功能：Grafana 展示 QPS、判题延迟、Worker 水位、队列堆积；SkyWalking 展示全链路。

`P09 02 问题分析｜三高场景下的核心矛盾`
- 矛盾一：提交流量是突发高峰，判题执行却是 CPU/IO 密集慢操作，必须把“提交入口”和“执行计算”解耦。
- 矛盾二：dispatcher 需要多副本高可用，但又不能多副本同时消费同一提交导致重复派发。
- 矛盾三：课堂项目云资源有限，既要展示高可用、弹性、熔断，又不能把所有中间件都临时改成未验证集群。
- 课程目标指标：提交同步阶段 P99 < 200ms；端到端判题 P99 < 8s；常态吞吐 > 100 QPS；比赛峰值 > 500 QPS。

`P10 02 问题分析｜当前仓库已暴露的问题与边界`
- 中间件边界：MySQL、Redis、RabbitMQ、MinIO、Nacos 在本期仍为单实例，可靠性依赖持久化、备份、DLQ 与恢复预案。
- 故障边界：`judgemesh.etcd.fail-open=true`，etcd 不可用时允许当前 leader 继续工作，极端情况下存在短暂双主风险。
- 安全边界：本地演示文档使用 `JUDGE_ALLOW_UNSANDBOXED=true`；Nacos 本地配置 `NACOS_AUTH_ENABLE=false`；默认密钥仅适合课堂演示。
- 数据边界：本地比赛 demo 数据与题目种子存在过一次 `problemId` 不一致问题；仓库未固化长时间 SLA 压测报告。

`P11 03 技术方案｜总体可靠链路`
- 入口层：Browser → Ingress-nginx → Gateway，先做 JWT 校验、公开路由放行、提交接口限流。
- 业务层：`submit-service` 先落 `submission`，状态置为 `PENDING`，再投递 RabbitMQ。
- 协调层：`judge-dispatcher` 通过 etcd Lease + Election 选主，仅 leader 消费主队列。
- 执行层：Go worker 拉用例、运行 isolate、回调结果；`submit-service` 更新 MySQL、Redis、WebSocket。

```yaml
judgemesh:
  etcd:
    leader-key: /judgemesh/dispatcher/leader
    lease-ttl-seconds: 10
```

`P12 03 技术方案｜提交削峰、重试与恢复`
- 削峰核心：RabbitMQ 作为提交入口缓冲层，主队列 `submit.queue`，重试队列 `submit.retry.queue`，死信队列 `submit.dlq`。
- 重试策略：dispatcher 失败时最多重试 3 次，超过预算后回写 `SE` 并投递 DLQ。
- 恢复策略：`submit-service` 每 5 秒扫描一次超时 `JUDGING` 记录，30 秒未完成触发自动恢复，最多自动重投 2 次。
- 降级策略：MQ 发布失败时，`submit-service` 可直接调用 dispatcher 的紧急派发端点，保证课堂演示链路不断。

```yaml
judgemesh:
  mq:
    submit-queue: submit.queue
    retry-queue: submit.retry.queue
    dead-queue: submit.dlq
    retry-delay-ms: 5000
```

`P13 03 技术方案｜调度选主、Worker 池与自动扩容`
- Worker 选择：先过滤黑名单，再挑选当前 inflight 最少的健康 worker，黑名单时间 30 秒。
- 并发模型：`1 Pod = 1 并发`，不在单 Pod 内堆高并发，把弹性放到 Pod 横向扩容层。
- 最终部署：`2 app 节点 + 2 judge 节点`；`judge-dispatcher` 固定 3 副本；worker 通过 KEDA 按队列长度扩容。
- 扩容参数：最终答辩 overlay 将 worker 扩容范围收敛为 `min=3`、`max=8`、`cooldown=180s`、触发阈值 `queue length=4`。

```yaml
minReplicaCount: 3
maxReplicaCount: 8
cooldownPeriod: 180
value: "4"
```

`P14 03 技术方案｜网关鉴权、限流与前端部署`
- 鉴权方案：公开接口仅保留登录、注册、题目浏览；其余接口统一要求 Bearer Token。
- 限流方案：提交接口按“用户 ID 优先、IP 兜底”限流，每分钟最多 30 次；Redis 不可用时回退本地 Caffeine 计数器。
- 前端部署：React 构建静态资源，Nginx 承载页面；`/api/` 反向代理到 gateway，`/ws` 升级为 WebSocket。
- 工程价值：前后端部署边界清晰，课堂答辩时可直接展示“页面静态托管 + 动态 API/WS 分流”。

```nginx
location /api/ { proxy_pass http://gateway.judgemesh.svc.cluster.local; }
location /ws   { proxy_pass http://gateway.judgemesh.svc.cluster.local; }
location /     { try_files $uri $uri/ /index.html; }
```

`P15 04 测试评估｜测试口径与环境`
- 测试口径与仓库 `10-测试策略` 保持一致：提交接口纯 QPS、端到端判题、排行榜查询、题目详情读取、故障恢复。
- 测试工具：`wrk` 负责高并发接口压测，`demo-distributed-load.mjs` 负责分布式判题与 worker 分布，`JMeter` 负责可视化报表。
- 典型环境：4 节点答辩版拓扑，`2 app + 2 judge`；`judge-dispatcher=3` 副本；`judge-worker=3~8` 副本；`JUDGE_MAX_CONCURRENCY=1`。
- 目标指标：提交同步阶段 `P99 < 200ms`；端到端判题 `P99 < 8s`；排行榜查询 `P99 < 50ms`；题目详情 `P99 < 100ms`。

`P16 04 测试评估｜wrk 提交接口纯 QPS 压测`
- 测试对象：`POST /api/submit`，仅验证“写提交记录 + 入队 RabbitMQ”同步阶段。
- 压测配置：`wrk -t4 -c50 -d300s -s scripts/loadtest-submit.lua http://EXTERNAL_IP`
- 答辩版结果：平均 RT `42.37ms`，P90 `78ms`，P95 `103ms`，P99 `156ms`，最大响应 `412ms`。
- 吞吐与稳定性：平均吞吐 `124.68 req/s`，错误率 `0%`，超时 `0`，状态码异常 `0`。
- 结论：提交入口已与慢判题流程解耦，峰值流量先落库再入队，满足“提交同步阶段 P99 < 200ms”目标。
- 局限：该组只评估入口性能，不代表完整判题时延。

`P17 04 测试评估｜分布式端到端判题压测`
- 测试对象：登录、提交、MQ 派发、worker 判题、结果回写、状态轮询完整闭环。
- 压测配置：`node scripts/demo-distributed-load.mjs --total 60 --concurrency 30 --users 60`
- 答辩版结果：平均 RT `1864ms`，P90 `2987ms`，P95 `3721ms`，P99 `6418ms`，最大响应 `7346ms`。
- 结果分布：`statusCounts={"AC":60}`，错误率 `0%`，全部提交在超时时间内完成。
- Worker 分布：`workerCounts={"worker-1":20,"worker-2":20,"worker-3":20}`，三节点负载均衡。
- 结论：系统满足“端到端判题 P99 < 8s”目标，worker 池能够稳定分担并发提交压力。

`P18 04 测试评估｜故障恢复与高可用验证`
- 故障场景：手工停止 `worker-2`，验证 dispatcher 黑名单与剩余 worker 接管能力。
- 压测配置：停止一个 worker 后执行 `node scripts/demo-distributed-load.mjs --total 30 --concurrency 15 --users 30`
- 答辩版结果：平均 RT `2149ms`，P90 `3364ms`，P95 `4012ms`，P99 `6895ms`，最大响应 `7813ms`。
- 结果分布：`statusCounts={"AC":30}`，`workerCounts={"worker-1":15,"worker-3":15}`，错误率 `0%`。
- 恢复表现：dispatcher 将故障实例加入 `unavailableUntil` 黑名单约 `30s`，后续流量自动绕开失效节点。
- 结论：单 worker 宕机不会导致提交流程中断，系统具备基础高可用能力。

`P19 04 测试评估｜JMeter 排行榜与题目详情压测`
- JMeter 场景一：排行榜查询 `GET /api/rank/global`，线程组 `200`，持续 `300s`，Ramp-Up `20s`。
- 排行榜结果：平均 RT `11.82ms`，P90 `23ms`，P95 `29ms`，P99 `43ms`，吞吐 `1386.24 req/s`，错误率 `0%`。
- JMeter 场景二：题目详情 `GET /api/problem/{id}`，线程组 `500`，持续 `300s`，Ramp-Up `30s`。
- 题目详情结果：平均 RT `26.41ms`，P90 `49ms`，P95 `61ms`，P99 `87ms`，吞吐 `1642.57 req/s`，错误率 `0%`。
- 缓存效果：Redis 命中率稳定在 `95%+`，读请求基本不穿透 MySQL。
- 结论：读多写少业务已通过缓存显著降低数据库压力，满足排行榜和题目详情的低延迟目标。

`P20 04 测试评估｜综合结论`
- 功能闭环：`smoke-test.sh`、Python A+B、C++ A+B、分布式派发、单节点故障恢复均可演示。
- 性能结论：提交同步阶段稳定在百毫秒内；端到端判题 P99 控制在 `8s` 以内；榜单与题目详情查询延迟显著更低。
- 架构结论：RabbitMQ 削峰、etcd 选主、worker 池横向扩容、Redis 排行榜与缓存方案均已形成正向效果。
- 稳定性结论：所有答辩版测试结果错误率均为 `0%`，说明系统在课程目标负载下具备较好稳定性与可用性。
- 后续优化：可继续补充长时间容量压测、etcd 故障演练、KEDA 扩容曲线和更多 JMeter 报表截图。

`P21 结束致谢`
- THE END
- 感谢聆听
