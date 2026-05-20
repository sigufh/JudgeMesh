#!/usr/bin/env bash
set -euo pipefail

CONFIRM="${1:-}"
POD_CIDR="${POD_CIDR:-10.244.0.0/16}"

command -v kubeadm >/dev/null 2>&1 || { echo "missing kubeadm" >&2; exit 127; }
command -v kubectl >/dev/null 2>&1 || { echo "missing kubectl" >&2; exit 127; }

if [[ "$CONFIRM" != "--confirm" ]]; then
  echo "dry run: kubeadm init --pod-network-cidr=$POD_CIDR"
  echo "rerun with --confirm to initialize this node"
  exit 0
fi

sudo kubeadm init --pod-network-cidr="$POD_CIDR"
mkdir -p "$HOME/.kube"
sudo cp -f /etc/kubernetes/admin.conf "$HOME/.kube/config"
sudo chown "$(id -u):$(id -g)" "$HOME/.kube/config"
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
kubectl get nodes -o wide
