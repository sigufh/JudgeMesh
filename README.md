# JudgeMesh

JudgeMesh 是一个面向课程大作业的分布式在线判题系统。实现范围来自 `docs/design/`:

- Spring Cloud Gateway 统一入口,负责 JWT 校验、路由和用户 Header 透传
- `user-service` 支持注册、登录、JWT、RBAC 档案和余额扣减演示接口
- `problem-service` 支持题目列表、详情、创建/编辑和测试用例 manifest
- `submit-service` 支持提交、状态机、比赛报名、排行榜和 WebSocket 推送
- `judge-dispatcher` 消费提交任务并派发到 worker
- `judge-worker` 支持 C/C++/Java/Python 编译运行、输出比对和回写
- React 前端提供题目、提交、比赛、排行和账户工作台
- K8s manifests 覆盖 frontend、gateway、4 个业务服务、dispatcher、worker 和 Ingress

## 5 分钟本地启动

先启动本地中间件:

```bash
cp infra/local/.env.example infra/local/.env
docker compose -f infra/local/docker-compose.yml --env-file infra/local/.env up -d
```

分别启动后端服务:

```bash
mvn -pl services/api install -DskipTests
mvn -pl services/user-service spring-boot:run
mvn -pl services/problem-service spring-boot:run
SUBMIT_CALLBACK_URL=http://127.0.0.1:8083/api/submit/internal/result mvn -pl services/submit-service spring-boot:run
mvn -pl services/judge-dispatcher spring-boot:run
mvn -pl services/gateway spring-boot:run
```

启动 worker:

```bash
cd services/judge-worker
LISTEN_ADDR=:8090 go run ./cmd/worker
```

启动前端:

```bash
cd frontend
npm install
npm run dev
```

默认前端地址是 `http://localhost:5173`,网关地址是 `http://localhost:8080`。种子账号:

| 角色 | 邮箱 | 密码 |
| --- | --- | --- |
| Student | `student@judgemesh.local` | `Student@12345` |
| Setter | `setter@judgemesh.local` | `Setter@12345` |
| Admin | `admin@judgemesh.local` | `Admin@12345` |

## 验证

```bash
mvn test
cd services/judge-worker && GOCACHE=/private/tmp/judgemesh-go-cache go test ./...
cd frontend && npm run build
BASE_URL=http://localhost:8080 scripts/smoke-test.sh
```

`scripts/smoke-test.sh` 会检查网关健康、公开题目列表、登录和一次提交入队。

## 部署

渲染 dev overlay:

```bash
kubectl kustomize infra/k8s/overlays/dev
```

应用到集群:

```bash
kubectl create namespace judgemesh || true
kubectl create secret generic judgemesh-secrets \
  --from-literal=MYSQL_USER=judgemesh \
  --from-literal=MYSQL_PASSWORD=judgemesh \
  --from-literal=RABBITMQ_USER=judgemesh \
  --from-literal=RABBITMQ_PASSWORD=judgemesh \
  --from-literal=JWT_SECRET=local-dev-secret-change-me-32-bytes \
  -n judgemesh
kubectl apply -k infra/k8s/overlays/dev
```

设计文档入口见 [docs/design/README.md](docs/design/README.md)。
