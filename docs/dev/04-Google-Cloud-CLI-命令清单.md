# Google Cloud CLI 命令清单

只放命令，默认对应你当前已存在的集群：

- `CLUSTER_NAME=judgemesh-gke`
- `ZONE=asia-east1-b`

## 1. 检查当前环境

```bash
gcloud version
gcloud config list
gcloud container clusters list
```

## 2. 连接当前集群

```bash
gcloud container clusters get-credentials judgemesh-gke --zone asia-east1-b
kubectl get nodes -o wide
kubectl get nodes -L cloud.google.com/gke-nodepool
```

## 2.1 安装 KEDA

最快方式，不依赖本机 Helm：

```bash
kubectl apply --server-side -f https://github.com/kedacore/keda/releases/download/v2.19.0/keda-2.19.0.yaml
kubectl get pods -n keda
kubectl get crd scaledobjects.keda.sh
```

如果你后续想装 Helm，Windows 可选：

```bash
winget install Helm.Helm
```

或：

```bash
choco install kubernetes-helm
```

## 3. 方案 A：最终答辩推荐，2 app + 2 judge

```bash
kubectl get nodes

kubectl label node APP_NODE_1 role=app --overwrite
kubectl label node APP_NODE_2 role=app --overwrite
kubectl label node JUDGE_NODE_1 role=judge --overwrite
kubectl label node JUDGE_NODE_2 role=judge --overwrite

kubectl taint node JUDGE_NODE_1 role=judge:NoSchedule --overwrite
kubectl taint node JUDGE_NODE_2 role=judge:NoSchedule --overwrite

scripts/install-keda.sh
kubectl apply -k infra/k8s/overlays/final-defense

kubectl get pods -A -o wide
kubectl get hpa -n judgemesh
kubectl get scaledobject -n judgemesh
kubectl get pdb -A
```

## 4. 方案 B：可做，1 app + 3 judge

```bash
kubectl label node gke-judgemesh-gke-default-pool-709229c4-nbjg role=app --overwrite

kubectl label node gke-judgemesh-gke-judge-pool-68005c61-67pm role=judge --overwrite
kubectl label node gke-judgemesh-gke-judge-pool-68005c61-79fw role=judge --overwrite
kubectl label node gke-judgemesh-gke-judge-pool-68005c61-xs5d role=judge --overwrite

kubectl taint node gke-judgemesh-gke-judge-pool-68005c61-67pm role=judge:NoSchedule --overwrite
kubectl taint node gke-judgemesh-gke-judge-pool-68005c61-79fw role=judge:NoSchedule --overwrite
kubectl taint node gke-judgemesh-gke-judge-pool-68005c61-xs5d role=judge:NoSchedule --overwrite

scripts/install-keda.sh
kubectl apply -k infra/k8s/overlays/gke-4node-demo

kubectl get nodes --show-labels
kubectl get pods -A -o wide
kubectl get scaledobject -n judgemesh
```

## 5. 如果以后要新建同规格四节点集群

```bash
export PROJECT_ID=YOUR_PROJECT_ID
export REGION=asia-east1
export ZONE=asia-east1-b
export CLUSTER_NAME=judgemesh-gke
export REPO=judgemesh
```

```bash
gcloud config set project "$PROJECT_ID"
gcloud services enable container.googleapis.com artifactregistry.googleapis.com compute.googleapis.com
```

```bash
gcloud artifacts repositories create "$REPO" \
  --repository-format=docker \
  --location="$REGION" \
  --description="JudgeMesh Docker repository"
```

```bash
gcloud auth configure-docker "${REGION}-docker.pkg.dev"
```

```bash
gcloud container clusters create "$CLUSTER_NAME" \
  --zone "$ZONE" \
  --machine-type e2-standard-4 \
  --num-nodes 1 \
  --release-channel regular \
  --enable-ip-alias
```

```bash
gcloud container node-pools create judge-pool \
  --cluster "$CLUSTER_NAME" \
  --zone "$ZONE" \
  --machine-type e2-standard-4 \
  --num-nodes 3
```

```bash
gcloud container clusters get-credentials "$CLUSTER_NAME" --zone "$ZONE" --project "$PROJECT_ID"
kubectl get nodes -o wide
```

## 6. 常用检查命令

```bash
kubectl get pods -A -o wide
kubectl get svc -A
kubectl get ingress -A
kubectl get nodes --show-labels
kubectl describe scaledobject -n judgemesh judge-worker-queue-scaler
kubectl get events -A --sort-by=.lastTimestamp
```

## 7. 压测前常用命令

```bash
kubectl -n judgemesh port-forward deploy/judge-dispatcher 8084:8084
BASE_URL=http://EXTERNAL_IP scripts/smoke-test.sh
TOKEN="$(BASE_URL=http://EXTERNAL_IP scripts/gen-jwt.sh)"
TOKEN="$TOKEN" PROBLEM_ID=1 USER_ID=1002 wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://EXTERNAL_IP
BASE_URL=http://EXTERNAL_IP DISPATCHER_URL=http://127.0.0.1:8084 node scripts/demo-distributed-load.mjs --total 60 --concurrency 30
```

## 8. 故障演示命令

```bash
kubectl get pods -n judgemesh -l app=judge-worker
kubectl delete pod -n judgemesh JUDGE_WORKER_POD
kubectl get pods -n judgemesh -w
```

```bash
kubectl apply -f infra/chaos/mq-down.yaml
kubectl get pods -n judgemesh-infra
kubectl delete -f infra/chaos/mq-down.yaml
```

## 9. 清理

```bash
gcloud container clusters delete judgemesh-gke --zone asia-east1-b
```

```bash
gcloud artifacts repositories delete judgemesh --location=asia-east1
```
