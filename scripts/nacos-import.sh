#!/usr/bin/env bash
set -euo pipefail

NACOS_ADDR="${NACOS_ADDR:-127.0.0.1:8848}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"

base_url="http://${NACOS_ADDR}/nacos/v1/cs/configs"

publish() {
  local data_id="$1"
  local content="$2"
  curl -fsS -X POST "${base_url}" \
    -u "${NACOS_USER}:${NACOS_PASSWORD}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${GROUP}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content=${content}" >/dev/null
  printf 'published %s\n' "${data_id}"
}

publish application.yaml "$(cat <<'YAML'
spring:
  cloud:
    sentinel:
      transport:
        dashboard: ${SENTINEL_DASHBOARD:sentinel-dashboard.judgemesh-infra.svc:8858}
seata:
  enabled: ${SEATA_ENABLED:false}
  tx-service-group: judgemesh-tx-group
  service:
    vgroup-mapping:
      judgemesh-tx-group: default
    grouplist:
      default: ${SEATA_SERVER_ADDR:seata-server.judgemesh-infra.svc:8091}
YAML
)"

publish gateway.yaml "$(cat <<'YAML'
judgemesh:
  gateway:
    submit-limit-per-minute: ${SUBMIT_LIMIT_PER_MINUTE:30}
YAML
)"

publish problem-service.yaml "$(cat <<'YAML'
judgemesh:
  problem:
    seed-placeholder-enabled: false
    cache:
      max-size: 2000
      detail-ttl-seconds: 300
YAML
)"
