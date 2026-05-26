#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-judgemesh}"
INFRA_NAMESPACE="${INFRA_NAMESPACE:-judgemesh-infra}"

command -v kubectl >/dev/null 2>&1 || { echo "missing kubectl" >&2; exit 127; }

pids=()
cleanup() {
  for pid in "${pids[@]:-}"; do
    kill "$pid" >/dev/null 2>&1 || true
  done
}
trap cleanup EXIT INT TERM

forward() {
  local namespace="$1"
  local target="$2"
  local ports="$3"
  echo "forward $namespace/$target $ports"
  kubectl -n "$namespace" port-forward "$target" "$ports" &
  pids+=("$!")
}

forward "$NAMESPACE" "svc/gateway" "8080:8080"
forward "$NAMESPACE" "svc/frontend" "5173:80"
forward "$INFRA_NAMESPACE" "svc/nacos" "8848:8848"
forward "$INFRA_NAMESPACE" "svc/prometheus-grafana" "3000:80"
forward "$INFRA_NAMESPACE" "svc/skywalking-oap" "8088:8080"

echo "port-forwards ready; press Ctrl-C to stop"
wait
