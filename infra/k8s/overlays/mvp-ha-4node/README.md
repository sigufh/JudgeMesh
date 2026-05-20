# MVP HA 4-Node Overlay

This overlay keeps only the components needed for an MVP that still demonstrates:

- distributed judging
- stateless service high availability
- dispatcher leader election

Topology:

- `2` nodes labeled `role=app`
- `2` nodes labeled `role=judge`

Recommended labels:

```bash
kubectl label node APP_NODE_1 role=app
kubectl label node APP_NODE_2 role=app
kubectl label node JUDGE_NODE_1 role=judge
kubectl label node JUDGE_NODE_2 role=judge
```

Behavior:

- app services prefer `app` nodes and run with `2` replicas
- `judge-dispatcher` runs with `3` replicas
- `judge-worker` runs with `2` replicas and is restricted to `judge` nodes
- ingress does not require a fixed hostname
- nonessential components are excluded: `cert-manager`, `grafana-dashboards`, `keda`

Deploy:

```bash
kubectl apply -k infra/k8s/overlays/mvp-ha-4node
kubectl get pods -A -o wide
```

Known scope limits:

- `mysql`, `minio`, `redis`, `rabbitmq`, and `nacos` are still single-instance in this MVP.
- This overlay targets business-path HA and distributed judging, not full HA for every stateful dependency.
