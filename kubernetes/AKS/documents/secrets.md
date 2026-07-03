# Kubernetes Secret Explained (Azure AKS)

## Secret Manifest

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: e-track-dev

type: Opaque

stringData:
  sql-password: sreenivas
```

---

# What is a Secret?

A **Secret** is a Kubernetes resource used to securely store **sensitive information** such as:

- Database passwords
- API Keys
- Authentication tokens
- TLS Certificates
- SSH Keys

Instead of storing confidential information inside the application source code, Docker image, or ConfigMap, Kubernetes stores it separately in a Secret and injects it into Pods at runtime.

In your Expense Tracker project, the Secret stores the **MySQL root password**, which is used by both the MySQL StatefulSet and the Spring Boot application.

---

# Why Use a Secret?

Without a Secret:

```properties
spring.datasource.password=sreenivas
```

The password is hardcoded in the application configuration, making it insecure and difficult to change.

With a Secret:

```
Kubernetes Secret
        │
        ▼
Spring Boot Pod
        │
        ▼
Reads password as an environment variable
```

This approach improves security and makes password management easier.

---

# Manifest Breakdown

## apiVersion

```yaml
apiVersion: v1
```

Specifies the Kubernetes Core API version.

The **v1** API includes resources such as:

- Pod
- Service
- Namespace
- ConfigMap
- Secret
- PersistentVolumeClaim

---

## kind

```yaml
kind: Secret
```

Creates a Kubernetes Secret resource.

Secrets are specifically designed to store confidential information.

---

## metadata

```yaml
metadata:
```

Contains metadata about the Secret.

---

### name

```yaml
name: app-secrets
```

Defines the Secret name.

Your application and MySQL StatefulSet reference this Secret using:

```yaml
secretKeyRef:
  name: app-secrets
  key: sql-password
```

---

### namespace

```yaml
namespace: e-track-dev
```

Creates the Secret in the **e-track-dev** namespace.

Only workloads within this namespace can reference it directly.

---

## type

```yaml
type: Opaque
```

Specifies the Secret type.

**Opaque** is the default and most commonly used Secret type for arbitrary key-value pairs.

It is suitable for storing:

- Database passwords
- API keys
- User credentials
- Connection strings

Other Secret types include:

| Type | Purpose |
|------|---------|
| `Opaque` | Generic key-value secrets |
| `kubernetes.io/tls` | TLS certificates and private keys |
| `kubernetes.io/dockerconfigjson` | Docker registry credentials |
| `kubernetes.io/basic-auth` | Username and password |
| `kubernetes.io/ssh-auth` | SSH private keys |

---

# stringData

```yaml
stringData:
  sql-password: sreenivas
```

The `stringData` field allows you to specify secret values as plain text.

Kubernetes automatically converts these values to Base64 and stores them in the `data` field internally.

Key:

```yaml
sql-password
```

Value:

```yaml
sreenivas
```

---

# Why Use `stringData` Instead of `data`?

There are two ways to create Secrets.

## Option 1: stringData (Recommended)

```yaml
stringData:
  sql-password: sreenivas
```

Advantages:

- Easy to read and maintain.
- No manual Base64 encoding required.
- Kubernetes automatically encodes the values.

---

## Option 2: data

```yaml
data:
  sql-password: c3JlZW5pdmFz
```

Here, the password is Base64 encoded.

You must encode the value manually:

```bash
echo -n "sreenivas" | base64
```

Output:

```
c3JlZW5pdmFz
```

Using `stringData` is generally preferred because it is more convenient.

---

# How the Secret is Used

## MySQL StatefulSet

The MySQL container reads the password from the Secret:

```yaml
env:
  - name: MYSQL_ROOT_PASSWORD
    valueFrom:
      secretKeyRef:
        name: app-secrets
        key: sql-password
```

This sets the environment variable:

```
MYSQL_ROOT_PASSWORD=sreenivas
```

The MySQL server uses this value as the root password during initialization.

---

## Spring Boot Deployment

The Spring Boot application also reads the password from the same Secret:

```yaml
env:
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: app-secrets
        key: sql-password
```

This creates the environment variable:

```
DB_PASSWORD=sreenivas
```

The application uses this password to establish a connection with the MySQL database.

---

# Secret Usage Flow

```
            Kubernetes Secret
             app-secrets
                  │
        +---------+---------+
        │                   │
        ▼                   ▼
 MySQL StatefulSet    Spring Boot Deployment
        │                   │
        ▼                   ▼
MYSQL_ROOT_PASSWORD    DB_PASSWORD
        │                   │
        └---------+---------┘
                  │
                  ▼
          MySQL Database Connection
```

---

# Secret vs ConfigMap

| ConfigMap | Secret |
|------------|--------|
| Stores non-sensitive data | Stores sensitive data |
| Application name | Database password |
| Database host | API keys |
| Database port | Authentication tokens |
| Database name | Certificates |
| Username | Private credentials |

For your project:

### ConfigMap

- Application Name
- Database Name
- Database Host
- Database Port
- Database Username
- Application Port

### Secret

- Database Password

---

# Useful Commands

## Create the Secret

```bash
kubectl apply -f secret.yaml
```

---

## View Secrets

```bash
kubectl get secrets -n e-track-dev
```

Example:

```
NAME            TYPE      DATA
app-secrets     Opaque    1
```

---

## Describe the Secret

```bash
kubectl describe secret app-secrets -n e-track-dev
```

Note: The actual secret values are not displayed.

---

## View the Secret as YAML

```bash
kubectl get secret app-secrets -n e-track-dev -o yaml
```

Example:

```yaml
data:
  sql-password: c3JlZW5pdmFz
```

The value is Base64 encoded.

---

## Decode the Secret

```bash
kubectl get secret app-secrets \
-n e-track-dev \
-o jsonpath="{.data.sql-password}" | base64 --decode
```

Output:

```
sreenivas
```

---

## Verify Environment Variables Inside a Pod

```bash
kubectl exec -it <pod-name> -n e-track-dev -- env
```

Example:

```
DB_PASSWORD=sreenivas
```

---

# Security Best Practices

- Do not hardcode passwords in application code.
- Use `stringData` when creating Secrets for easier management.
- Restrict access to Secrets using Kubernetes RBAC.
- Consider using **Azure Key Vault** with the **Secrets Store CSI Driver** in production to avoid storing sensitive data directly in Kubernetes.
- Rotate passwords and secrets regularly.

---

# Architecture Diagram

```text
                 Kubernetes Secret
                  app-secrets
                       │
         +-------------+--------------+
         │                            │
         ▼                            ▼
  MySQL StatefulSet         Spring Boot Deployment
         │                            │
         ▼                            ▼
MYSQL_ROOT_PASSWORD            DB_PASSWORD
         │                            │
         └-------------+--------------┘
                       │
                       ▼
                 MySQL Database
```

---

# Summary

| Field | Description |
|--------|-------------|
| `apiVersion: v1` | Uses the Kubernetes Core API |
| `kind: Secret` | Creates a Secret resource |
| `metadata.name` | Secret name (`app-secrets`) |
| `namespace` | Creates the Secret in the `e-track-dev` namespace |
| `type: Opaque` | Stores generic key-value secret data |
| `stringData` | Allows secrets to be defined as plain text; Kubernetes encodes them automatically |
| `sql-password` | Stores the MySQL root password |
| **Purpose** | Securely stores sensitive information, such as the MySQL password, and injects it into the MySQL StatefulSet and Spring Boot Deployment as environment variables. |