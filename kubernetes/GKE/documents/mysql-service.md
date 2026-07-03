# Kubernetes Headless Service Explained

## Service Manifest

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-sfs
  namespace: e-track-dev
spec:
  clusterIP: None
  selector:
    app: mysql-sfs
  ports:
    - port: 3306
      targetPort: 3306
      name: mysql
```

---

# What is a Service?

A **Service** in Kubernetes provides a stable network endpoint for accessing one or more Pods.

Normally, Pods have dynamic IP addresses that change whenever they are recreated. A Service solves this problem by providing a consistent DNS name and IP address.

In your project, this Service allows the Spring Boot application to communicate with the MySQL StatefulSet.

---

# Why a Headless Service?

Notice this line:

```yaml
clusterIP: None
```

This makes it a **Headless Service**.

Unlike a normal Service, a Headless Service **does not get a Cluster IP**.

Instead, Kubernetes returns the IP addresses of the Pods directly.

A Headless Service is required when using a **StatefulSet** because each Pod must have a stable network identity.

---

# Line-by-Line Explanation

## 1. apiVersion

```yaml
apiVersion: v1
```

Specifies the Kubernetes core API version.

---

## 2. kind

```yaml
kind: Service
```

Creates a Kubernetes Service resource.

---

## 3. metadata

```yaml
metadata:
  name: mysql-sfs
  namespace: e-track-dev
```

### name

Assigns the Service name.

Applications can connect to MySQL using:

```
mysql-sfs
```

instead of using the Pod IP.

For example:

```properties
spring.datasource.url=jdbc:mysql://mysql-sfs:3306/expense_tracker
```

---

### namespace

Creates the Service inside the **e-track-dev** namespace.

The fully qualified DNS name becomes:

```
mysql-sfs.e-track-dev.svc.cluster.local
```

Within the same namespace, applications can simply use:

```
mysql-sfs
```

---

# spec Section

Defines how the Service routes traffic.

---

## 4. clusterIP

```yaml
clusterIP: None
```

This is the most important configuration.

Normally, a Service receives a virtual Cluster IP.

Example:

```
ClusterIP Service

Service IP
10.96.10.20
      │
      ▼
Pod A
Pod B
Pod C
```

With:

```yaml
clusterIP: None
```

there is **no virtual IP**.

Instead, Kubernetes returns the Pod IP directly.

Example:

```
Headless Service

mysql-sfs
      │
      ▼
mysql-sfs-0
10.110.0.15
```

This allows StatefulSet Pods to have predictable DNS names.

---

## 5. selector

```yaml
selector:
  app: mysql-sfs
```

The selector tells the Service which Pods it should expose.

Your StatefulSet Pods have:

```yaml
labels:
  app: mysql-sfs
```

Because the labels match, Kubernetes automatically creates Service endpoints.

You verified this using:

```bash
kubectl get endpoints mysql-sfs -n e-track-dev
```

Example output:

```
NAME         ENDPOINTS
mysql-sfs    10.110.0.15:3306
```

If the selector doesn't match the Pod labels, the endpoints will be empty:

```
NAME         ENDPOINTS
mysql-sfs    <none>
```

This is exactly the type of issue you encountered earlier with your application Service.

---

## 6. ports

```yaml
ports:
```

Defines which ports the Service exposes.

---

### port

```yaml
port: 3306
```

The port clients use when connecting to the Service.

Example:

```
mysql-sfs:3306
```

---

### targetPort

```yaml
targetPort: 3306
```

The port on which the MySQL container is listening.

Flow:

```
Application
     │
mysql-sfs:3306
     │
     ▼
Service
     │
     ▼
MySQL Pod
3306
```

---

### name

```yaml
name: mysql
```

Provides a friendly name for the port.

This becomes useful when multiple ports are exposed by the same Service.

---

# DNS Resolution

Since this is a StatefulSet, Kubernetes creates stable DNS entries.

```
mysql-sfs-0.mysql-sfs.e-track-dev.svc.cluster.local
```

or simply

```
mysql-sfs-0.mysql-sfs
```

Applications inside the namespace can connect using:

```
mysql-sfs
```

or

```
mysql-sfs-0.mysql-sfs
```

---

# How This Works in Your Project

```
Spring Boot Application
        │
        │
        ▼
DB_HOST=mysql-sfs
        │
        ▼
Headless Service
(mysql-sfs)
        │
        ▼
StatefulSet
(mysql-sfs-0)
        │
        ▼
MySQL Database
```

Your Deployment gets the database host from the ConfigMap:

```yaml
DB_HOST=mysql-sfs
```

When the application starts:

```
Spring Boot
      │
      ▼
mysql-sfs
      │
      ▼
Headless Service
      │
      ▼
mysql-sfs-0
      │
      ▼
MySQL
```

---

# Why Use a Headless Service with a StatefulSet?

A Headless Service provides:

- Stable DNS names
- Direct access to StatefulSet Pods
- No load balancing
- Predictable network identity

This is ideal for databases because clients often need to connect to a specific database instance.

---

# Useful Commands

## View the Service

```bash
kubectl get svc -n e-track-dev
```

---

## Describe the Service

```bash
kubectl describe svc mysql-sfs -n e-track-dev
```

---

## View Service Endpoints

```bash
kubectl get endpoints mysql-sfs -n e-track-dev
```

Example:

```
NAME         ENDPOINTS
mysql-sfs    10.110.0.15:3306
```

---

## Test DNS Resolution

```bash
kubectl exec -it <spring-boot-pod> -n e-track-dev -- nslookup mysql-sfs
```

Example output:

```
Name: mysql-sfs
Address: 10.110.0.15
```

---

# Architecture

```
                +---------------------------+
                | Spring Boot Application   |
                +------------+--------------+
                             |
                   DB_HOST=mysql-sfs
                             |
                             ▼
                +---------------------------+
                | Headless Service          |
                | mysql-sfs                |
                | ClusterIP: None          |
                +------------+--------------+
                             |
                             ▼
                +---------------------------+
                | StatefulSet              |
                | mysql-sfs-0             |
                +------------+--------------+
                             |
                             ▼
                +---------------------------+
                | MySQL Database           |
                +---------------------------+
```

---

# Summary

| Field | Purpose |
|--------|---------|
| `apiVersion: v1` | Uses the Kubernetes core API |
| `kind: Service` | Creates a Service resource |
| `metadata.name` | Service name (`mysql-sfs`) |
| `namespace` | Creates the Service in `e-track-dev` |
| `clusterIP: None` | Makes it a Headless Service with no virtual IP |
| `selector` | Connects the Service to Pods labeled `app: mysql-sfs` |
| `port: 3306` | Port clients use to connect |
| `targetPort: 3306` | Forwards traffic to the MySQL container |
| `name: mysql` | Friendly name for the Service port |
| **Purpose** | Provides stable DNS and direct Pod access for the MySQL StatefulSet, enabling the Spring Boot application to reliably connect to the database. |