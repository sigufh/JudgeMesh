# Google Cloud 四节点 MVP 部署与压测指南

本文档对应当前仓库的四节点课程作业部署入口：

- [infra/k8s/overlays/gke-4node-demo](/D:/JudgeMesh/infra/k8s/overlays/gke-4node-demo/kustomization.yaml:1)

目标：

- 按仓库当前 `1 app + 3 judge` 的 MVP 方案部署
- 从登录 Google Cloud 开始，完成项目创建、镜像推送、GKE 部署、功能验收
- 使用仓库已有脚本完成基础压测和分布式判题演示

本文档严格对齐当前仓库设计边界：

- 重点展示分布式判题和可演示的高可用表现
- `mysql / minio / redis / rabbitmq / nacos` 当前仍为单实例
- 当前不覆盖完整生产级高可用、TLS、Seata 上云落地

## 1. 总体拓扑

推荐使用 `GKE Standard`，建立两个 node pool：

- `app-pool`：`1` 台业务节点，标签 `role=app`
- `judge-pool`：`3` 台判题节点，标签 `role=judge`

调度目标：

- `frontend / gateway / user-service / problem-service / submit-service` 固定运行在 `app` 节点
- `judge-dispatcher` 运行 `3` 副本，固定在 `app` 节点
- `judge-worker` 运行 `3` 副本，固定在 `judge` 节点，并尽量打散到 3 台机器

## 2. 你需要准备什么

本机建议准备：

- Google 账号
- 已激活计费的 Google Cloud 试用账号
- `Docker`
- `Java 17`
- `Maven`
- `Node.js 20`
- `kubectl`
- `gcloud CLI`
- 当前仓库代码

命令行约定：

- 本文中带环境变量的命令按 `Bash` / `Git Bash` / `WSL` 书写
- 如果你在 Windows 的 `PowerShell` 中操作，建议：
  - 普通 `gcloud / kubectl / docker / mvn / node` 命令直接执行
  - 带 `FOO=bar command` 形式的示例，优先切到 `Git Bash` 执行
  - 仓库里的 `scripts/*.sh` 也建议在 `Git Bash` 或 `WSL` 中执行

建议部署区域：

- 如果你在中国大陆附近，优先试 `asia-east1`
- 如果某个机型配额不足，再换到 `asia-southeast1` 或 `us-central1`

## 3. 第一步：登录 Google Cloud 并创建项目

### 3.1 浏览器登录

1. 打开 Google Cloud Console：<https://console.cloud.google.com/>
2. 使用你的 Google 账号登录
3. 确认当前账号已经激活 Billing

如果你正在用试用额度，建议立刻先做一件事：

1. 进入 `Billing`
2. 打开 `Budgets & alerts`
3. 创建一个预算
4. 建议阈值至少设置：
   - `50%`
   - `90%`
   - `100%`

课程作业阶段很容易因为忘记关集群持续计费，这一步不要跳。

### 3.2 创建项目

在控制台右上角项目选择器中：

1. 点击 `New Project`
2. 填写项目名，例如 `judgemesh-demo`
3. 记录 `Project ID`
4. 绑定 Billing account
5. 点击 `Create`

后文把这个值记为：

```bash
PROJECT_ID=project-ad762830-bf41-4f10-b00
```

## 4. 第二步：安装并初始化本机命令行环境

### 4.1 安装 gcloud CLI

官方文档：

- <https://cloud.google.com/sdk/docs/install>

Windows 用户最简单的方式是下载安装器；Linux/macOS 按官方页面安装即可。

安装后执行：

```bash
gcloud version
```

### 4.2 登录 gcloud

```bash
gcloud auth login
gcloud config set project PROJECT_ID
```

建议再执行一次初始化：

```bash
gcloud init
```

### 4.3 检查 kubectl

```bash
kubectl version --client
```

如果没有安装，可以自行安装系统包，或者使用 gcloud 提供的组件方式安装。

## 5. 第三步：启用项目 API

本项目最少需要启用：

- `Kubernetes Engine API`
- `Artifact Registry API`
- `Compute Engine API`

命令：

```bash
gcloud services enable container.googleapis.com artifactregistry.googleapis.com compute.googleapis.com
```

可选再检查一次：

```bash
gcloud services list --enabled
```

## 6. 第四步：创建镜像仓库 Artifact Registry

### 6.1 创建仓库

这里假设区域使用 `asia-east1`，仓库名使用 `judgemesh`：

```bash
REGION=asia-east1
REPO=judgemesh

gcloud artifacts repositories create "judgemesh" --repository-format=docker --location="asia-east1" --description="JudgeMesh Docker repository"
```

镜像前缀将是：

```text
asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh
```

### 6.2 配置 Docker 认证

```bash
gcloud auth configure-docker "asia-east1-docker.pkg.dev"
```

## 7. 第五步：先在本地做一次构建与测试

建议在推镜像之前先跑一遍仓库基础测试：

```bash
mvn test
go test ./...
cd frontend && npm run lint && npm run build
```

这一步我之前已经在当前仓库跑过，结果是通过的。你后续改代码后，再上线前建议自己再跑一遍。

## 8. 第六步：构建并推送项目镜像

先定义镜像前缀：

```bash
IMAGE_PREFIX=${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}
```

### 8.1 构建共享 API 模块

```bash
mvn -pl services/api install -DskipTests
```

### 8.2 构建并推送 Java 服务

```bash
mvn -pl services/gateway -DskipTests jib:build -Djib.to.image=${IMAGE_PREFIX}/judgemesh-gateway:latest
mvn -pl services/user-service -DskipTests jib:build -Djib.to.image=${IMAGE_PREFIX}/judgemesh-user-service:latest
mvn -pl services/problem-service -DskipTests jib:build -Djib.to.image=${IMAGE_PREFIX}/judgemesh-problem-service:latest
mvn -pl services/submit-service -DskipTests jib:build -Djib.to.image=${IMAGE_PREFIX}/judgemesh-submit-service:latest
mvn -pl services/judge-dispatcher -DskipTests jib:build -Djib.to.image=${IMAGE_PREFIX}/judgemesh-judge-dispatcher:latest
```

### 8.3 构建并推送 judge-worker

```bash
docker build -t ${IMAGE_PREFIX}/judge-worker:latest services/judge-worker
docker push ${IMAGE_PREFIX}/judge-worker:latest
```

### 8.4 构建并推送 frontend

```bash
docker build -t ${IMAGE_PREFIX}/frontend:latest frontend
docker push ${IMAGE_PREFIX}/frontend:latest
```

### 8.5 当 `problem-service` / `submit-service` 源码有修复时重新发布

如果你修改了下面这类内容，仅执行 `kubectl apply -k` 不会自动把代码变更带进集群，必须重新构建并推送镜像：

- `services/problem-service/src/main/resources/application.yml`
- `services/submit-service/src/main/resources/application.yml`
- 任意 Java 源码、依赖、`Dockerfile`、资源文件

重新发布命令如下：

```bash
mvn -pl services/problem-service -DskipTests jib:build -Djib.to.image=${IMAGE_PREFIX}/judgemesh-problem-service:latest
mvn -pl services/submit-service -DskipTests jib:build -Djib.to.image=${IMAGE_PREFIX}/judgemesh-submit-service:latest

kubectl rollout restart deployment -n judgemesh problem-service
kubectl rollout restart deployment -n judgemesh submit-service
kubectl get pods -n judgemesh
```

典型场景：

- 修复 Redis / MySQL / Nacos 环境变量映射
- 调整 Spring Boot 配置文件
- 修复服务启动失败或依赖连接失败
- Java 服务默认通过 Jib 发布，不要求服务目录内存在 Dockerfile

## 9. 第七步：修改 Kustomize 镜像地址

编辑：

- [infra/k8s/overlays/gke-4node-demo/kustomization.yaml](/D:/JudgeMesh/infra/k8s/overlays/gke-4node-demo/kustomization.yaml:1)

把 `images:` 里所有 `newName` 改成你的 Artifact Registry 地址，例如：

```yaml
- name: docker.io/judgemesh/judgemesh-gateway
  newName: asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judgemesh-gateway
  newTag: latest
```

需要替换的镜像一共有：

- `judgemesh-gateway`
- `judgemesh-user-service`
- `judgemesh-problem-service`
- `judgemesh-submit-service`
- `judgemesh-judge-dispatcher`
- `judge-worker`
- `frontend`

## 10. 第八步：创建 GKE Standard 四节点集群

### 10.1 为什么选 GKE Standard

本仓库的 `judge-worker` 使用了特权容器能力，当前 manifest 带有 `privileged: true`。因此这里按 `GKE Standard` 方案部署，不建议用 Autopilot。

### 10.2 创建业务节点池所在的集群

这里用单区集群，成本更低，也更适合课程作业演示。

```bash
CLUSTER_NAME=judgemesh-gke
ZONE=asia-east1-b

gcloud container clusters create "$CLUSTER_NAME" --zone "$ZONE" --machine-type e2-standard-8 --num-nodes 1 --release-channel regular --enable-ip-alias
```

说明：

- 这一步会先创建第一个默认 node pool
- 我们把它当作 `app-pool` 使用
- 如果你在控制台创建，也保持等价配置即可

### 10.3 创建 judge 节点池

```bash
gcloud container node-pools create judge-pool --cluster "$CLUSTER_NAME" --zone "$ZONE" --machine-type e2-standard-4 --num-nodes 3
```

如果你的预算比较宽裕，也可以把其中一台 judge 的规格提到 `e2-standard-8`，但对课程答辩不是必须。

## 11. 第九步：连接集群并给节点打标签

### 11.1 获取凭据

```bash
gcloud container clusters get-credentials "$CLUSTER_NAME" --zone "$ZONE" --project "$PROJECT_ID"
```

### 11.2 查看节点

```bash
kubectl get nodes -o wide
kubectl get nodes -L cloud.google.com/gke-nodepool
```

你会看到一台默认池节点，三台 `judge-pool` 节点。

### 11.3 给节点打标签和污点

把默认池那台标为 `app`，把三台 `judge-pool` 节点标为 `judge`：

```bash
kubectl label node APP_NODE role=app
kubectl label node JUDGE_NODE_1 role=judge
kubectl label node JUDGE_NODE_2 role=judge
kubectl label node JUDGE_NODE_3 role=judge
```

再给三台 judge 节点加污点：

```bash
kubectl taint node JUDGE_NODE_1 role=judge:NoSchedule
kubectl taint node JUDGE_NODE_2 role=judge:NoSchedule
kubectl taint node JUDGE_NODE_3 role=judge:NoSchedule
```

再次检查：

```bash
kubectl get nodes --show-labels
```

## 12. 第十步：检查 Secret 模板并部署

课程作业场景下，仓库里的 Secret 模板已经做过简化：

- [infra/k8s/base/app-secrets/secret-template.yaml](/D:/JudgeMesh/infra/k8s/base/app-secrets/secret-template.yaml:1)
- [infra/k8s/base/infra-secrets/secret-template.yaml](/D:/JudgeMesh/infra/k8s/base/infra-secrets/secret-template.yaml:1)

如果你只是演示，可以直接使用当前模板里的默认值。

正式部署前先渲染检查：

```bash
kubectl kustomize infra/k8s/overlays/gke-4node-demo > NUL
```

如果没有报错，再执行部署：

```bash
kubectl apply -k infra/k8s/overlays/gke-4node-demo
```

## 13. 第十一步：等待资源启动

建议按下面顺序看：

```bash
kubectl get pods -A -w
```

另开一个终端检查：

```bash
kubectl get svc -A
kubectl get ingress -A
```

重点关注：

- `judgemesh-infra` 命名空间里的中间件全部进入 `Running`
- `judgemesh` 命名空间里的业务服务和判题服务全部进入 `Running`
- Ingress 出现外部地址

如果 Ingress 地址还没出来，多等几分钟再看：

```bash
kubectl get svc -n ingress-nginx
kubectl get ingress -n judgemesh
```

## 14. 第十二步：从浏览器登录项目

### 14.1 获取访问地址

假设 `kubectl get ingress -n judgemesh` 得到外网 IP：

```text
EXTERNAL_IP=34.x.x.x
```

浏览器访问：

```text
http://EXTERNAL_IP/
```

### 14.2 使用系统内置种子账号登录

当前仓库的 `user-service` 会自动创建种子用户，可以直接用于第一次验收：

- 学生账号：`student@judgemesh.local`
- 密码：`Student@12345`

也可以用下面两个账号测试：

- 管理员：`admin@judgemesh.local` / `Admin@12345`
- 出题人：`setter@judgemesh.local` / `Setter@12345`

### 14.3 首次人工功能验收

至少手动检查：

1. 首页能否打开
2. 登录能否成功
3. 题目列表能否加载
4. 提交代码后能否看到提交记录

## 15. 第十三步：用仓库脚本做自动验收

### 15.1 运行 smoke test

仓库已有：

- [scripts/smoke-test.sh](/D:/JudgeMesh/scripts/smoke-test.sh:1)

命令：

```bash
BASE_URL=http://EXTERNAL_IP scripts/smoke-test.sh
```

它会自动检查：

- Gateway 健康状态
- 公开题目列表
- 登录
- 一次代码提交是否成功入队

### 15.2 如果题目数据还不够

你可以用仓库脚本补一些演示数据：

```bash
BASE_URL=http://EXTERNAL_IP node scripts/seed-demo-data.mjs
```

这个脚本会：

- 创建一个演示题
- 注册一批测试用户
- 生成一批提交记录
- 如果有比赛，还会补部分比赛数据

## 16. 第十四步：准备压测前置数据

### 16.1 获取登录 JWT

仓库已有：

- [scripts/gen-jwt.sh](/D:/JudgeMesh/scripts/gen-jwt.sh:1)

命令：

```bash
TOKEN="$(BASE_URL=http://EXTERNAL_IP scripts/gen-jwt.sh)"
echo "$TOKEN"
```

### 16.2 获取题目 ID

```bash
curl -s http://EXTERNAL_IP/api/problem/list
```

取返回结果中的第一个题目 ID，记为：

```bash
PROBLEM_ID=题目ID
```

## 17. 第十五步：做第一组压测

仓库已有：

- [scripts/loadtest-submit.lua](/D:/JudgeMesh/scripts/loadtest-submit.lua:1)

这个脚本适合做“提交接口连续压测”。

### 17.1 安装 wrk

如果你本机没有 `wrk`，先安装它。Windows 可以用包管理器安装，Linux/macOS 直接装系统包或源码编译都可以。

### 17.2 执行压测

```bash
TOKEN="$TOKEN" PROBLEM_ID="$PROBLEM_ID" USER_ID=1002 wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
```

参数说明：

- `-t4`：4 个线程
- `-c32`：32 并发连接
- `-d60s`：持续 60 秒

建议你按下面三档逐步压：

1. 轻载：

```bash
TOKEN="$TOKEN" PROBLEM_ID="$PROBLEM_ID" USER_ID=1002 wrk -t2 -c16 -d30s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
```

2. 中载：

```bash
TOKEN="$TOKEN" PROBLEM_ID="$PROBLEM_ID" USER_ID=1002 wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
```

3. 答辩演示载荷：

```bash
TOKEN="$TOKEN" PROBLEM_ID="$PROBLEM_ID" USER_ID=1002 wrk -t6 -c50 -d120s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
```

## 18. 第十六步：做分布式判题演示压测

仓库已有：

- [scripts/demo-distributed-load.mjs](/D:/JudgeMesh/scripts/demo-distributed-load.mjs:1)

这个脚本更适合答辩，因为它不仅会并发提交，还会统计：

- 每次提交的最终状态
- 判题延迟
- 不同 worker 的分布情况

### 18.1 可选：本地转发 dispatcher 管理接口

如果你想让脚本顺便读取 dispatcher 状态，先开一个本地转发：

```bash
kubectl -n judgemesh port-forward deploy/judge-dispatcher 8084:8084
```

### 18.2 执行分布式演示脚本

```bash
BASE_URL=http://EXTERNAL_IP DISPATCHER_URL=http://127.0.0.1:8084 node scripts/demo-distributed-load.mjs --total 60 --concurrency 30
```

建议答辩前至少跑两组：

1. 基础组：

```bash
BASE_URL=http://EXTERNAL_IP DISPATCHER_URL=http://127.0.0.1:8084 node scripts/demo-distributed-load.mjs --total 30 --concurrency 10
```

2. 展示组：

```bash
BASE_URL=http://EXTERNAL_IP DISPATCHER_URL=http://127.0.0.1:8084 node scripts/demo-distributed-load.mjs --total 60 --concurrency 30
```

运行完成后，重点看脚本输出里的：

- `statusCounts`
- `workerCounts`
- `latencyMs p50 / p95 / max`

如果 `workerCounts` 显示任务落到了多个 worker，就能直接体现分布式判题。

## 19. 第十七步：压测时你应该同时观察什么

压测进行时，另开终端观察：

```bash
kubectl get pods -n judgemesh -o wide
```

重点确认：

- `judge-worker` 是否分散在 3 台 `judge` 节点
- `judge-dispatcher` 是否保持 3 副本
- 业务 Pod 是否仍然固定在 `app` 节点

再看日志：

```bash
kubectl logs -n judgemesh deploy/judge-dispatcher --tail=100
kubectl logs -n judgemesh deploy/submit-service --tail=100
kubectl logs -n judgemesh deploy/problem-service --tail=100
```

如果你想演示“自动恢复”：

```bash
kubectl delete pod -n judgemesh POD_NAME
```

建议删一个 `judge-worker` Pod 或一个 `app` Pod，然后观察系统自动重建。

## 20. 第十八步：答辩时推荐的演示顺序

推荐顺序：

1. 先展示 `kubectl get nodes`
   说明集群是 `1 app + 3 judge`
2. 再展示 `kubectl get pods -n judgemesh -o wide`
   说明 `judge-worker` 分散在 judge 节点
3. 浏览器登录系统
   说明系统已经可用
4. 运行 `scripts/smoke-test.sh`
   说明主链路可通
5. 运行 `demo-distributed-load.mjs`
   说明并发提交和 worker 分布
6. 删除一个 `judge-worker` Pod
   说明具备可演示的自动恢复能力

## 21. 常见问题

### 21.1 Ingress 没有外网地址

先看：

```bash
kubectl get svc -n ingress-nginx
kubectl get ingress -n judgemesh
```

通常是 LoadBalancer 还没分配完成，等几分钟再看。

### 21.2 judge-worker 起不来

先看：

```bash
kubectl describe pod -n judgemesh POD_NAME
kubectl logs -n judgemesh POD_NAME
```

高频原因：

- 节点没有正确打 `role=judge`
- judge 节点没有打污点或污点和 toleration 不一致
- 机型资源太小

### 21.3 镜像拉取失败

检查：

1. `kustomization.yaml` 里的镜像地址是否全部改对
2. 镜像是否真的已经 push 到 Artifact Registry
3. 区域前缀是否一致，例如是否都是 `asia-east1-docker.pkg.dev`

## 22. 用完后一定要清理

课程作业阶段最容易忘的就是清理资源。

### 22.1 删除集群

```bash
gcloud container clusters delete "$CLUSTER_NAME" --zone "$ZONE"
```

### 22.2 如有需要，删除镜像仓库

```bash
gcloud artifacts repositories delete "$REPO" --location="$REGION"
```

### 22.3 再去控制台确认

手动检查：

- GKE 集群是否已经删除
- LoadBalancer 是否已经释放
- Artifact Registry 是否按需保留或删除

## 23. 参考资料

Google Cloud 官方文档：

- 安装 gcloud CLI：<https://cloud.google.com/sdk/docs/install>
- 创建项目：<https://cloud.google.com/resource-manager/docs/creating-managing-projects>
- 预算与告警：<https://cloud.google.com/billing/docs/how-to/budgets>
- Artifact Registry Docker 仓库：<https://cloud.google.com/artifact-registry/docs/docker/store-docker-container-images>
- 创建 GKE Standard 集群：<https://cloud.google.com/kubernetes-engine/docs/how-to/creating-a-zonal-cluster>
- 管理 GKE node pools：<https://cloud.google.com/kubernetes-engine/docs/how-to/node-pools>
- 获取集群凭据：<https://cloud.google.com/sdk/gcloud/reference/container/clusters/get-credentials>

仓库内相关文件：

- [infra/k8s/overlays/gke-4node-demo/README.md](/D:/JudgeMesh/infra/k8s/overlays/gke-4node-demo/README.md:1)
- [scripts/README.md](/D:/JudgeMesh/scripts/README.md:1)
- [scripts/smoke-test.sh](/D:/JudgeMesh/scripts/smoke-test.sh:1)
- [scripts/loadtest-submit.lua](/D:/JudgeMesh/scripts/loadtest-submit.lua:1)
- [scripts/demo-distributed-load.mjs](/D:/JudgeMesh/scripts/demo-distributed-load.mjs:1)
