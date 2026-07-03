# Kubernetes StorageClass Explained

## StorageClass Manifest

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

---

# What is a StorageClass?

A **StorageClass** in Kubernetes defines **how Persistent Volumes (PVs) are dynamically created**.

Instead of manually creating a Persistent Volume, Kubernetes automatically provisions one whenever a PersistentVolumeClaim (PVC) requests this StorageClass.

In GKE, the StorageClass tells Kubernetes what type of Google Persistent Disk to create.

---

# Line-by-Line Explanation

## 1. apiVersion

```yaml
apiVersion: storage.k8s.io/v1
```

Specifies the Kubernetes API version used for Storage resources.

- `storage.k8s.io` → Storage API group
- `v1` → Stable API version

---

## 2. kind

```yaml
kind: StorageClass
```

Defines the resource type.

Here, Kubernetes creates a **StorageClass**.

---

## 3. metadata

```yaml
metadata:
  name: mysql-premium-sc
```

Assigns a name to the StorageClass.

Applications refer to this name in their PersistentVolumeClaims.

Example:

```yaml
storageClassName: mysql-premium-sc
```

---

## 4. provisioner

```yaml
provisioner: pd.csi.storage.gke.io
```

Specifies **who creates the storage**.

In GKE:

- `pd.csi.storage.gke.io`
- Uses the Google Persistent Disk CSI Driver.

When a PVC requests storage, Kubernetes automatically creates a Google Cloud Persistent Disk.

Example:

```
PVC
 │
 ▼
StorageClass
 │
 ▼
Google Persistent Disk
 │
 ▼
Persistent Volume
```

---

## 5. reclaimPolicy

```yaml
reclaimPolicy: Retain
```

Determines what happens when the PVC is deleted.

### Retain

The disk is **not deleted**.

```
Delete PVC
      │
      ▼
Persistent Disk remains
```

Useful for databases because data is preserved even if the PVC is accidentally deleted.

Other options:

### Delete

```yaml
reclaimPolicy: Delete
```

Deletes both the PV and the Google Persistent Disk.

Recommended for temporary workloads.

---

## 6. volumeBindingMode

```yaml
volumeBindingMode: WaitForFirstConsumer
```

Controls **when Kubernetes creates the disk**.

### WaitForFirstConsumer

The Persistent Disk is created **only after a Pod using the PVC is scheduled**.

Benefits:

- Creates the disk in the same availability zone as the Pod.
- Avoids scheduling issues in multi-zone clusters.
- Recommended for StatefulSets and databases.

Workflow:

```
PVC Created
      │
      ▼
Waiting...
      │
Pod Scheduled
      │
      ▼
Persistent Disk Created
      │
      ▼
PV Bound
```

Alternative:

```yaml
Immediate
```

Creates the disk immediately after the PVC is created.

---

## 7. allowVolumeExpansion

```yaml
allowVolumeExpansion: true
```

Allows increasing the disk size later without creating a new volume.

Example:

Old PVC

```yaml
resources:
  requests:
    storage: 10Gi
```

Later:

```yaml
resources:
  requests:
    storage: 20Gi
```

Kubernetes expands the Google Persistent Disk.

This is very useful for growing databases.

---

## 8. parameters

```yaml
parameters:
  type: pd-balanced
```

Specifies the Google Cloud disk type.

### pd-balanced

Balanced Persistent Disk.

Provides a good balance between:

- Performance
- Cost
- Reliability

Suitable for:

- MySQL
- PostgreSQL
- Spring Boot applications
- Production workloads

Other options include:

| Disk Type | Description | Best For |
|------------|-------------|----------|
| `pd-standard` | Standard HDD | Low-cost workloads |
| `pd-balanced` | Balanced SSD/HDD | General-purpose databases |
| `pd-ssd` | High-performance SSD | High IOPS databases |
| `hyperdisk-balanced` | High-performance balanced storage | Large production systems |

---

# How It Works

```
StorageClass
      │
      ▼
PersistentVolumeClaim
      │
      ▼
Google Persistent Disk
      │
      ▼
Persistent Volume
      │
      ▼
Mounted inside Pod
```

---

# Example PVC

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
      storage: 10Gi
```

When this PVC is created, Kubernetes automatically:

1. Creates a Google Persistent Disk.
2. Creates a Persistent Volume.
3. Binds the PV to the PVC.
4. Mounts the disk into the MySQL Pod.

---

# Why This Configuration is Good for MySQL

- Uses dynamic provisioning (no manual PV creation).
- Preserves data with `Retain`.
- Waits until a Pod is scheduled before provisioning the disk.
- Supports future disk expansion.
- Uses `pd-balanced`, which is a good fit for database workloads.

---

# Summary

| Field | Purpose |
|--------|---------|
| `apiVersion` | Kubernetes Storage API version |
| `kind` | Creates a StorageClass |
| `metadata.name` | Name referenced by PVCs |
| `provisioner` | Uses the GKE Persistent Disk CSI driver |
| `reclaimPolicy: Retain` | Keeps the disk after PVC deletion |
| `volumeBindingMode: WaitForFirstConsumer` | Creates the disk only when a Pod is scheduled |
| `allowVolumeExpansion: true` | Allows increasing disk size later |
| `parameters.type: pd-balanced` | Uses Google Cloud Balanced Persistent Disk |