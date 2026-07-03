# Kubernetes StatefulSet Explained (Azure AKS)

## StatefulSet Manifest

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql-sfs
  namespace: e-track-dev

spec:
  selector:
    matchLabels:
      app: mysql-sfs

  serviceName: "mysql-sfs"

  replicas: 2

  template:
    metadata:
      labels:
        app: mysql-sfs

    spec:
      terminationGracePeriodSeconds: 10

      containers:
      - name: mysql-sfs
        image: mysql:8-oracle
        imagePullPolicy: IfNotPresent

        ports:
        - containerPort: 3306
          name: mysql

        volumeMounts:
        - name: mysql-data
          mountPath: /var/lib/mysql

        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: sql-password

        - name: MYSQL_DATABASE
          valueFrom:
            configMapKeyRef:
              name: app-vars
              key: database-name

        resources:
          requests:
            cpu: "250m"
            memory: "512Mi"
          limits:
            cpu: "1"
            memory: "1Gi"

        startupProbe:
          exec:
            command:
            - sh
            - -c
            - mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"
          failureThreshold: 30
          periodSeconds: 10

        readinessProbe:
          exec:
            command:
            - sh
            - -c
            - mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"
          initialDelaySeconds: 10
          periodSeconds: 10

        livenessProbe:
          exec:
            command:
            - sh
            - -c
            - mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"
          initialDelaySeconds: 30
          periodSeconds: 20

  volumes:
  - name: mysql-data
    persistentVolumeClaim:
      claimName: mysql-pvc
```

---

# What is a StatefulSet?

A **StatefulSet** is a Kubernetes workload resource used to manage **stateful applications** that require:

- Persistent storage
- Stable network identity
- Predictable pod names
- Ordered deployment and scaling

Unlike a Deployment, a StatefulSet ensures that each Pod has its own identity and storage.

For databases like **MySQL**, StatefulSet is the recommended controller because databases need persistent data and stable hostnames.

---

# Why Use a StatefulSet for MySQL?

Databases must preserve data across:

- Pod restarts
- Node failures
- Cluster upgrades
- Scaling operations

StatefulSet provides:

- Stable Pod names
- Stable DNS names
- Persistent storage
- Ordered startup and shutdown

Example:

```
mysql-sfs-0
mysql-sfs-1
```

Even after a restart, the Pod names remain the same.

---

# Manifest Breakdown

## apiVersion

```yaml
apiVersion: apps/v1
```

Uses the Kubernetes Apps API for workload resources such as:

- Deployment
- StatefulSet
- DaemonSet
- ReplicaSet

---

## kind

```yaml
kind: StatefulSet
```

Creates a StatefulSet.

Unlike a Deployment, Pods are created with unique identities and stable hostnames.

---

## metadata

### Name

```yaml
name: mysql-sfs
```

Defines the StatefulSet name.

Pods created will be:

```
mysql-sfs-0
mysql-sfs-1
```

---

### Namespace

```yaml
namespace: e-track-dev
```

Creates the StatefulSet inside the **e-track-dev** namespace.

---

# spec Section

The `spec` defines the desired state of the StatefulSet.

---

## selector

```yaml
selector:
  matchLabels:
    app: mysql-sfs
```

The selector identifies which Pods belong to the StatefulSet.

Every Pod with:

```yaml
labels:
  app: mysql-sfs
```

is managed by this StatefulSet.

---

## serviceName

```yaml
serviceName: mysql-sfs
```

References the **Headless Service** associated with the StatefulSet.

This enables stable DNS names for each Pod.

DNS format:

```
<pod-name>.<service-name>.<namespace>.svc.cluster.local
```

Examples:

```
mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local

mysql-sfs-1.mysql-sfs.e-track-dev.svc.cluster.local
```

Applications can reliably connect to a specific MySQL Pod using these DNS names.

---

## replicas

```yaml
replicas: 2
```

Requests two MySQL Pods:

```
mysql-sfs-0

mysql-sfs-1
```

### Note

For a **standard MySQL deployment**, running two replicas is **not recommended** unless you have configured MySQL replication (Primary/Replica or Group Replication).

If both replicas mount the **same PVC** (`mysql-pvc`) with `ReadWriteOnce`, only one Pod can mount the volume at a time, causing the second Pod to remain in a `Pending` state.

For a single MySQL instance, use:

```yaml
replicas: 1
```

To run multiple MySQL instances, each replica should have its **own PersistentVolumeClaim**, typically created using `volumeClaimTemplates`.

---

## template

Defines the Pod template used to create MySQL Pods.

---

### labels

```yaml
labels:
  app: mysql-sfs
```

Must match the selector.

---

# Container

## Name

```yaml
name: mysql-sfs
```

Container name inside the Pod.

---

## Image

```yaml
image: mysql:8-oracle
```

Uses the official MySQL 8 Oracle container image.

---

## imagePullPolicy

```yaml
imagePullPolicy: IfNotPresent
```

Behavior:

- Pull image if not available locally.
- Reuse cached image if already downloaded.

---

# Container Port

```yaml
ports:
- containerPort: 3306
```

Exposes the MySQL server on port **3306**.

---

# Volume Mount

```yaml
volumeMounts:
- name: mysql-data
  mountPath: /var/lib/mysql
```

Mounts the persistent storage at:

```
/var/lib/mysql
```

This is where MySQL stores:

- Databases
- Tables
- Indexes
- Transaction logs

---

# Environment Variables

## MYSQL_ROOT_PASSWORD

```yaml
MYSQL_ROOT_PASSWORD
```

Reads the MySQL root password from the Kubernetes Secret.

```yaml
secretKeyRef:
  name: app-secrets
```

---

## MYSQL_DATABASE

```yaml
MYSQL_DATABASE
```

Reads the database name from the ConfigMap.

```yaml
configMapKeyRef:
  name: app-vars
```

MySQL automatically creates:

```
expense_tracker
```

during initialization.

---

# Resource Requests and Limits

```yaml
requests:
  cpu: "250m"
  memory: "512Mi"

limits:
  cpu: "1"
  memory: "1Gi"
```

### Requests

Minimum resources reserved:

- CPU: 250 millicores
- Memory: 512 MiB

### Limits

Maximum resources allowed:

- CPU: 1 Core
- Memory: 1 GiB

---

# Health Probes

## Startup Probe

```yaml
startupProbe
```

Checks whether MySQL has successfully started.

Command:

```bash
mysqladmin ping
```

If startup takes too long, Kubernetes restarts the container.

---

## Readiness Probe

```yaml
readinessProbe
```

Determines whether MySQL is ready to receive connections.

Until this probe succeeds:

- Service does not send traffic to the Pod.

---

## Liveness Probe

```yaml
livenessProbe
```

Continuously checks whether MySQL is still healthy.

If the probe fails repeatedly:

- Kubernetes restarts the container automatically.

---

# Volumes

```yaml
volumes:
- name: mysql-data
  persistentVolumeClaim:
    claimName: mysql-pvc
```

Attaches the existing PersistentVolumeClaim (`mysql-pvc`) to the container.

The volume is mounted at:

```
/var/lib/mysql
```

ensuring database files persist across Pod restarts.

> **Important:** Because the PVC uses the `ReadWriteOnce` access mode, a single Azure Managed Disk can only be attached to one node at a time. Using `replicas: 2` with one shared PVC is not suitable. Use `replicas: 1` or configure `volumeClaimTemplates` to create a separate PVC for each replica.

---

# StatefulSet Lifecycle

```
Create StatefulSet
        │
        ▼
Create mysql-sfs-0
        │
        ▼
Wait Until Ready
        │
        ▼
Create mysql-sfs-1
```

Pods are created and terminated **in order**, ensuring stable database operations.

---

# Architecture Diagram

```text
                     StatefulSet
                      mysql-sfs
                           │
          ┌────────────────┴────────────────┐
          │                                 │
          ▼                                 ▼
     mysql-sfs-0                      mysql-sfs-1
          │                                 │
          ▼                                 ▼
      MySQL Pod                        MySQL Pod
          │                                 │
          └──────────────┬──────────────────┘
                         │
              (Not recommended with one PVC)
                         │
                         ▼
              PersistentVolumeClaim
                    mysql-pvc
                         │
                         ▼
              Azure Managed Disk
```

---

# Useful Commands

## Create StatefulSet

```bash
kubectl apply -f statefulset.yaml
```

---

## View StatefulSets

```bash
kubectl get statefulsets -n e-track-dev
```

---

## View Pods

```bash
kubectl get pods -n e-track-dev
```

---

## Describe StatefulSet

```bash
kubectl describe statefulset mysql-sfs -n e-track-dev
```

---

## Check Logs

```bash
kubectl logs mysql-sfs-0 -n e-track-dev
```

---

# Best Practice for Azure AKS

For a single MySQL instance:

```yaml
replicas: 1
```

For multiple MySQL replicas:

- Configure MySQL replication (Primary/Replica or Group Replication).
- Use `volumeClaimTemplates` so each replica receives its own Azure Managed Disk.

---

# Summary

| Field | Description |
|--------|-------------|
| `apiVersion: apps/v1` | Uses the Kubernetes Apps API |
| `kind: StatefulSet` | Creates a StatefulSet for stateful workloads |
| `serviceName` | References the Headless Service used for stable DNS |
| `replicas` | Number of MySQL Pods to create |
| `selector` | Matches Pods managed by the StatefulSet |
| `image` | Uses the official `mysql:8-oracle` container image |
| `volumeMounts` | Mounts persistent storage at `/var/lib/mysql` |
| `Secret` | Supplies the MySQL root password securely |
| `ConfigMap` | Supplies the database name |
| `startupProbe` | Verifies successful startup |
| `readinessProbe` | Ensures the Pod is ready to receive traffic |
| `livenessProbe` | Restarts unhealthy containers automatically |
| `PersistentVolumeClaim` | Provides durable Azure Managed Disk storage |
| **Purpose** | Deploys MySQL as a stateful application with persistent storage, stable network identity, and automated health monitoring on Azure Kubernetes Service (AKS). |