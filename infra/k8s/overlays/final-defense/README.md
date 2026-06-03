# Final Defense Overlay

This is the single recommended deployment shape for the final defense version.

Why this overlay exists:

- It matches the current requirement boundary: distributed judging, stateless service HA, queue-based autoscaling, and observable degradation behavior.
- It avoids introducing unverified last-minute stateful cluster refactors into `MySQL`, `Redis`, `RabbitMQ`, `MinIO`, and `Nacos`.
- It is the fastest stable path for a four-node GKE answer-defense environment.

Recommended topology:

- `2` nodes labeled `role=app`
- `2` nodes labeled `role=judge`
- judge nodes should also be tainted with `role=judge:NoSchedule`

Execution policy:

- `frontend`, `gateway`, `user-service`, `problem-service`, and `submit-service` run with `2` replicas and prefer `app` nodes.
- `judge-dispatcher` runs with `3` replicas and etcd leader election.
- `judge-worker` keeps `JUDGE_MAX_CONCURRENCY=1` per Pod and scales horizontally through KEDA.
- Multiple worker Pods on one judge node are allowed under pressure; this is the intended answer-defense scaling strategy.
- `mysql`, `redis`, `rabbitmq`, `minio`, and `nacos` remain single-instance in this phase, with persistence, backup, DLQ, and recovery procedures handled operationally.

Additional resilience:

- PodDisruptionBudgets are added for the main stateless services, worker pool, and etcd.
- CPU-based HPAs are added for `gateway`, `problem-service`, and `submit-service`.
- Worker queue scaling remains handled by KEDA.

Prepare nodes:

```bash
kubectl get nodes

kubectl label node APP_NODE_1 role=app
kubectl label node APP_NODE_2 role=app
kubectl label node JUDGE_NODE_1 role=judge
kubectl label node JUDGE_NODE_2 role=judge

kubectl taint node JUDGE_NODE_1 role=judge:NoSchedule
kubectl taint node JUDGE_NODE_2 role=judge:NoSchedule
```

Deploy:

```bash
scripts/install-keda.sh
kubectl apply -k infra/k8s/overlays/final-defense
kubectl get pods -A -o wide
```

Validation:

```bash
kubectl get scaledobject -n judgemesh
kubectl get hpa -n judgemesh
kubectl get pdb -A
kubectl get pods -n judgemesh -o wide
```

Related guide:

- [`docs/dev/03-Google-Cloud-四节点-最终答辩部署指南.md`](/D:/JudgeMesh/docs/dev/03-Google-Cloud-四节点-最终答辩部署指南.md:1)
- [`docs/dev/04-Google-Cloud-CLI-命令清单.md`](/D:/JudgeMesh/docs/dev/04-Google-Cloud-CLI-命令清单.md:1)
