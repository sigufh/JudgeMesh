# infra/ — 基础设施资源(@sigufh 负责)

| 子目录                   | 内容                                              |
| ------------------------ | ------------------------------------------------- |
| `k8s/base/<component>/`  | 每个组件的 Deployment / Service / ConfigMap       |
| `k8s/overlays/{dev,prod}/` | Kustomize 环境差异(副本数、资源、镜像 tag)     |
| `helm/values/`           | Helm chart values(Prom / Grafana / SkyWalking / Loki / Chaos Mesh) |
| `grafana/dashboards/`    | 4 个大盘 JSON(业务总览 / 判题流水线 / 治理 / 基础设施) |
| `chaos/`                 | ChaosMesh 实验脚本(杀 worker / 杀 leader / MQ down) |

详见 [docs/design/08-部署架构.md](../docs/design/08-部署架构.md)。

当前约定:

- `infra/k8s/base/` 放业务服务与中间件最小可运行资源。
- `infra/k8s/overlays/dev` 用于联调，默认单副本、低资源。
- `infra/k8s/overlays/prod` 用于演示/答辩，包含完整中间件和 autoscaling。
- Helm 安装命令统一引用 `infra/helm/values/*.yaml`，不在 README 之外维护第二套参数。
