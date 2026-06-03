# infra/k8s/base/ — 基础 manifest

每个目录代表一个部署单元。当前 base 已包含业务服务、中间件、命名空间、Grafana 大盘 ConfigMap 与 autoscaling 配置，可通过 overlays 一次性渲染。

## 业务服务对应端口

| 服务              | 端口 | 命名空间   |
| ----------------- | ---- | ---------- |
| gateway           | 8080 | judgemesh  |
| user-service      | 8081 | judgemesh  |
| problem-service   | 8082 | judgemesh  |
| submit-service    | 8083 | judgemesh  |
| judge-dispatcher  | 8084 | judgemesh  |
| judge-worker      | 8090 | judgemesh  |

## 平台组件

| 组件                                              | 交付位置                           | 命名空间          |
| ------------------------------------------------- | ---------------------------------- | ----------------- |
| namespaces / 基础 secret 模板                     | `base/namespaces` `base/app-secrets` `base/infra-secrets` | 多命名空间 |
| nacos                                             | `base/nacos`                       | judgemesh-infra   |
| redis                                             | `base/redis`                       | judgemesh-infra   |
| rabbitmq                                          | `base/rabbitmq`                    | judgemesh-infra   |
| etcd                                              | `base/etcd`                        | judgemesh-infra   |
| cert-manager issuer                               | `base/cert-manager`                | cert-manager      |
| ingress-nginx metrics service                     | `base/ingress-nginx`               | ingress-nginx     |
| judge-worker queue autoscaling                    | `base/keda`                        | judgemesh         |
| grafana dashboards ConfigMap                      | `base/grafana-dashboards`          | judgemesh-observe |
| prometheus / grafana / alertmanager / skywalking / loki | helm values(详见 ../helm/values/) | judgemesh-observe |
| chaos-mesh                                        | helm values + `infra/chaos/`       | chaos-mesh        |

业务服务保持 Deployment/Service + 探针的基础模式；中间件以 StatefulSet + PVC 为主；可观测性和混沌组件继续通过 Helm values 安装。

注意:

- `app-secrets` 与 `infra-secrets` 里的 `change-me` 仅用于模板占位，实际部署前必须改成外部注入或 CI secret 渲染。
- `prod`、`mvp-ha-4node`、`gke-4node-demo` overlays 包含 KEDA `ScaledObject`，依赖集群已安装 KEDA CRD/Operator。
