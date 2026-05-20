#!/usr/bin/env bash
set -euo pipefail

CONFIRM="${1:-}"
POD_CIDR="${POD_CIDR:-10.244.0.0/16}"
JOIN_COMMAND_FILE="${JOIN_COMMAND_FILE:-/tmp/judgemesh-kubeadm-join.sh}"
CALICO_MANIFEST_URL="${CALICO_MANIFEST_URL:-https://raw.githubusercontent.com/projectcalico/calico/v3.29.3/manifests/calico.yaml}"
FLANNEL_MANIFEST_URL="${FLANNEL_MANIFEST_URL:-https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml}"
CNI_PROVIDER="${CNI_PROVIDER:-calico}"

command -v kubeadm >/dev/null 2>&1 || { echo "missing kubeadm" >&2; exit 127; }
command -v kubectl >/dev/null 2>&1 || { echo "missing kubectl" >&2; exit 127; }

if [[ "$CONFIRM" != "--confirm" ]]; then
  echo "dry run: kubeadm init --pod-network-cidr=$POD_CIDR"
  echo "cni provider: $CNI_PROVIDER"
  echo "join command file: $JOIN_COMMAND_FILE"
  echo "rerun with --confirm to initialize this node"
  exit 0
fi

sudo kubeadm init --pod-network-cidr="$POD_CIDR"
mkdir -p "$HOME/.kube"
sudo cp -f /etc/kubernetes/admin.conf "$HOME/.kube/config"
sudo chown "$(id -u):$(id -g)" "$HOME/.kube/config"

case "$CNI_PROVIDER" in
  calico)
    kubectl apply -f "$CALICO_MANIFEST_URL"
    ;;
  flannel)
    kubectl apply -f "$FLANNEL_MANIFEST_URL"
    ;;
  *)
    echo "unsupported CNI_PROVIDER: $CNI_PROVIDER" >&2
    exit 2
    ;;
esac

kubectl taint nodes --all node-role.kubernetes.io/control-plane- || true
sudo kubeadm token create --print-join-command | tee "$JOIN_COMMAND_FILE" >/dev/null

cat <<EOF
cluster bootstrap complete
- kubeconfig: $HOME/.kube/config
- join command saved to: $JOIN_COMMAND_FILE
- next steps:
  1. label judge nodes: kubectl label node <node> role=judge
  2. taint judge nodes: kubectl taint nodes <node> role=judge:NoSchedule
  3. install ingress-nginx / cert-manager / helm charts
  4. apply overlays: kubectl apply -k infra/k8s/overlays/dev
EOF

kubectl get nodes -o wide
