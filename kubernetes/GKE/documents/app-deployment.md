# Kubernetes Deployment Explained

## Deployment Manifest

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: e-track-app-dep
  namespace: e-track-dev
spec:
  replicas: 2
  selector:
    matchLabels:
      app: e-track-app

  template:
    metadata:
      labels:
        app: e-track-app

    spec:
      initContainers:
        - name: wait-for-mysql
          image: busybox:1.36
          command:
            - sh
            - -c
            - |
              until nc -z mysql-sfs 3306;
              do
                echo "Waiting for MySQL...";
                sleep 5;
              done;
              echo "MySQL is ready!"

      containers:
        - name: e-track-app
          image: sree471/e-track-app:latest
          imagePullPolicy: Always

          ports:
            - containerPort: 8080

          env:
            - name: APP_NAME
              valueFrom:
                configMapKeyRef:
                  name: app-vars
                  key: app-name

            - name: DB_HOST
              valueFrom:
                configMapKeyRef:
                  name: app-vars
                  key: db-host

            - name: DB_PORT
              valueFrom:
                configMapKeyRef:
                  name: app-vars
                  key: db-port

            - name: DB_NAME
              valueFrom:
                configMapKeyRef:
                  name: app-vars
                  key: database-name

            - name: DB_USERNAME
              valueFrom:
                configMapKeyRef:
                  name: app-vars
                  key: db-username

            - name: APP_PORT
              valueFrom:
                configMapKeyRef:
                  name: app-vars
                  key: app-port

            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: sql-password

          resources:
            requests:
              cpu: "100m"
              memory: "256Mi"

            limits:
              cpu: "1"
              memory: "1Gi"
```

---

# What is a Deployment?

A **Deployment** is a Kubernetes workload resource used to manage **stateless applications**.

It provides features such as:

- Running multiple replicas
- Self-healing
- Rolling updates
- Rollbacks
- Automatic Pod recreation

Your Spring Boot Expense Tracker application is stateless, making a Deployment the appropriate choice.

---

# Deployment vs StatefulSet

| Deployment | StatefulSet |
|------------|-------------|
| Used for stateless applications | Used for stateful applications |
| Pod names change when recreated | Pod names remain stable |
| No stable storage | Persistent storage |
| Suitable for Spring Boot | Suitable for MySQL |

In your project:

```
Spring Boot
        │
        ▼
Deployment

MySQL
      │
      ▼
StatefulSet
```

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
kind: Deployment
```

Creates a Deployment resource.

---

## 3. metadata

```yaml
metadata:
  name: e-track-app-dep
  namespace: e-track-dev
```

### name

The Deployment is named:

```
e-track-app-dep
```

Pods created by this Deployment will have names similar to:

```
e-track-app-dep-697b446d6c-m7kwb
e-track-app-dep-697b446d6c-zfxrj
```

---

### namespace

Deploys the application into:

```
e-track-dev
```

---

# spec Section

Defines the desired state of the Deployment.

---

## 4. replicas

```yaml
replicas: 2
```

Creates **two identical application Pods**.

```
Deployment
      │
      ├──────────────┐
      ▼              ▼
Pod-1           Pod-2
```

Benefits:

- High Availability
- Load Balancing
- Fault Tolerance

If one Pod crashes, the other continues serving requests.

---

## 5. selector

```yaml
selector:
  matchLabels:
    app: e-track-app
```

The Deployment manages Pods with:

```yaml
labels:
  app: e-track-app
```

The selector and Pod labels must match.

---

# Pod Template

Defines how Pods should be created.

---

## 6. Labels

```yaml
labels:
  app: e-track-app
```

These labels are used by:

- Deployment
- Service

Your application Service uses:

```yaml
selector:
  app: e-track-app
```

This allows the Service to discover the Pods.

---

# initContainers

```yaml
initContainers:
```

An **initContainer** runs **before** the main application container starts.

It must complete successfully before Kubernetes starts the main container.

---

## wait-for-mysql

```yaml
name: wait-for-mysql
```

The initContainer is named:

```
wait-for-mysql
```

---

## Image

```yaml
image: busybox:1.36
```

Uses the lightweight BusyBox image because it contains shell utilities like:

- sh
- nc (netcat)

---

## Command

```bash
until nc -z mysql-sfs 3306
```

The initContainer repeatedly checks whether MySQL is accepting TCP connections.

Flow:

```
Application Pod
       │
       ▼
Init Container Starts
       │
       ▼
Can connect to MySQL?
       │
   No ─────► Wait 5 seconds
       │
      Yes
       ▼
Exit Successfully
       ▼
Spring Boot Starts
```

Without this initContainer, the Spring Boot application might start before MySQL is ready and fail to connect.

---

# Main Container

## Name

```yaml
name: e-track-app
```

Names the application container.

---

## Image

```yaml
image: sree471/e-track-app:latest
```

Docker image stored in Docker Hub.

---

## imagePullPolicy

```yaml
imagePullPolicy: Always
```

Always downloads the latest image before starting.

Useful during development because every deployment uses the newest version.

Options:

- Always
- IfNotPresent
- Never

---

# Container Port

```yaml
ports:
  - containerPort: 8080
```

Your Spring Boot application listens on:

```
8080
```

You verified this using:

```bash
kubectl exec -it <pod> -- netstat -tln
```

Output:

```
0.0.0.0:8080 LISTEN
```

---

# Environment Variables

Instead of hardcoding configuration, Kubernetes injects values from ConfigMaps and Secrets.

---

## ConfigMap Variables

These values come from **app-vars**.

```yaml
APP_NAME
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
APP_PORT
```

Example:

```
ConfigMap
     │
     ▼
Application
```

---

## Secret Variable

```yaml
DB_PASSWORD
```

Loaded securely from:

```
Secret
      │
      ▼
DB_PASSWORD
```

Passwords should always be stored in Secrets instead of ConfigMaps.

---

# Resource Requests and Limits

```yaml
requests:
```

Minimum resources guaranteed.

```
CPU: 100m

Memory: 256Mi
```

---

```yaml
limits:
```

Maximum resources the Pod can consume.

```
CPU: 1 Core

Memory: 1 GiB
```

Benefits:

- Prevents one Pod from consuming excessive resources.
- Helps Kubernetes schedule Pods efficiently.

---

# Deployment Workflow

```
Deployment
      │
      ▼
ReplicaSet
      │
      ├─────────────┐
      ▼             ▼
Pod-1          Pod-2
      │             │
      ▼             ▼
Init Container  Init Container
      │             │
Wait for MySQL Wait for MySQL
      │             │
      ▼             ▼
Spring Boot    Spring Boot
```

---

# How It Works in Your Project

```
User
   │
   ▼
LoadBalancer Service
   │
   ▼
Deployment
   │
   ├──────────────┐
   ▼              ▼
Spring Boot   Spring Boot
   │
   ▼
Headless Service
(mysql-sfs)
   │
   ▼
MySQL StatefulSet
```

---

# Benefits of This Deployment

- Runs two application instances.
- Automatically recreates failed Pods.
- Waits for MySQL before starting the application.
- Uses ConfigMaps for configuration.
- Uses Secrets for sensitive information.
- Pulls the latest Docker image automatically.
- Limits CPU and memory usage.
- Supports rolling updates and scaling.

---

# Useful Commands

## View Deployment

```bash
kubectl get deployment -n e-track-dev
```

---

## View Pods

```bash
kubectl get pods -n e-track-dev
```

---

## Describe Deployment

```bash
kubectl describe deployment e-track-app-dep -n e-track-dev
```

---

## View Logs

```bash
kubectl logs deployment/e-track-app-dep -n e-track-dev
```

---

## Scale Deployment

```bash
kubectl scale deployment e-track-app-dep --replicas=3 -n e-track-dev
```

---

## Restart Deployment

```bash
kubectl rollout restart deployment e-track-app-dep -n e-track-dev
```

---

## Check Rollout Status

```bash
kubectl rollout status deployment e-track-app-dep -n e-track-dev
```

---

# Architecture Diagram

```
                   +---------------------------+
                   |        Deployment         |
                   |    e-track-app-dep        |
                   +-------------+-------------+
                                 |
                       Replicas = 2
                                 |
               +-----------------+-----------------+
               |                                   |
               ▼                                   ▼
       +---------------+                   +---------------+
       | Spring Boot   |                   | Spring Boot   |
       | Pod-1         |                   | Pod-2         |
       +-------+-------+                   +-------+-------+
               |                                   |
               | Wait for MySQL                    |
               +---------------+-------------------+
                               |
                               ▼
                    +---------------------+
                    | Headless Service    |
                    | mysql-sfs           |
                    +----------+----------+
                               |
                               ▼
                    +---------------------+
                    | MySQL StatefulSet   |
                    | mysql-sfs-0         |
                    +---------------------+
```

---

# Summary

| Field | Purpose |
|--------|---------|
| `Deployment` | Manages the Spring Boot application |
| `replicas: 2` | Runs two application Pods for high availability |
| `selector` | Identifies Pods managed by the Deployment |
| `labels` | Used by the Deployment and Service to select Pods |
| `initContainer` | Waits until MySQL is ready before starting Spring Boot |
| `busybox:1.36` | Lightweight image used to check MySQL availability |
| `image` | Docker image for the Spring Boot application |
| `imagePullPolicy: Always` | Always pulls the latest image from Docker Hub |
| `containerPort: 8080` | Application listens on port 8080 |
| `ConfigMap` | Provides application and database configuration |
| `Secret` | Provides the database password securely |
| `resources` | Defines CPU and memory requests and limits |
| **Purpose** | Deploys a scalable, highly available Spring Boot application that waits for MySQL before starting and connects securely using ConfigMaps and Secrets. |