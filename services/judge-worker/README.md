# judge-worker

> A · Go 1.23 + isolate · docs/design/05-判题流水线.md / docs/design/12-五人分工.md §A

## 本地运行

```bash
go run ./cmd/worker
# 另开终端
curl localhost:8090/health
curl -X POST localhost:8090/judge -H 'Content-Type: application/json' -d '{
  "submit_id": 1, "problem_id": 1, "language": "cpp",
  "source": "int main(){}", "time_limit_ms": 1000, "memory_limit_mb": 256,
  "testcases": [{"name":"01","input_url":"","expected_output_url":""}],
  "callback_url": "http://localhost:8083/api/submits/internal/result"
}'
```

## 目录

```
cmd/worker/main.go         入口 + 优雅停机
internal/server/           HTTP 路由(/health /metrics /judge)
internal/judge/            判题逻辑(isolate 优先 + C/C++/Java/Python)
internal/config/           env 配置
```

## 生产运行约束

- 容器镜像安装 `isolate`、gcc/g++、OpenJDK 17 与 Python 3。
- `JUDGE_ALLOW_UNSANDBOXED=false` 时,缺少 isolate 会直接判系统错误;本地开发可显式设为 `true`。
- `/metrics` 暴露 Prometheus 指标,包括 worker 存活、判题结果计数和判题耗时直方图。
- 测试用例输入/输出通过 data URL、HTTP(S) URL 或 MinIO 预签名 URL 下载。
