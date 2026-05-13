# scripts/ — 运维与工具脚本

| 脚本                      | 用途                          | 维护者 | 状态     |
| ------------------------- | ----------------------------- | ------ | -------- |
| `k8s-bootstrap.sh`        | kubeadm 集群初始化保护脚本,默认 dry-run | @sigufh      | READY    |
| `import-problems.py`      | 批量导入 50 道 demo 题和用例    | @KY-raika      | READY    |
| `loadtest-submit.lua`     | wrk 压测脚本(提交链路)        | @sigufh + @Nier291  | READY    |
| `demo-distributed-load.mjs` | 并发提交并统计 worker 分布,用于分布式调度课堂演示 | @Phoen1xCode + @Nier291 | READY |
| `gen-jwt.sh`              | 调试用:登录并输出测试 JWT      | @Phoen1xCode      | READY    |
| `port-forward.sh`         | 一键转发 Gateway / Frontend / Nacos / Grafana / SkyWalking | @sigufh | READY |
| `smoke-test.sh`           | 健康检查、登录、题目列表和提交入队 | 全员 | READY |

## 常用命令

```bash
python3 scripts/import-problems.py --service http://localhost:8082 --limit 50
TOKEN="$(BASE_URL=http://localhost:8080 scripts/gen-jwt.sh)"
wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://localhost:8080
node scripts/demo-distributed-load.mjs --total 60 --concurrency 30
scripts/port-forward.sh
scripts/k8s-bootstrap.sh            # dry-run
scripts/k8s-bootstrap.sh --confirm  # 实际初始化 kubeadm
```
