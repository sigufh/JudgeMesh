#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${KEDA_NAMESPACE:-keda}"
RELEASE_NAME="${KEDA_RELEASE_NAME:-keda}"
VALUES_FILE="${VALUES_FILE:-infra/helm/values/keda.yaml}"

command -v helm >/dev/null 2>&1 || { echo "missing helm" >&2; exit 127; }
command -v kubectl >/dev/null 2>&1 || { echo "missing kubectl" >&2; exit 127; }

helm repo add kedacore https://kedacore.github.io/charts >/dev/null
helm repo update >/dev/null
helm upgrade --install "$RELEASE_NAME" kedacore/keda \
  --namespace "$NAMESPACE" \
  --create-namespace \
  -f "$VALUES_FILE"

kubectl get pods -n "$NAMESPACE"
