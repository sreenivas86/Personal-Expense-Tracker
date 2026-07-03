# Kubernetes Namespace Explained (Azure AKS)

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

Instead of placing all resources into the default namespace, you can create separate namespaces for different environments such as:

- Development
- Testing
- Staging
- Production

In your project, the namespace **e-track-dev** is used to isolate all Kubernetes resources related to the development environment.

---

# Why Use a Namespace?

Namespaces provide several benefits:

- Organize Kubernetes resources.
- Isolate development, testing, and production workloads.
- Prevent naming conflicts between environments.
- Apply resource quotas and limits.
- Control user access using Role-Based Access Control (RBAC).

Example:

```
AKS Cluster
│
├── default
│
├── kube-system
│
├── monitoring
│
└── e-track-dev
      │
      ├── Deployment
      ├── StatefulSet
      ├── Services
      ├── ConfigMap
      ├── Secret
      ├── PVC
      └── Pods
```

---

# Manifest Breakdown

## apiVersion

```yaml
apiVersion: v1
```

Specifies the Kubernetes Core API version.

The **v1** API group is used for core Kubernetes resources such as:

- Namespace
- Pod
- Service
- ConfigMap
- Secret
- PersistentVolumeClaim

---

## kind

```yaml
kind: Namespace
```

Creates a Kubernetes Namespace resource.

A Namespace acts like a virtual cluster inside the AKS cluster.

---

## metadata

```yaml
metadata:
```

Contains identifying information about the Namespace.

---

### name

```yaml
name: e-track-dev
```

Specifies the name of the Namespace.

All resources deployed with:

```yaml
namespace: e-track-dev
```

will belong to this Namespace.

Example:

```yaml
metadata:
  namespace: e-track-dev
```

Resources created in your project include:

- Deployment
- StatefulSet
- Services
- ConfigMap
- Secret
- PersistentVolumeClaim
- Pods

---

### labels

```yaml
labels:
  name: development
```

Labels are key-value pairs attached to Kubernetes objects.

In this example:

- **Key:** `name`
- **Value:** `development`

Labels help organize and categorize resources.

They can also be used for:

- Filtering resources
- Automation
- Monitoring
- Policy enforcement

Example:

```bash
kubectl get namespaces --show-labels
```

Output:

```
NAME          STATUS   AGE   LABELS
e-track-dev   Active   5m    name=development
```

---

# How Namespace Isolation Works

Without namespaces:

```
AKS Cluster
│
├── Deployment
├── StatefulSet
├── Service
├── Pod
└── Secret
```

All resources exist together, making management difficult.

With namespaces:

```
AKS Cluster
│
├── default
│
├── kube-system
│
├── e-track-dev
│     ├── Deployment
│     ├── Service
│     ├── StatefulSet
│     ├── Secret
│     └── Pods
│
└── e-track-prod
      ├── Deployment
      ├── Service
      ├── StatefulSet
      ├── Secret
      └── Pods
```

Each environment is isolated from the others.

---

# Benefits of Using Namespaces

## 1. Resource Isolation

Development resources remain separate from production resources.

Example:

```
Development Namespace

Deployment
Service
StatefulSet
Pods
```

cannot interfere with

```
Production Namespace

Deployment
Service
StatefulSet
Pods
```

---

## 2. Resource Organization

Namespaces make it easier to manage applications.

Example:

```
e-track-dev
│
├── Deployment
├── StatefulSet
├── Service
├── ConfigMap
├── Secret
└── PVC
```

---

## 3. RBAC (Role-Based Access Control)

Permissions can be granted for a specific namespace.

Example:

A developer may have access only to:

```
e-track-dev
```

but not:

```
production
```

---

## 4. Resource Quotas

Administrators can limit the amount of CPU, memory, and storage used within a namespace.

Example:

```
Development Namespace

CPU: 4 Cores

Memory: 8 Gi

Storage: 100 Gi
```

---

## 5. Prevent Naming Conflicts

Resources with the same name can exist in different namespaces.

Example:

```
Namespace: e-track-dev

Deployment:
expense-tracker
```

```
Namespace: e-track-prod

Deployment:
expense-tracker
```

Both deployments can coexist because they are in different namespaces.

---

# Working with Namespaces

## Create Namespace

```bash
kubectl apply -f namespace.yaml
```

---

## List Namespaces

```bash
kubectl get namespaces
```

Example output:

```
NAME              STATUS   AGE
default           Active   15d
kube-system       Active   15d
kube-public       Active   15d
e-track-dev       Active   5m
```

---

## View Resources in Namespace

```bash
kubectl get all -n e-track-dev
```

---

## Describe Namespace

```bash
kubectl describe namespace e-track-dev
```

---

## Delete Namespace

```bash
kubectl delete namespace e-track-dev
```

Deleting the namespace removes all resources inside it.

---

# How the Namespace Fits into Your Project

```
Azure Kubernetes Service (AKS)
│
└── Namespace
      e-track-dev
          │
          ├── Spring Boot Deployment
          ├── MySQL StatefulSet
          ├── LoadBalancer Service
          ├── Headless Service
          ├── ConfigMap
          ├── Secret
          ├── PersistentVolumeClaim
          └── Pods
```

Every Kubernetes resource in your Expense Tracker project is deployed inside the **e-track-dev** namespace.

---

# Architecture Diagram

```text
                  Azure Kubernetes Service
                           (AKS)
                              │
         ┌────────────────────┴────────────────────┐
         │                                         │
         ▼                                         ▼
   Namespace: default                   Namespace: e-track-dev
                                                │
                  ┌─────────────────────────────┼─────────────────────────────┐
                  │                             │                             │
                  ▼                             ▼                             ▼
          Spring Boot Deployment       MySQL StatefulSet         LoadBalancer Service
                  │                             │
                  ▼                             ▼
               Application Pods             MySQL Pod
                  │
                  ▼
       ConfigMap • Secret • PVC
```

---

# Summary

| Field | Description |
|--------|-------------|
| `apiVersion: v1` | Uses the Kubernetes Core API |
| `kind: Namespace` | Creates a Namespace resource |
| `metadata.name` | Defines the Namespace name (`e-track-dev`) |
| `labels` | Adds metadata for organization and filtering |
| **Purpose** | Logically isolates all Expense Tracker resources within the AKS cluster, improving organization, security, and resource management. |