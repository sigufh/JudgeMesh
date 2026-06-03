# GKE 4-Node Demo Overlay

This overlay is for course-demo deployment on GKE Standard with four worker nodes:

- `1` app node labeled `role=app`
- `3` judge nodes labeled `role=judge`
- judge nodes should also be tainted with `role=judge:NoSchedule`

Behavior:

- app and infra workloads are pinned to the single app node
- `judge-worker` runs with `3` replicas and uses required node affinity plus pod anti-affinity
- `judge-worker` scales from `3` replicas based on queue length after the KEDA operator is installed
- ingress does not require a fixed hostname

Prepare nodes:

```bash
kubectl label node APP_NODE role=app
kubectl label node JUDGE_NODE_1 role=judge
kubectl label node JUDGE_NODE_2 role=judge
kubectl label node JUDGE_NODE_3 role=judge

kubectl taint node JUDGE_NODE_1 role=judge:NoSchedule
kubectl taint node JUDGE_NODE_2 role=judge:NoSchedule
kubectl taint node JUDGE_NODE_3 role=judge:NoSchedule
```

Deploy:

```bash
scripts/install-keda.sh
kubectl apply -k infra/k8s/overlays/gke-4node-demo
kubectl get pods -n judgemesh -o wide
```

Note:

- This overlay is aimed at demonstrating distributed judging.
- `mysql`, `minio`, `redis`, `rabbitmq`, and `nacos` are still single-instance in this MVP.
