# Kubernetes Expense Tracker Documentation

## Contents
1. Namespace
2. StorageClass
3. PersistentVolumeClaim
4. MySQL Headless Service
5. MySQL StatefulSet
6. Spring Boot Deployment
7. Spring Boot LoadBalancer Service

---

# 1. Namespace

A Namespace logically isolates resources in a Kubernetes cluster.

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: e-track-dev
  labels:
    name: development
```

- Creates the `e-track-dev` environment.
- Separates development resources from other environments.
- Enables resource organization and RBAC.

---

# 2. StorageClass

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: mysql-premium-sc
provisioner: pd.csi.storage.gke.io
reclaimPolicy: Retain
volumeBindingMode: WaitForFirstConsumer
allowVolumeExpansion: true
parameters:
  type: pd-balanced
```

- Uses the GKE Persistent Disk CSI driver.
- Dynamically provisions storage.
- Retains disks after PVC deletion.
- Waits until a Pod is scheduled before creating storage.
- Supports online volume expansion.

---

# 3. PersistentVolumeClaim (PVC)

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  storageClassName: mysql-premium-sc
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 4Gi
```

- Requests 4Gi persistent storage.
- Uses the `mysql-premium-sc` StorageClass.
- Mounted by the MySQL StatefulSet.
- Preserves database files across Pod restarts.

---

# 4. MySQL Headless Service

```yaml
clusterIP: None
selector:
  app: mysql-sfs
ports:
- port: 3306
```

- Required for StatefulSets.
- Provides stable DNS names.
- No load balancing.
- Routes directly to MySQL Pods.

---

# 5. MySQL StatefulSet

Key features:

- Stable Pod name (`mysql-sfs-0`)
- Persistent storage using `mysql-pvc`
- Reads configuration from ConfigMap and Secret
- Startup, readiness, and liveness probes
- Graceful shutdown
- Uses MySQL 8 Oracle image

Data is stored under `/var/lib/mysql`.

---

# 6. Spring Boot Deployment

Key features:

- Runs 2 replicas.
- Uses an initContainer to wait for MySQL.
- Pulls image `sree471/e-track-app:latest`.
- Reads configuration from ConfigMap.
- Reads database password from Secret.
- Exposes port 8080.
- Defines CPU and memory requests/limits.

---

# 7. Spring Boot LoadBalancer Service

```yaml
type: LoadBalancer
selector:
  app: e-track-app
ports:
- port: 8080
  targetPort: 8080
```

- Creates a Google Cloud Load Balancer.
- Assigns a public IP.
- Routes traffic to Spring Boot Pods.
- Distributes requests across replicas.

---

# Overall Architecture

```text
                 Internet
                     |
          Google Cloud Load Balancer
                     |
        Service (e-track-app-svc)
                     |
              Spring Boot Deployment
             /                     \
         Pod-1                  Pod-2
                |
          Headless Service
             (mysql-sfs)
                |
        MySQL StatefulSet
          mysql-sfs-0
                |
        PersistentVolumeClaim
           (mysql-pvc)
                |
         StorageClass
     (mysql-premium-sc)
                |
   Google Persistent Disk
```

# Summary

This project deploys a Spring Boot Expense Tracker on GKE. The application is exposed through a LoadBalancer Service, connects to a MySQL database managed by a StatefulSet, and stores data on a persistent Google Persistent Disk provisioned dynamically using a StorageClass. Configuration is managed through ConfigMaps and Secrets, while an initContainer ensures the application starts only after MySQL is ready.
