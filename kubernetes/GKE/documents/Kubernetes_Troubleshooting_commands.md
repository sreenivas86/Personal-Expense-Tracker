# Kubernetes Troubleshooting Commands

## 1. Verify all resources

``` bash
kubectl get all -n e-track-dev
```

Checks Pods, Services, Deployments, ReplicaSets, and StatefulSets.

## 2. Check application logs

``` bash
kubectl logs deployment/e-track-app-dep -n e-track-dev
kubectl logs pod/<pod-name> -n e-track-dev
```

## 3. Describe a Pod

``` bash
kubectl describe pod <pod-name> -n e-track-dev
```

Shows events, image pull errors, init container status.

## 4. View init container logs

``` bash
kubectl logs <pod-name> -c wait-for-mysql -n e-track-dev
```

## 5. Describe the Service

``` bash
kubectl describe svc e-track-app-svc -n e-track-dev
```

## 6. Check Endpoints

``` bash
kubectl get endpoints e-track-app-svc -n e-track-dev
```

Initially:

``` text
ENDPOINTS <none>
```

After fixing selector:

``` text
10.x.x.x:8080
```

## 7. Verify application is listening

``` bash
kubectl exec -it <pod-name> -n e-track-dev -- netstat -tln
```

Expected:

``` text
0.0.0.0:8080 LISTEN
```

## 8. Inspect Deployment

``` bash
kubectl get deployment e-track-app-dep -n e-track-dev -o yaml
```

## 9. Build Docker image

``` bash
docker build -t sree471/e-track-app:latest .
```

Or:

``` bash
docker build -f Dockerfile.multi -t sree471/e-track-app:latest .
```

## 10. Verify local image

``` bash
docker images
```

## 11. Push image

``` bash
docker push sree471/e-track-app:latest
```

## 12. Restart Deployment

``` bash
kubectl rollout restart deployment e-track-app-dep -n e-track-dev
```

## 13. Verify Pods

``` bash
kubectl get pods -n e-track-dev
```

## 14. Port-forward (optional)

``` bash
kubectl port-forward svc/e-track-app-svc 8080:8080 -n e-track-dev
```

## 15. Apply manifests

``` bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f statefulset.yaml
```

# Issues Resolved

  -------------------------------------------------------------------------------
  Problem                             Commands
  ----------------------------------- -------------------------------------------
  ImagePullBackOff                    `kubectl describe pod`, `docker images`,
                                      `docker push`, `kubectl rollout restart`

  MySQL wait/init                     `kubectl logs -c wait-for-mysql`,
                                      `kubectl describe pod`

  Service selector mismatch           `kubectl describe svc`,
                                      `kubectl get endpoints`

  App startup                         `kubectl logs deployment/e-track-app-dep`

  Verify listening port               `kubectl exec -- netstat -tln`

  Cluster status                      `kubectl get all -n e-track-dev`
  -------------------------------------------------------------------------------
