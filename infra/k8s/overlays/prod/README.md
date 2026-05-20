# Production Overlay Notes

This overlay is tuned for course-project deployment:

- Secrets already use fixed demo values from the base secret templates.
- The ingress rule has no fixed host, so you can access it by server IP or bind your own domain later.
- Change image repositories in `kustomization.yaml` under `images:` before deploying if you push to your own registry.

Typical deployment flow:

```bash
kubectl apply -k infra/k8s/overlays/prod
kubectl get pods -A
kubectl get svc -A
kubectl get ingress -A
```
