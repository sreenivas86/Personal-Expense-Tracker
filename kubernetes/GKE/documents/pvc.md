# Kubernetes PersistentVolumeClaim (PVC) Explained

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
      storage: 4Gi
```

---

# What is a PersistentVolumeClaim (PVC)?

A **PersistentVolumeClaim (PVC)** is a request for persistent storage in Kubernetes.

Instead of creating storage manually, an application requests storage by creating a PVC. Kubernetes then provisions or binds a Persistent Volume (PV) based on the requested storage requirements.

Think of it like this:

- **Application** → Needs storage
- **PVC** → Requests storage
- **StorageClass** → Defines how storage should be created
- **Persistent Volume (PV)** → Actual storage provisioned for the application

---

# Line-by-Line Explanation

## 1. apiVersion

```yaml
apiVersion: v1
```

Specifies the Kubernetes API version used for PersistentVolumeClaim resources.

- `v1` is the stable core API version.

---

## 2. kind

```yaml
kind: PersistentVolumeClaim
```

Defines the Kubernetes resource type.

Here, Kubernetes creates a **PersistentVolumeClaim (PVC)**.

---

## 3. metadata

```yaml
metadata:
```

Contains metadata about the PVC such as its name, namespace, and labels.

---

## 4. name

```yaml
name: mysql-pvc
```

Assigns a unique name to the PVC.

The MySQL StatefulSet refers to this PVC:

```yaml
volumes:
  - name: mysql-storage
    persistentVolumeClaim:
      claimName: mysql-pvc
```

This tells Kubernetes to mount the storage requested by `mysql-pvc` into the MySQL container.

---

## 5. namespace

```yaml
namespace: e-track-dev
```

Creates the PVC inside the **e-track-dev** namespace.

Only Pods in the same namespace can reference this PVC.

---

## 6. labels

```yaml
labels:
  app: mysql-pvc
```

Labels are key-value pairs used to organize Kubernetes resources.

Here:

- Key → `app`
- Value → `mysql-pvc`

You can filter PVCs using labels:

```bash
kubectl get pvc -l app=mysql-pvc -n e-track-dev
```

---

# spec Section

The `spec` section defines the storage requirements.

---

## 7. storageClassName

```yaml
storageClassName: mysql-premium-sc
```

Specifies which **StorageClass** Kubernetes should use to provision the storage.

Earlier, you created:

```yaml
kind: StorageClass
metadata:
  name: mysql-premium-sc
```

When this PVC is created, Kubernetes uses that StorageClass to dynamically create a Google Persistent Disk.

Workflow:

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

## 8. accessModes

```yaml
accessModes:
  - ReadWriteOnce
```

Defines how the storage can be mounted.

### ReadWriteOnce (RWO)

- The volume can be mounted as **read-write by a single node**.
- Multiple Pods can use the volume only if they are running on the **same node**.

This is the recommended mode for MySQL because only one database instance should write to the data directory.

Other access modes include:

| Access Mode | Description |
|--------------|-------------|
| ReadWriteOnce (RWO) | Mounted read-write by one node |
| ReadOnlyMany (ROX) | Mounted read-only by many nodes |
| ReadWriteMany (RWX) | Mounted read-write by many nodes (depends on storage backend) |
| ReadWriteOncePod (RWOP) | Mounted by only one Pod in the cluster |

---

## 9. resources

```yaml
resources:
```

Specifies the amount of storage requested.

---

## 10. requests

```yaml
requests:
  storage: 4Gi
```

Requests **4 GiB** of persistent storage.

Kubernetes creates a Persistent Volume of at least 4 GiB.

Example:

```
Requested:
4 GiB

Provisioned:
Google Persistent Disk
Size: 4 GiB
```

If your StorageClass has:

```yaml
allowVolumeExpansion: true
```

you can later increase the storage to:

```yaml
resources:
  requests:
    storage: 10Gi
```

without recreating the volume.

---

# How This PVC Works with Your MySQL StatefulSet

Your StatefulSet contains:

```yaml
volumes:
  - name: mysql-storage
    persistentVolumeClaim:
      claimName: mysql-pvc
```

and

```yaml
volumeMounts:
  - name: mysql-storage
    mountPath: /var/lib/mysql
```

Workflow:

```
MySQL Pod
     │
     ▼
Volume Mount
/var/lib/mysql
     │
     ▼
PersistentVolumeClaim
(mysql-pvc)
     │
     ▼
StorageClass
(mysql-premium-sc)
     │
     ▼
Persistent Volume
     │
     ▼
Google Persistent Disk
```

All MySQL database files are stored on the persistent disk instead of inside the container.

---

# Why Use a PVC for MySQL?

Without a PVC:

```
Container Deleted
      │
      ▼
Database Files Lost
```

With a PVC:

```
Container Deleted
      │
      ▼
Persistent Volume Remains
      │
      ▼
New MySQL Pod
      │
      ▼
Existing Data Mounted
```

This ensures that your database survives Pod restarts and rescheduling.

---

# Useful Commands

## View PVC

```bash
kubectl get pvc -n e-track-dev
```

---

## Describe PVC

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

## Verify the Mounted Volume

```bash
kubectl exec -it mysql-sfs-0 -n e-track-dev -- df -h
```

This shows the mounted persistent disk inside the MySQL container.

---

# Architecture

```
              +----------------------+
              |   MySQL StatefulSet  |
              +----------+-----------+
                         |
                         |
                Mounts Volume
                         |
                         ▼
              +----------------------+
              | PersistentVolumeClaim|
              |      mysql-pvc       |
              +----------+-----------+
                         |
                         |
              Uses StorageClass
                         |
                         ▼
              +----------------------+
              | mysql-premium-sc     |
              +----------+-----------+
                         |
                Dynamically Creates
                         |
                         ▼
              +----------------------+
              | Persistent Volume    |
              +----------+-----------+
                         |
                         ▼
              Google Persistent Disk
```

---

# Summary

| Field | Purpose |
|--------|---------|
| `apiVersion: v1` | Uses the Kubernetes core API |
| `kind: PersistentVolumeClaim` | Creates a PVC |
| `metadata.name` | Name of the PVC (`mysql-pvc`) |
| `namespace` | Creates the PVC in `e-track-dev` |
| `labels` | Metadata for organizing resources |
| `storageClassName` | Uses the `mysql-premium-sc` StorageClass |
| `accessModes: ReadWriteOnce` | Allows one node to mount the volume for read/write |
| `storage: 4Gi` | Requests 4 GiB of persistent storage |
| **Purpose** | Provides persistent storage for the MySQL database, ensuring data survives Pod restarts and rescheduling |