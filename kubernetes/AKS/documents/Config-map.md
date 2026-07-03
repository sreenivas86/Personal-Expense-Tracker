# Kubernetes ConfigMap Explained (Azure AKS)

## ConfigMap Manifest

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-vars
  namespace: e-track-dev

data:
  database-name: expense_tracker
  app-name: Expense-Tracker
  db-host: mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local
  db-port: "3306"
  db-username: root
  app-port: "8080"
```

---

# What is a ConfigMap?

A **ConfigMap** is a Kubernetes resource used to store **non-sensitive configuration data** as key-value pairs.

Instead of hardcoding configuration values such as:

- Database name
- Database host
- Database port
- Application name
- Application port

inside the application code or Docker image, Kubernetes stores them in a ConfigMap and injects them into Pods at runtime.

This makes the application portable and easier to manage across different environments such as Development, Testing, and Production.

---

# Why Use a ConfigMap?

Without a ConfigMap, configuration values are hardcoded.

Example:

```properties
spring.datasource.url=jdbc:mysql://192.168.1.10:3306/expense_tracker
spring.datasource.username=root
```

If the database changes, the application must be rebuilt.

With a ConfigMap:

```
ConfigMap
      │
      ▼
Spring Boot Pod
```

The application reads configuration dynamically without rebuilding the Docker image.

---

# Manifest Breakdown

## apiVersion

```yaml
apiVersion: v1
```

Specifies the Kubernetes Core API version.

The **v1** API contains resources such as:

- Namespace
- Pod
- Service
- ConfigMap
- Secret
- PersistentVolumeClaim

---

## kind

```yaml
kind: ConfigMap
```

Creates a Kubernetes ConfigMap.

A ConfigMap stores application configuration that is **not confidential**.

Examples include:

- Application name
- Database host
- Database name
- Database port
- Feature flags
- Environment variables

---

## metadata

```yaml
metadata:
```

Contains information about the ConfigMap.

---

### name

```yaml
name: app-vars
```

Defines the ConfigMap name.

Your Deployment references this ConfigMap:

```yaml
env:
- name: APP_NAME
  valueFrom:
    configMapKeyRef:
      name: app-vars
      key: app-name
```

---

### namespace

```yaml
namespace: e-track-dev
```

Creates the ConfigMap in the **e-track-dev** namespace.

Only Pods within this namespace can reference it directly.

---

# data Section

The **data** section stores configuration as key-value pairs.

---

## database-name

```yaml
database-name: expense_tracker
```

Specifies the MySQL database name.

Spring Boot receives this value as:

```
DB_NAME=expense_tracker
```

Application uses:

```
expense_tracker
```

database during startup.

---

## app-name

```yaml
app-name: Expense-Tracker
```

Stores the application name.

Injected into the application as:

```
APP_NAME=Expense-Tracker
```

Useful for:

- Logging
- Monitoring
- Displaying the application name
- Environment identification

---

## db-host

```yaml
db-host: mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local
```

Specifies the fully qualified domain name (FQDN) of the MySQL StatefulSet Pod.

This DNS name is automatically created by Kubernetes because MySQL is deployed as a StatefulSet with a Headless Service.

DNS Format:

```
<pod-name>.<service-name>.<namespace>.svc.cluster.local
```

Breaking it down:

| Component | Value | Description |
|-----------|-------|-------------|
| Pod Name | mysql-sfs-0 | First MySQL Pod |
| Service Name | mysql-sfs | Headless Service |
| Namespace | e-track-dev | Kubernetes Namespace |
| Cluster Domain | svc.cluster.local | Default Kubernetes DNS domain |

Complete DNS Name:

```
mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local
```

This provides a stable hostname for the MySQL Pod.

---

## db-port

```yaml
db-port: "3306"
```

Specifies the MySQL port.

The value is enclosed in quotes because **ConfigMap values are always stored as strings**.

Spring Boot receives:

```
DB_PORT=3306
```

---

## db-username

```yaml
db-username: root
```

Specifies the MySQL username.

The application receives:

```
DB_USERNAME=root
```

The password is intentionally **not stored here**.

Instead, it is stored securely in a Kubernetes Secret.

---

## app-port

```yaml
app-port: "8080"
```

Specifies the port on which the Spring Boot application listens.

Again, it is quoted because ConfigMap values are strings.

Application receives:

```
APP_PORT=8080
```

---

# Why are Port Numbers Quoted?

In ConfigMaps, **all values are treated as strings**.

Correct:

```yaml
db-port: "3306"
```

Incorrect:

```yaml
db-port: 3306
```

Although Kubernetes often converts numbers automatically, quoting values avoids parsing issues and follows best practices.

---

# How the ConfigMap is Used

Your Deployment references the ConfigMap:

```yaml
env:
- name: DB_HOST
  valueFrom:
    configMapKeyRef:
      name: app-vars
      key: db-host
```

Kubernetes injects the value into the container as an environment variable.

Example:

```
ConfigMap
      │
      ▼
DB_HOST=mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local
      │
      ▼
Spring Boot Application
```

---

# Relationship Between ConfigMap and Deployment

```
                ConfigMap
                 app-vars
                     │
       ┌─────────────┼─────────────┐
       │             │             │
       ▼             ▼             ▼
 APP_NAME       DB_HOST      DB_PORT
       │             │             │
       └─────────────┼─────────────┘
                     │
                     ▼
         Spring Boot Deployment
                     │
                     ▼
             Spring Boot Pods
```

---

# ConfigMap vs Secret

| ConfigMap | Secret |
|------------|--------|
| Stores non-sensitive data | Stores sensitive data |
| Plain text | Base64 encoded (not encrypted by default) |
| App name | Passwords |
| Database host | API keys |
| Database name | Tokens |
| Port numbers | Certificates |

In your project:

**ConfigMap**

- Database Name
- Database Host
- Database Port
- Username
- Application Name
- Application Port

**Secret**

- Database Password

---

# Useful Commands

## Create ConfigMap

```bash
kubectl apply -f configmap.yaml
```

---

## View ConfigMaps

```bash
kubectl get configmap -n e-track-dev
```

---

## Describe ConfigMap

```bash
kubectl describe configmap app-vars -n e-track-dev
```

---

## View ConfigMap as YAML

```bash
kubectl get configmap app-vars -n e-track-dev -o yaml
```

---

## Verify Environment Variables Inside Pod

```bash
kubectl exec -it <pod-name> -n e-track-dev -- env
```

Example output:

```
APP_NAME=Expense-Tracker
DB_HOST=mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local
DB_PORT=3306
DB_NAME=expense_tracker
DB_USERNAME=root
APP_PORT=8080
```

---

# Architecture Diagram

```text
                ConfigMap
                 app-vars
                     │
      +--------------+---------------+
      |              |               |
      ▼              ▼               ▼
  APP_NAME      DB_HOST         DB_PORT
      │              │               │
      +--------------+---------------+
                     │
                     ▼
          Spring Boot Deployment
                     │
                     ▼
            Spring Boot Pods
                     │
                     ▼
         MySQL StatefulSet
```

---

# Summary

| Field | Description |
|--------|-------------|
| `apiVersion: v1` | Uses the Kubernetes Core API |
| `kind: ConfigMap` | Creates a ConfigMap resource |
| `metadata.name` | ConfigMap name (`app-vars`) |
| `namespace` | Creates the ConfigMap in the `e-track-dev` namespace |
| `database-name` | MySQL database name (`expense_tracker`) |
| `app-name` | Application name (`Expense-Tracker`) |
| `db-host` | Fully Qualified Domain Name (FQDN) of the MySQL StatefulSet Pod |
| `db-port` | MySQL port (`3306`) |
| `db-username` | MySQL username (`root`) |
| `app-port` | Spring Boot application port (`8080`) |
| **Purpose** | Stores non-sensitive configuration values that are injected into the Spring Boot application as environment variables, enabling flexible configuration without rebuilding the Docker image. |