# infra/k8s/base/ — 基础 manifest

每个目录代表一个部署单元。当前 base 已包含 `frontend`、`gateway`、`user-service`、`problem-service`、`submit-service`、`judge-dispatcher`、`judge-worker` 与 `ingress`,可通过 overlays 一次性渲染。

## 业务服务对应端口

| 服务              | 端口 | 命名空间   |
| ----------------- | ---- | ---------- |
| gateway           | 8080 | judgemesh  |
| user-service      | 8081 | judgemesh  |
| problem-service   | 8082 | judgemesh  |
| submit-service    | 8083 | judgemesh  |
| judge-dispatcher  | 8084 | judgemesh  |
| judge-worker      | 8090 | judgemesh  |

## 中间件命名空间

| 组件                                              | 部署方式                           | 命名空间          |
| ------------------------------------------------- | ---------------------------------- | ----------------- |
| nacos                                             | nacos-k8s helm chart               | judgemesh-infra   |
| mysql                                             | StatefulSet + PVC                  | judgemesh-infra   |
| redis                                             | StatefulSet + PVC                  | judgemesh-infra   |
| rabbitmq                                          | RabbitMQ Cluster Operator          | judgemesh-infra   |
| etcd                                              | StatefulSet 3 副本                 | judgemesh-infra   |
| minio                                             | MinIO Operator                     | judgemesh-infra   |
| seata-server                                      | StatefulSet                        | judgemesh-infra   |
| ingress-nginx                                     | helm                               | ingress-nginx     |
| cert-manager                                      | helm                               | cert-manager      |
| prometheus / grafana / alertmanager / skywalking / loki | helm(详见 ../helm/values/)  | judgemesh-observe |
| chaos-mesh                                        | helm                               | chaos-mesh        |

base 目录已包含业务 Deployment/Service、探针、资源配额、worker HPA 与 ingress;中间件安装参数在 `infra/helm/values/` 和 `infra/local/` 下维护。
