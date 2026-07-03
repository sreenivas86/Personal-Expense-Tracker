# Kubernetes StorageClass Explained (Azure AKS)

## StorageClass Manifest

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: mysql-premium-sc
  namespace: e-track-dev
provisioner: disk.csi.azure.com
reclaimPolicy: Retain
volumeBindingMode: WaitForFirstConsumer
allowVolumeExpansion: true
parameters:
  storageaccounttype: Premium_LRS
  kind: Managed
```

---

# What is a StorageClass?

A **StorageClass** in Kubernetes defines **how Persistent Volumes (PVs) are dynamically provisioned**.

Instead of manually creating Azure Managed Disks, Kubernetes automatically provisions one whenever a PersistentVolumeClaim (PVC) requests this StorageClass.

In an **Azure Kubernetes Service (AKS)** cluster, the StorageClass specifies:

- Which storage driver to use.
- The type of Azure Managed Disk to create.
- The disk performance tier.
- Whether the disk can be expanded.
- What happens to the disk after the PVC is deleted.

---

# Line-by-Line Explanation

## 1. apiVersion

```yaml
apiVersion: storage.k8s.io/v1
```

Specifies the Kubernetes API version for storage resources.

- `storage.k8s.io` → Storage API group.
- `v1` → Stable API version.

---

## 2. kind

```yaml
kind: StorageClass
```

Creates a Kubernetes **StorageClass** resource.

A StorageClass acts as a blueprint for creating Persistent Volumes dynamically.

---

## 3. metadata

```yaml
metadata:
  name: mysql-premium-sc
  namespace: e-track-dev
```

### name

```yaml
name: mysql-premium-sc
```

Defines the StorageClass name.

Applications reference this StorageClass in their PVC:

```yaml
storageClassName: mysql-premium-sc
```

---

### namespace

Although Kubernetes allows this field in the manifest, **StorageClass is a cluster-scoped resource**, meaning it is **not associated with any namespace**.

Therefore, the `namespace: e-track-dev` field is ignored.

A typical StorageClass manifest omits the namespace:

```yaml
metadata:
  name: mysql-premium-sc
```

---

## 4. provisioner

```yaml
provisioner: disk.csi.azure.com
```

Specifies which storage driver provisions the volume.

In AKS:

```
disk.csi.azure.com
```

is the **Azure Disk CSI Driver**.

When a PVC requests storage, Kubernetes performs the following steps:

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
```

This process is known as **Dynamic Provisioning**.

---

## 5. reclaimPolicy

```yaml
reclaimPolicy: Retain
```

Determines what happens when the PersistentVolumeClaim (PVC) is deleted.

### Retain

```
Delete PVC
      │
      ▼
Persistent Volume remains
      │
      ▼
Azure Managed Disk remains
```

The Azure Managed Disk is **not deleted**.

This is highly recommended for databases such as:

- MySQL
- PostgreSQL
- SQL Server

because it protects data from accidental deletion.

---

### Alternative

```yaml
reclaimPolicy: Delete
```

Deletes:

- Persistent Volume
- Azure Managed Disk

This option is more suitable for temporary or development environments.

---

## 6. volumeBindingMode

```yaml
volumeBindingMode: WaitForFirstConsumer
```

Controls **when the Azure Managed Disk is created**.

### WaitForFirstConsumer

The disk is created **only after a Pod using the PVC is scheduled**.

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
Azure Managed Disk Created
      │
      ▼
Persistent Volume Bound
```

### Benefits

- Ensures the disk is created in the correct Availability Zone.
- Prevents scheduling conflicts.
- Recommended for StatefulSets and databases.

---

### Alternative

```yaml
Immediate
```

Creates the Azure Managed Disk immediately after the PVC is created.

---

## 7. allowVolumeExpansion

```yaml
allowVolumeExpansion: true
```

Allows the disk size to be increased later without recreating the volume.

Example:

Initial PVC:

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

Kubernetes automatically expands the Azure Managed Disk.

This feature is especially useful for growing databases.

---

## 8. parameters

The `parameters` section contains Azure-specific storage configuration.

---

### storageaccounttype

```yaml
storageaccounttype: Premium_LRS
```

Specifies the performance tier of the Azure Managed Disk.

**Premium_LRS** stands for:

- **Premium** → SSD-backed storage
- **LRS** → Locally Redundant Storage

Characteristics:

- High IOPS
- Low latency
- SSD storage
- Data replicated three times within a single Azure region

Suitable for:

- MySQL
- PostgreSQL
- SQL Server
- Production databases
- High-performance applications

Other options include:

| Storage Type | Description | Best For |
|--------------|-------------|----------|
| Standard_LRS | Standard HDD | Development, testing |
| StandardSSD_LRS | Standard SSD | General-purpose workloads |
| Premium_LRS | Premium SSD | Production databases |
| PremiumV2_LRS | Premium SSD v2 | High-performance production workloads |
| UltraSSD_LRS | Ultra Disk | Mission-critical, high IOPS databases |

---

### kind

```yaml
kind: Managed
```

Specifies that Kubernetes should create an **Azure Managed Disk**.

Azure offers two disk management models:

### Managed Disk

```
AKS
 │
 ▼
Azure Managed Disk
```

Azure automatically manages:

- Storage accounts
- Replication
- Availability
- Scaling
- Maintenance

This is the recommended option for AKS.

---

# How It Works

```
PersistentVolumeClaim
        │
        ▼
StorageClass
(mysql-premium-sc)
        │
        ▼
Azure Disk CSI Driver
(disk.csi.azure.com)
        │
        ▼
Azure Managed Disk
(Premium SSD)
        │
        ▼
Persistent Volume
        │
        ▼
Mounted inside MySQL Pod
```

---

# Example PVC Using This StorageClass

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

1. Uses the Azure Disk CSI Driver.
2. Creates a Premium SSD Managed Disk.
3. Creates a Persistent Volume.
4. Binds the PV to the PVC.
5. Mounts the disk into the MySQL Pod.

---

# Why This Configuration is Good for MySQL

This StorageClass is ideal for running MySQL on AKS because it:

- Dynamically provisions Azure Managed Disks.
- Uses high-performance Premium SSD storage.
- Preserves database data using `Retain`.
- Waits until a Pod is scheduled before provisioning storage.
- Supports online disk expansion.
- Uses the Azure Disk CSI driver, which is the recommended storage driver for AKS.

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
                Azure Disk CSI Driver
                         |
                         ▼
              +----------------------+
              | Azure Managed Disk   |
              | Premium SSD (LRS)    |
              +----------------------+
```

---

# Summary

| Field | Purpose |
|--------|---------|
| `apiVersion` | Kubernetes Storage API version |
| `kind` | Creates a StorageClass |
| `metadata.name` | Name referenced by PVCs |
| `metadata.namespace` | Ignored because StorageClass is cluster-scoped |
| `provisioner` | Uses the Azure Disk CSI Driver |
| `reclaimPolicy: Retain` | Keeps the Azure Managed Disk after PVC deletion |
| `volumeBindingMode: WaitForFirstConsumer` | Creates the disk only after a Pod is scheduled |
| `allowVolumeExpansion: true` | Allows increasing disk size later |
| `storageaccounttype: Premium_LRS` | Uses Premium SSD with Local Redundant Storage |
| `kind: Managed` | Creates an Azure Managed Disk |
| **Purpose** | Dynamically provisions Premium Azure Managed Disks for persistent database storage in AKS |