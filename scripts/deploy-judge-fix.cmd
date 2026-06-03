@echo off
setlocal

set PROJECT_ID=project-ad762830-bf41-4f10-b00
set REGION=asia-east1
set REPO=judgemesh
set TAG=fix-20260603-b63b632
set IMAGE_PREFIX=%REGION%-docker.pkg.dev/%PROJECT_ID%/%REPO%
set CLUSTER_NAME=judgemesh-gke
set CLUSTER_ZONE=asia-east1-b

echo == JudgeMesh fix deploy ==
echo PROJECT_ID=%PROJECT_ID%
echo IMAGE_PREFIX=%IMAGE_PREFIX%
echo TAG=%TAG%
echo.

echo [1/10] Set gcloud project
gcloud config set project %PROJECT_ID%
if errorlevel 1 exit /b 1

echo [2/10] Enable required APIs
gcloud services enable artifactregistry.googleapis.com cloudbuild.googleapis.com
if errorlevel 1 exit /b 1

echo [3/10] Configure Artifact Registry auth
gcloud auth configure-docker %REGION%-docker.pkg.dev --quiet
if errorlevel 1 exit /b 1

echo [4/10] Install shared API jar
mvn -pl services/api -DskipTests install
if errorlevel 1 exit /b 1

echo [5/10] Build and push judge-dispatcher
mvn -pl services/judge-dispatcher -DskipTests jib:build "-Djib.to.image=%IMAGE_PREFIX%/judgemesh-judge-dispatcher:%TAG%"
if errorlevel 1 exit /b 1

echo [6/10] Build and push judge-worker via Cloud Build
gcloud builds submit services/judge-worker --tag %IMAGE_PREFIX%/judge-worker:%TAG%
if errorlevel 1 exit /b 1

echo [7/10] Connect kubectl to cluster
gcloud container clusters get-credentials %CLUSTER_NAME% --zone %CLUSTER_ZONE% --project %PROJECT_ID%
if errorlevel 1 exit /b 1

echo [8/10] Stabilize worker and dispatcher replica counts
kubectl patch scaledobject judge-worker-queue-scaler -n judgemesh --type merge -p "{\"spec\":{\"minReplicaCount\":3,\"maxReplicaCount\":3}}"
if errorlevel 1 exit /b 1
kubectl scale deploy/judge-worker -n judgemesh --replicas=3
if errorlevel 1 exit /b 1
kubectl scale deploy/judge-dispatcher -n judgemesh --replicas=1
if errorlevel 1 exit /b 1

echo [9/10] Switch deployments to fixed images
kubectl set image deploy/judge-worker -n judgemesh worker=%IMAGE_PREFIX%/judge-worker:%TAG%
if errorlevel 1 exit /b 1
kubectl set image deploy/judge-dispatcher -n judgemesh app=%IMAGE_PREFIX%/judgemesh-judge-dispatcher:%TAG%
if errorlevel 1 exit /b 1
kubectl delete pod -n judgemesh -l app=judge-worker --field-selector=status.phase=Pending
kubectl delete pod -n judgemesh -l app=judge-dispatcher --field-selector=status.phase=Pending

echo [10/10] Wait for rollout
kubectl rollout status deploy/judge-worker -n judgemesh --timeout=10m
if errorlevel 1 exit /b 1
kubectl rollout status deploy/judge-dispatcher -n judgemesh --timeout=10m
if errorlevel 1 exit /b 1

echo.
echo == Final status ==
kubectl get deploy judge-worker judge-dispatcher -n judgemesh
kubectl get pods -n judgemesh -l app=judge-worker -o wide
kubectl get pods -n judgemesh -l app=judge-dispatcher -o wide

endlocal
