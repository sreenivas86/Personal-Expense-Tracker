# Kubernetes StatefulSet Explained

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

  replicas: 1

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
            - name: mysql-storage
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
              cpu: "150m"
              memory: "256Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"

          startupProbe:
            exec:
              command:
                - sh
                - -c
                - mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"

          readinessProbe:
            exec:
              command:
                - sh
                - -c
                - mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"

          livenessProbe:
            exec:
              command:
                - sh
                - -c
                - mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"

      volumes:
        - name: mysql-storage
          persistentVolumeClaim:
            claimName: mysql-pvc
```

---

# What is a StatefulSet?

A **StatefulSet** is a Kubernetes workload resource used to manage **stateful applications** such as:

- MySQL
- PostgreSQL
- MongoDB
- Cassandra
- Kafka
- Redis

Unlike a Deployment, a StatefulSet provides:

- Stable Pod names
- Stable network identity
- Persistent storage
- Ordered deployment and termination

It is the preferred choice for databases because they need persistent data and consistent identities.

---

# Why StatefulSet Instead of Deployment?

A Deployment creates interchangeable Pods.

Example:

```
Deployment

mysql-7c45f8d98d-abc12
mysql-7c45f8d98d-xyz45
```

Pod names change whenever they are recreated.

For databases, changing Pod names can cause issues.

A StatefulSet creates predictable Pod names:

```
mysql-sfs-0
mysql-sfs-1
mysql-sfs-2
```

These names remain the same even after restarts.

---

# Line-by-Line Explanation

## 1. apiVersion

```yaml
apiVersion: apps/v1
```

Specifies the Kubernetes API version for workload resources.

---

## 2. kind

```yaml
kind: StatefulSet
```

Creates a StatefulSet resource.

---

## 3. metadata

```yaml
metadata:
  name: mysql-sfs
  namespace: e-track-dev
```

### name

Defines the StatefulSet name.

Pods will be created as:

```
mysql-sfs-0
mysql-sfs-1
mysql-sfs-2
```

### namespace

Deploys the StatefulSet into the **e-track-dev** namespace.

---

# spec Section

Defines the desired state of the StatefulSet.

---

## 4. selector

```yaml
selector:
  matchLabels:
    app: mysql-sfs
```

The selector tells Kubernetes which Pods belong to this StatefulSet.

Pods created by the StatefulSet have:

```yaml
labels:
  app: mysql-sfs
```

These labels must match.

---

## 5. serviceName

```yaml
serviceName: mysql-sfs
```

A StatefulSet requires a **Headless Service**.

The corresponding Service is:

```yaml
kind: Service
metadata:
  name: mysql-sfs

spec:
  clusterIP: None
```

This provides stable DNS names.

Example:

```
mysql-sfs-0.mysql-sfs
```

instead of dynamic Pod IPs.

---

## 6. replicas

```yaml
replicas: 1
```

Specifies the number of MySQL Pods.

Here:

```
mysql-sfs-0
```

Only one database instance is created.

---

# Pod Template

Defines the Pod that the StatefulSet creates.

---

## 7. labels

```yaml
labels:
  app: mysql-sfs
```

Used by the StatefulSet selector and the Service selector.

---

## 8. terminationGracePeriodSeconds

```yaml
terminationGracePeriodSeconds: 10
```

Allows MySQL **10 seconds** to shut down gracefully.

Without this, Kubernetes might terminate the container abruptly, risking data corruption.

---

# Container Configuration

---

## 9. Container Name

```yaml
name: mysql-sfs
```

Names the container inside the Pod.

---

## 10. Image

```yaml
image: mysql:8-oracle
```

Uses the MySQL 8 Oracle Docker image.

---

## 11. imagePullPolicy

```yaml
imagePullPolicy: IfNotPresent
```

Kubernetes pulls the image only if it is not already available on the node.

Options:

- Always
- IfNotPresent
- Never

---

## 12. Container Port

```yaml
ports:
  - containerPort: 3306
```

Exposes MySQL on port **3306**.

---

# Persistent Storage

## volumeMounts

```yaml
volumeMounts:
  - name: mysql-storage
    mountPath: /var/lib/mysql
```

Mounts the Persistent Volume inside the container.

MySQL stores all database files in:

```
/var/lib/mysql
```

---

## volumes

```yaml
volumes:
  - name: mysql-storage
    persistentVolumeClaim:
      claimName: mysql-pvc
```

Connects the StatefulSet to the existing PVC.

Workflow:

```
PVC
 │
 ▼
Persistent Volume
 │
 ▼
Google Persistent Disk
 │
 ▼
Mounted at /var/lib/mysql
```

---

# Environment Variables

## Root Password

```yaml
MYSQL_ROOT_PASSWORD
```

Loaded securely from a Kubernetes Secret.

```
Secret
 │
 ▼
MYSQL_ROOT_PASSWORD
```

---

## Database Name

```yaml
MYSQL_DATABASE
```

Loaded from a ConfigMap.

```
ConfigMap
 │
 ▼
MYSQL_DATABASE
```

This creates the initial database automatically.

---

# Resource Requests and Limits

```yaml
requests:
  cpu: 150m
  memory: 256Mi

limits:
  cpu: 500m
  memory: 512Mi
```

### Requests

Minimum resources guaranteed.

### Limits

Maximum resources the container can consume.

This prevents MySQL from exhausting cluster resources.

---

# Startup Probe

```yaml
startupProbe:
```

Checks whether MySQL has completed startup.

Command:

```bash
mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"
```

If MySQL is still initializing, Kubernetes waits instead of restarting it.

---

# Readiness Probe

```yaml
readinessProbe:
```

Determines whether MySQL is ready to accept connections.

Only after this probe succeeds will Kubernetes send traffic to the Pod.

---

# Liveness Probe

```yaml
livenessProbe:
```

Checks whether MySQL is still healthy.

If the probe fails repeatedly, Kubernetes automatically restarts the container.

---

# How the StatefulSet Works

```
StatefulSet
      │
      ▼
mysql-sfs-0
      │
      ▼
Headless Service
(mysql-sfs)
      │
      ▼
PersistentVolumeClaim
(mysql-pvc)
      │
      ▼
Persistent Volume
      │
      ▼
Google Persistent Disk
```

---

# Lifecycle

```
Create StatefulSet
        │
        ▼
Create Pod
mysql-sfs-0
        │
        ▼
Mount PVC
        │
        ▼
Initialize MySQL
        │
        ▼
Startup Probe
        │
        ▼
Readiness Probe
        │
        ▼
Receive Traffic
        │
        ▼
Liveness Probe
        │
Restart if unhealthy
```

---

# Why This Configuration is Good

- Uses a StatefulSet for MySQL.
- Provides a stable Pod name.
- Uses persistent storage.
- Stores the password securely in a Secret.
- Reads configuration from a ConfigMap.
- Uses health probes for reliability.
- Limits CPU and memory usage.
- Allows graceful shutdown.

---

# Summary

| Field | Purpose |
|--------|---------|
| `StatefulSet` | Manages stateful applications like MySQL |
| `serviceName` | Associates a Headless Service for stable network identity |
| `replicas: 1` | Creates one MySQL instance |
| `containerPort: 3306` | Exposes the MySQL service |
| `volumeMounts` | Mounts persistent storage at `/var/lib/mysql` |
| `PersistentVolumeClaim` | Provides durable storage |
| `MYSQL_ROOT_PASSWORD` | Loads the root password from a Secret |
| `MYSQL_DATABASE` | Loads the initial database name from a ConfigMap |
| `startupProbe` | Waits for MySQL to finish initialization |
| `readinessProbe` | Marks the Pod ready only after MySQL accepts connections |
| `livenessProbe` | Restarts MySQL automatically if it becomes unhealthy |
| `resources` | Reserves and limits CPU and memory usage |
| **Purpose** | Runs a reliable, stateful MySQL database with persistent storage and automatic health monitoring |