# Kubernetes Deployment Explained (Azure AKS)

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

A **Deployment** is a Kubernetes workload resource that manages **stateless applications**.

It provides:

- Automatic Pod creation
- Scaling
- Rolling updates
- Self-healing
- High availability

In this project, the Deployment manages the **Spring Boot Expense Tracker application**.

---

# Why Use a Deployment?

Unlike a Pod, a Deployment ensures the desired number of application instances are always running.

If a Pod fails:

```
Pod Crash
    │
    ▼
Deployment detects failure
    │
    ▼
Creates a new Pod automatically
```

This improves application availability and reliability.

---

# Manifest Breakdown

## apiVersion

```yaml
apiVersion: apps/v1
```

Uses the Kubernetes Apps API for workload resources.

Resources under this API include:

- Deployment
- StatefulSet
- DaemonSet
- ReplicaSet

---

## kind

```yaml
kind: Deployment
```

Creates a Deployment resource.

The Deployment manages ReplicaSets, which in turn manage Pods.

---

## metadata

### Name

```yaml
name: e-track-app-dep
```

Defines the Deployment name.

---

### Namespace

```yaml
namespace: e-track-dev
```

Deploys the application into the **e-track-dev** namespace.

---

# spec Section

Defines the desired state of the Deployment.

---

## replicas

```yaml
replicas: 2
```

Runs **two instances** of the Spring Boot application.

Example:

```
e-track-app-dep

│
├── Pod 1
└── Pod 2
```

Benefits:

- High Availability
- Load Distribution
- Zero Downtime Updates

If one Pod fails, the second continues serving requests while Kubernetes creates a replacement.

---

## selector

```yaml
selector:
  matchLabels:
    app: e-track-app
```

The Deployment identifies its Pods using labels.

Every Pod with:

```yaml
labels:
  app: e-track-app
```

belongs to this Deployment.

---

## template

Defines the Pod template.

Every Pod created by the Deployment is based on this template.

---

### Labels

```yaml
labels:
  app: e-track-app
```

Must match the selector.

---

# initContainer

```yaml
initContainers:
```

An **Init Container** runs **before** the main application container.

The main container starts **only after** all Init Containers complete successfully.

---

## wait-for-mysql

```yaml
name: wait-for-mysql
```

This Init Container waits until the MySQL database is ready.

---

## Image

```yaml
image: busybox:1.36
```

Uses the lightweight BusyBox image.

---

## Command

```bash
until nc -z mysql-sfs 3306
```

The command continuously checks whether the MySQL Service is accepting TCP connections on port **3306**.

Workflow:

```
Start Pod
     │
     ▼
Run Init Container
     │
     ▼
Check MySQL Port 3306
     │
     ├── Not Ready
     │      │
     │      ▼
     │   Sleep 5 Seconds
     │
     └── Ready
            │
            ▼
Start Spring Boot Container
```

This prevents the application from starting before MySQL is available.

---

# Main Application Container

## Name

```yaml
name: e-track-app
```

Container name.

---

## Image

```yaml
image: sree471/e-track-app:latest
```

Uses the Docker image containing the Spring Boot application.

---

## imagePullPolicy

```yaml
imagePullPolicy: Always
```

Kubernetes always checks the container registry for the latest image before starting the Pod.

Useful during development because new images are pulled automatically.

---

# Container Port

```yaml
ports:
- containerPort: 8080
```

Exposes the Spring Boot application on port **8080**.

This port is later exposed through the Kubernetes Service.

---

# Environment Variables

The application receives configuration from both a **ConfigMap** and a **Secret**.

---

## ConfigMap Values

The following values are loaded from the ConfigMap (`app-vars`):

### APP_NAME

```yaml
APP_NAME
```

Application name.

---

### DB_HOST

```yaml
DB_HOST
```

Fully Qualified Domain Name (FQDN) of the MySQL StatefulSet.

Example:

```
mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local
```

---

### DB_PORT

```yaml
DB_PORT
```

MySQL port.

```
3306
```

---

### DB_NAME

```yaml
DB_NAME
```

Database name.

```
expense_tracker
```

---

### DB_USERNAME

```yaml
DB_USERNAME
```

MySQL username.

```
root
```

---

### APP_PORT

```yaml
APP_PORT
```

Spring Boot application port.

```
8080
```

---

## Secret Value

### DB_PASSWORD

```yaml
DB_PASSWORD
```

Loaded securely from the Kubernetes Secret:

```yaml
secretKeyRef:
  name: app-secrets
```

The password is not stored in the Deployment manifest.

---

# Resource Requests and Limits

```yaml
resources:
```

Defines CPU and memory requirements.

---

## Requests

```yaml
cpu: "100m"
memory: "256Mi"
```

Minimum resources guaranteed by Kubernetes.

- CPU: 0.1 Core
- Memory: 256 MiB

---

## Limits

```yaml
cpu: "1"
memory: "1Gi"
```

Maximum resources the container can consume.

- CPU: 1 Core
- Memory: 1 GiB

If the container exceeds these limits, Kubernetes throttles CPU usage or may terminate the container if it exceeds the memory limit.

---

# Deployment Workflow

```
Deployment
      │
      ▼
ReplicaSet
      │
      ▼
2 Pods
      │
      ▼
Init Container
(wait-for-mysql)
      │
      ▼
Main Spring Boot Container
      │
      ▼
Connects to MySQL
```

---

# Relationship with Other Resources

```
                     Deployment
                  e-track-app-dep
                         │
                 Creates 2 Pods
                         │
        ┌────────────────┴────────────────┐
        ▼                                 ▼
 Spring Boot Pod                    Spring Boot Pod
        │                                 │
        └───────────────┬─────────────────┘
                        │
          Reads ConfigMap & Secret
                        │
          ┌─────────────┴─────────────┐
          ▼                           ▼
      ConfigMap                   Secret
      app-vars                 app-secrets
          │                           │
          └─────────────┬─────────────┘
                        │
                        ▼
              MySQL StatefulSet
```

---

# High Availability

With two replicas:

```
Internet
     │
     ▼
Service
     │
 ┌───┴────┐
 ▼        ▼
Pod 1   Pod 2
```

Traffic is automatically distributed across both Pods by the Kubernetes Service.

---

# Useful Commands

## Create Deployment

```bash
kubectl apply -f deployment.yaml
```

---

## View Deployments

```bash
kubectl get deployments -n e-track-dev
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

Increase replicas:

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

```text
                      Deployment
                   e-track-app-dep
                           │
                  ReplicaSet
                           │
          ┌────────────────┴────────────────┐
          ▼                                 ▼
     Spring Boot Pod                  Spring Boot Pod
          │                                 │
     Init Container                    Init Container
   wait-for-mysql                    wait-for-mysql
          │                                 │
          ▼                                 ▼
    Spring Boot App                  Spring Boot App
          │                                 │
          └──────────────┬──────────────────┘
                         │
                  Kubernetes Service
                         │
                         ▼
                 MySQL StatefulSet
                         │
                         ▼
               Azure Managed Disk
```

---

# Summary

| Field | Description |
|--------|-------------|
| `apiVersion: apps/v1` | Uses the Kubernetes Apps API |
| `kind: Deployment` | Creates a Deployment for the Spring Boot application |
| `replicas: 2` | Runs two application Pods for high availability |
| `selector` | Identifies Pods managed by the Deployment |
| `initContainer` | Waits until the MySQL Service is reachable before starting the application |
| `image` | Uses the `sree471/e-track-app:latest` container image |
| `imagePullPolicy: Always` | Always pulls the latest image from the container registry |
| `containerPort: 8080` | Exposes the Spring Boot application port |
| `ConfigMap` | Provides non-sensitive configuration such as database host, port, and application name |
| `Secret` | Securely provides the database password |
| `resources` | Defines CPU and memory requests and limits |
| **Purpose** | Deploys a highly available Spring Boot application on Azure Kubernetes Service (AKS), ensuring it starts only after MySQL is available and receives its configuration securely through ConfigMaps and Secrets. |
```