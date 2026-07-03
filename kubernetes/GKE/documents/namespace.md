# Kubernetes Namespace Explained

## Namespace Manifest

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: e-track-dev
  labels:
    name: development
```

---

# What is a Namespace?

A **Namespace** is a logical partition within a Kubernetes cluster that allows you to organize and isolate resources.

Instead of creating separate Kubernetes clusters for different environments, you can create multiple namespaces within a single cluster.

For example:

```
Kubernetes Cluster
│
├── default
├── kube-system
├── kube-public
├── e-track-dev
├── e-track-test
└── e-track-prod
```

Each namespace contains its own resources such as Pods, Services, Deployments, ConfigMaps, and Secrets.

---

# Line-by-Line Explanation

## 1. apiVersion

```yaml
apiVersion: v1
```

Specifies the Kubernetes API version used for creating a Namespace.

- `v1` is the stable core API version.
- Namespaces are part of the Kubernetes core API.

---

## 2. kind

```yaml
kind: Namespace
```

Defines the type of Kubernetes resource.

Here, Kubernetes creates a **Namespace**.

---

## 3. metadata

```yaml
metadata:
```

Contains information about the Namespace such as its name and labels.

---

## 4. name

```yaml
name: e-track-dev
```

Assigns a unique name to the Namespace.

All Kubernetes resources created using:

```bash
kubectl apply -f deployment.yaml -n e-track-dev
```

or

```yaml
metadata:
  namespace: e-track-dev
```

will belong to this namespace.

Example:

```
Namespace: e-track-dev

├── Deployment
├── StatefulSet
├── Service
├── ConfigMap
├── Secret
├── PVC
└── Pods
```

---

## 5. labels

```yaml
labels:
  name: development
```

Labels are key-value pairs attached to Kubernetes resources.

Here:

- Key → `name`
- Value → `development`

Labels help organize, identify, and filter resources.

Example:

```bash
kubectl get namespaces --show-labels
```

Output:

```
NAME          STATUS   AGE   LABELS
default       Active   10d   kubernetes.io/metadata.name=default
e-track-dev   Active   2h    name=development
```

You can also filter resources using labels:

```bash
kubectl get namespaces -l name=development
```

---

# Why Use a Namespace?

Namespaces provide isolation between applications and environments.

Example:

```
Kubernetes Cluster
│
├── e-track-dev
│   ├── MySQL
│   ├── Spring Boot App
│   └── ConfigMaps
│
├── e-track-test
│   ├── MySQL
│   ├── Spring Boot App
│   └── ConfigMaps
│
└── e-track-prod
    ├── MySQL
    ├── Spring Boot App
    └── ConfigMaps
```

Each environment is independent, even if resource names are the same.

For example:

```
e-track-dev
└── Service: mysql

e-track-prod
└── Service: mysql
```

These services do not conflict because they exist in different namespaces.

---

# Benefits of Using Namespaces

- Organize resources by project or environment.
- Isolate development, testing, and production workloads.
- Apply resource quotas and limits per environment.
- Simplify access control using Kubernetes RBAC.
- Avoid naming conflicts between resources.

---

# Common Namespace Commands

## Create a Namespace

```bash
kubectl apply -f namespace.yaml
```

or

```bash
kubectl create namespace e-track-dev
```

---

## List Namespaces

```bash
kubectl get namespaces
```

---

## View Resources in a Namespace

```bash
kubectl get all -n e-track-dev
```

---

## Delete a Namespace

```bash
kubectl delete namespace e-track-dev
```

> **Note:** Deleting a namespace deletes all resources within it, including Pods, Services, Deployments, ConfigMaps, Secrets, and PVCs (unless the underlying storage uses a `Retain` reclaim policy).

---

# How Your Application Uses This Namespace

Your Expense Tracker resources are deployed into the `e-track-dev` namespace:

```
e-track-dev
│
├── Deployment
│      └── e-track-app-dep
│
├── StatefulSet
│      └── mysql-sfs
│
├── Services
│      ├── e-track-app-svc
│      └── mysql-sfs
│
├── ConfigMap
│      └── app-vars
│
├── Secret
│      └── app-secrets
│
├── PersistentVolumeClaim
│      └── mysql-pvc
│
└── Pods
       ├── e-track-app-dep-xxxxx
       ├── e-track-app-dep-yyyyy
       └── mysql-sfs-0
```

All these resources are isolated from other namespaces in the cluster.

---

# Summary

| Field | Purpose |
|--------|---------|
| `apiVersion: v1` | Uses the Kubernetes core API |
| `kind: Namespace` | Creates a Namespace resource |
| `metadata.name` | Specifies the namespace name (`e-track-dev`) |
| `labels` | Adds metadata for organization and filtering |
| **Purpose** | Groups and isolates Kubernetes resources for a specific environment (Development) |
```