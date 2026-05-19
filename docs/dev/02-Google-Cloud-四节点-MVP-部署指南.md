# Google Cloud 四节点 MVP 部署指南

本文档对应当前仓库的四节点课程答辩部署入口：

- [infra/k8s/overlays/gke-4node-demo](/D:/JudgeMesh/infra/k8s/overlays/gke-4node-demo/kustomization.yaml:1)

目标：

- 满足课程作业场景下的分布式判题
- 尽量突出 judge 集群的横向分布式特征
- 在可接受复杂度内跑通完整链路

## 1. 部署拓扑

推荐使用 Google Cloud `GKE Standard` 集群，4 台工作节点，划分为两组：

- `1` 台 `app` 节点
- `3` 台 `judge` 节点

对应能力：

- `frontend / gateway / user-service / problem-service / submit-service` 运行在 app 节点
- `judge-dispatcher` 使用三副本
- `judge-worker` 使用三副本，固定调度到 `judge` 节点

说明：

- 这是一套 `MVP` 方案，不追求所有基础设施都高可用
- `mysql / minio / redis / rabbitmq / nacos` 当前仍为单实例

## 2. 需要启用的 GCP 服务

在 Google Cloud 项目中启用：

- `Kubernetes Engine API`
- `Artifact Registry API`
- `Compute Engine API`

建议先设置预算告警，避免试用额度意外消耗。

## 3. 创建 Artifact Registry

建议创建一个 Docker 仓库：

- Region: `asia-east1` 或离你最近的区域
- Repository name: `judgemesh`

示例镜像前缀：

```text
asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh
```

认证：

```bash
gcloud auth login
gcloud config set project PROJECT_ID
gcloud auth configure-docker asia-east1-docker.pkg.dev
```

## 4. 构建并推送镜像

先安装共享 API 模块：

```bash
mvn -pl services/api install -DskipTests
```

### 4.1 Java 服务

```bash
mvn -pl services/gateway -DskipTests jib:build -Djib.to.image=asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judgemesh-gateway:latest
mvn -pl services/user-service -DskipTests jib:build -Djib.to.image=asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judgemesh-user-service:latest
mvn -pl services/problem-service -DskipTests jib:build -Djib.to.image=asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judgemesh-problem-service:latest
mvn -pl services/submit-service -DskipTests jib:build -Djib.to.image=asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judgemesh-submit-service:latest
mvn -pl services/judge-dispatcher -DskipTests jib:build -Djib.to.image=asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judgemesh-judge-dispatcher:latest
```

### 4.2 judge-worker

```bash
docker build -t asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judge-worker:latest services/judge-worker
docker push asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judge-worker:latest
```

### 4.3 frontend

```bash
docker build -t asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/frontend:latest frontend
docker push asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/frontend:latest
```

## 5. 修改部署镜像地址

编辑：

- [infra/k8s/overlays/gke-4node-demo/kustomization.yaml](/D:/JudgeMesh/infra/k8s/overlays/gke-4node-demo/kustomization.yaml:1)

将 `images:` 中的 `newName` 改成你的 Artifact Registry 地址，例如：

```yaml
- name: docker.io/judgemesh/judgemesh-gateway
  newName: asia-east1-docker.pkg.dev/PROJECT_ID/judgemesh/judgemesh-gateway
  newTag: latest
```

## 6. 创建 GKE Standard 集群

建议：

- 集群模式：`GKE Standard`
- 区域：选择离你最近的区域
- 节点数：共 `4` 台工作节点

推荐建立两个 node pool：

### 6.1 app-pool

- Node count: `1`
- Machine type: `e2-standard-8`

### 6.2 judge-pool

- Node count: `3`
- Machine type: `e2-standard-4`

如果预算更宽裕，可以把其中一台 `judge` 节点升到 `e2-standard-8`。

## 7. 获取集群凭据

```bash
gcloud container clusters get-credentials CLUSTER_NAME --zone ZONE --project PROJECT_ID
```

查看节点：

```bash
kubectl get nodes
```

## 8. 为节点打标签

给一个业务节点打 `app` 标签，三个判题节点打 `judge` 标签：

```bash
kubectl label node APP_NODE role=app
kubectl label node JUDGE_NODE_1 role=judge
kubectl label node JUDGE_NODE_2 role=judge
kubectl label node JUDGE_NODE_3 role=judge

kubectl taint node JUDGE_NODE_1 role=judge:NoSchedule
kubectl taint node JUDGE_NODE_2 role=judge:NoSchedule
kubectl taint node JUDGE_NODE_3 role=judge:NoSchedule
```

说明：

- 这套 `gke-4node-demo` 使用 `taint + toleration` 将判题节点和业务节点隔离
- `judge-worker` 会固定运行在 `judge` 节点
- 其他服务固定运行在 `app` 节点

## 9. 部署应用

```bash
kubectl apply -k infra/k8s/overlays/gke-4node-demo
```

检查资源：

```bash
kubectl get pods -A -o wide
kubectl get svc -A
kubectl get ingress -A
```

## 10. 验收重点

### 10.1 查看调度分布

```bash
kubectl get pods -A -o wide
```

重点确认：

- `frontend / gateway / user-service / problem-service / submit-service` 都落在 `app` 节点
- `judge-dispatcher` 是三副本
- `judge-worker` 在 `judge` 节点上，并尽量分散在 3 台 judge 机器

### 10.2 查看核心日志

```bash
kubectl logs -n judgemesh deploy/gateway
kubectl logs -n judgemesh deploy/submit-service
kubectl logs -n judgemesh deploy/problem-service
kubectl logs -n judgemesh deploy/judge-dispatcher
kubectl logs -n judgemesh-infra statefulset/mysql
kubectl logs -n judgemesh-infra statefulset/minio
kubectl logs -n judgemesh-infra job/minio-bootstrap
```

### 10.3 访问入口

当前 overlay 已移除固定 `host` 绑定，可直接使用 Ingress 对外地址访问。

建议先测试：

- 首页是否打开
- 登录是否正常
- 题目列表是否正常
- 提交代码是否能进入判题链路

## 11. 答辩时建议演示点

建议展示：

1. `kubectl get pods -A -o wide`
   说明 `judge-worker` 分布在 3 台 judge 节点
2. 提交一次代码
   说明 `submit-service -> RabbitMQ -> judge-dispatcher -> judge-worker`
3. 删除一个 worker Pod 或一个 app Pod
   说明系统会自动拉起，体现高可用

示例：

```bash
kubectl delete pod -n judgemesh POD_NAME
```

## 12. 当前 MVP 边界

这套部署满足：

- 分布式判题
- 判题节点横向扩展展示
- 课程作业级别的可演示性

这套部署暂时不覆盖：

- `mysql / minio / redis / rabbitmq / nacos` 的多副本高可用
- `app` 节点故障后的完整业务高可用
- 生产级 TLS
- 完整监控告警栈
- `seata`
