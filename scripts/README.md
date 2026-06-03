# scripts/ - Operations And Utility Scripts

| Script | Purpose | Owner | Status |
| ------ | ------- | ----- | ------ |
| `k8s-bootstrap.sh` | kubeadm cluster bootstrap guard script, dry-run by default | @sigufh | READY |
| `import-problems.py` | bulk import 50 demo problems and test cases | @KY-raika | READY |
| `loadtest-submit.lua` | wrk load test for submit path | @sigufh + @Nier291 | READY |
| `demo-distributed-load.mjs` | concurrent submit demo with worker distribution stats | @Phoen1xCode + @Nier291 | READY |
| `seed-demo-data.mjs` | seed users, submission history, contest leaderboard data | @sigufh | READY |
| `gen-jwt.sh` | login and print a test JWT | @Phoen1xCode | READY |
| `port-forward.sh` | forward Gateway / Frontend / Nacos / Grafana / SkyWalking | @sigufh | READY |
| `smoke-test.sh` | health, login, problem list, and submit enqueue smoke check | all | READY |
| `install-keda.sh` | install the KEDA operator required by worker queue autoscaling | infra | READY |

## Common Commands

```bash
python3 scripts/import-problems.py --service http://localhost:8082 --limit 50
TOKEN="$(BASE_URL=http://localhost:8080 scripts/gen-jwt.sh)"
wrk -t4 -c32 -d60s -s scripts/loadtest-submit.lua http://localhost:8080
node scripts/demo-distributed-load.mjs --total 60 --concurrency 30
node scripts/seed-demo-data.mjs
scripts/port-forward.sh
scripts/k8s-bootstrap.sh            # dry-run
scripts/k8s-bootstrap.sh --confirm  # actual kubeadm init
scripts/install-keda.sh
```

## Final Defense

- Recommended overlay: `infra/k8s/overlays/final-defense`
- Deployment guide: [`docs/dev/03-Google-Cloud-四节点-最终答辩部署指南.md`](/D:/JudgeMesh/docs/dev/03-Google-Cloud-四节点-最终答辩部署指南.md:1)
- Worker strategy: keep `JUDGE_MAX_CONCURRENCY=1`, scale out with KEDA, and allow multiple worker Pods on the same judge node when queue pressure rises

## Submit Load Test Variables

```bash
USER_ID=1001 PROBLEM_ID=1 CONTEST_ID=100001 LANGUAGE=cpp \
  wrk -t4 -c50 -d5m -s scripts/loadtest-submit.lua http://127.0.0.1:8083
```
