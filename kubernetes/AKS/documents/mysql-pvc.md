# Kubernetes PersistentVolumeClaim (PVC) Explained (Azure AKS)

## PersistentVolumeClaim Manifest

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: e-track-dev
  labels:
    app: mysql-pvc

spec:
  storageClassName: mysql-premium-sc

  accessModes:
    - ReadWriteOnce

  resources:
    requests:
      storage: 2Gi
```

---

# What is a PersistentVolumeClaim (PVC)?

A **PersistentVolumeClaim (PVC)** is a Kubernetes resource that requests persistent storage for an application.

Instead of directly creating an Azure Managed Disk, the application requests storage through a PVC. Kubernetes then uses the specified **StorageClass** to dynamically provision the required storage.

In your project, the PVC provides persistent storage for the **MySQL StatefulSet**, ensuring that database data is retained even if the MySQL Pod is restarted or recreated.

---

# Why Use a PVC?

Containers have **ephemeral storage**, which means any data stored inside the container is lost if the container is deleted or restarted.

A PersistentVolumeClaim solves this problem by attaching persistent storage to the Pod.

Without PVC:

```
MySQL Pod
     │
     ▼
Container Storage
     │
Pod Deleted
     │
     ▼
Data Lost
```

With PVC:

```
MySQL Pod
     │
     ▼
PersistentVolumeClaim
     │
     ▼
Azure Managed Disk
     │
Pod Deleted
     │
     ▼
Data Preserved
```

---

# Manifest Breakdown

## apiVersion

```yaml
apiVersion: v1
```

Specifies the Kubernetes Core API version.

The **v1** API group contains resources such as:

- Pod
- Service
- Namespace
- Secret
- ConfigMap
- PersistentVolumeClaim

---

## kind

```yaml
kind: PersistentVolumeClaim
```

Creates a PersistentVolumeClaim resource.

A PVC requests storage from Kubernetes without requiring knowledge of the underlying storage provider.

---

## metadata

```yaml
metadata:
```

Contains identifying information about the PVC.

---

### name

```yaml
name: mysql-pvc
```

Defines the name of the PVC.

Your MySQL StatefulSet references this PVC:

```yaml
volumes:
  - name: mysql-storage
    persistentVolumeClaim:
      claimName: mysql-pvc
```

This attaches the persistent disk to the MySQL Pod.

---

### namespace

```yaml
namespace: e-track-dev
```

Creates the PVC in the **e-track-dev** namespace.

Only Pods in this namespace can directly reference this PVC.

---

### labels

```yaml
labels:
  app: mysql-pvc
```

Adds metadata to the PVC.

Labels help:

- Organize resources
- Filter resources
- Support monitoring and automation

Example:

```bash
kubectl get pvc --show-labels
```

Output:

```
NAME        STATUS   LABELS
mysql-pvc   Bound    app=mysql-pvc
```

---

# spec Section

The `spec` defines the storage requirements.

---

## storageClassName

```yaml
storageClassName: mysql-premium-sc
```

Specifies the StorageClass Kubernetes should use to provision storage.

The referenced StorageClass:

```yaml
mysql-premium-sc
```

uses:

- Azure Disk CSI Driver
- Premium SSD Managed Disk
- Dynamic provisioning

Provisioning flow:

```
PersistentVolumeClaim
        │
        ▼
StorageClass
(mysql-premium-sc)
        │
        ▼
Azure Disk CSI Driver
        │
        ▼
Azure Managed Disk
        │
        ▼
Persistent Volume
```

---

## accessModes

```yaml
accessModes:
  - ReadWriteOnce
```

Specifies how the storage can be mounted.

### ReadWriteOnce (RWO)

Only **one node** can mount the volume with read-write access at a time.

Example:

```
AKS Cluster

Node A
│
└── MySQL Pod
     │
     ▼
Azure Managed Disk
```

If MySQL moves to another node, Kubernetes detaches the disk from the old node and attaches it to the new node.

This is the recommended access mode for Azure Managed Disks and databases such as MySQL.

---

### Other Access Modes

| Access Mode | Description |
|--------------|-------------|
| ReadWriteOnce (RWO) | Mounted as read-write by a single node |
| ReadOnlyMany (ROX) | Mounted as read-only by multiple nodes |
| ReadWriteMany (RWX) | Mounted as read-write by multiple nodes (requires Azure Files or another shared storage solution) |

---

## resources

```yaml
resources:
```

Defines the amount of storage requested.

---

### requests

```yaml
requests:
  storage: 2Gi
```

Requests a **2 GiB** Azure Managed Disk.

When the PVC is created, Kubernetes dynamically provisions a disk with at least 2 GiB of capacity.

Example:

```
Requested Storage

2 GiB
```

If the StorageClass allows expansion (`allowVolumeExpansion: true`), the storage can later be increased.

Example:

```yaml
resources:
  requests:
    storage: 5Gi
```

Kubernetes expands the Azure Managed Disk without recreating it.

---

# Dynamic Provisioning Workflow

When the PVC is created:

```
kubectl apply -f pvc.yaml
```

Kubernetes performs the following steps:

1. Reads the StorageClass (`mysql-premium-sc`).
2. Calls the Azure Disk CSI Driver.
3. Creates a Premium SSD Managed Disk.
4. Creates a PersistentVolume (PV).
5. Binds the PV to the PVC.
6. Mounts the disk into the MySQL Pod.

Workflow:

```
PersistentVolumeClaim
        │
        ▼
StorageClass
        │
        ▼
Azure Disk CSI Driver
        │
        ▼
Azure Managed Disk
        │
        ▼
Persistent Volume
        │
        ▼
MySQL StatefulSet
```

---

# Relationship Between PV, PVC, and Pod

```
                MySQL StatefulSet
                        │
                        ▼
                  MySQL Pod
                        │
                        ▼
             PersistentVolumeClaim
                  (mysql-pvc)
                        │
                        ▼
             PersistentVolume (PV)
                        │
                        ▼
          Azure Managed Disk (Premium SSD)
```

---

# Why Use a PVC for MySQL?

Databases require persistent storage because data must survive:

- Pod restarts
- Pod rescheduling
- Node failures
- Cluster maintenance

Using a PVC ensures that:

- Database files are stored on an Azure Managed Disk.
- Data remains available even if the MySQL Pod is recreated.
- Kubernetes automatically reattaches the disk when the Pod starts again.

---

# Useful Commands

## Create the PVC

```bash
kubectl apply -f pvc.yaml
```

---

## View PVCs

```bash
kubectl get pvc -n e-track-dev
```

Example output:

```
NAME        STATUS   VOLUME                                     CAPACITY
mysql-pvc   Bound    pvc-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx   2Gi
```

---

## Describe the PVC

```bash
kubectl describe pvc mysql-pvc -n e-track-dev
```

---

## View Persistent Volumes

```bash
kubectl get pv
```

---

## Check StorageClass

```bash
kubectl get storageclass
```

---

# Architecture Diagram

```text
                 MySQL StatefulSet
                        │
                        ▼
                  MySQL Container
                        │
          Mounts /var/lib/mysql
                        │
                        ▼
          +-------------------------+
          | PersistentVolumeClaim   |
          |      mysql-pvc          |
          +------------+------------+
                       |
             Uses StorageClass
                       |
                       ▼
          +-------------------------+
          | StorageClass            |
          | mysql-premium-sc        |
          +------------+------------+
                       |
          Azure Disk CSI Driver
        (disk.csi.azure.com)
                       |
                       ▼
          +-------------------------+
          | Azure Managed Disk      |
          | Premium SSD (2 GiB)     |
          +-------------------------+
```

---

# Summary

| Field | Description |
|--------|-------------|
| `apiVersion: v1` | Uses the Kubernetes Core API |
| `kind: PersistentVolumeClaim` | Creates a PVC resource |
| `metadata.name` | Defines the PVC name (`mysql-pvc`) |
| `namespace` | Creates the PVC in the `e-track-dev` namespace |
| `labels` | Adds metadata for organization and filtering |
| `storageClassName` | References the `mysql-premium-sc` StorageClass |
| `accessModes: ReadWriteOnce` | Allows the volume to be mounted by one node with read-write access |
| `storage: 2Gi` | Requests a 2 GiB Azure Managed Disk |
| **Purpose** | Provides persistent storage for the MySQL StatefulSet by dynamically provisioning an Azure Premium Managed Disk through the Azure Disk CSI Driver. |