# Kubernetes Service Explained (Azure AKS)

## Service Manifest

```yaml
apiVersion: v1
kind: Service
metadata:
  name: e-track-app-svc
  namespace: e-track-dev

spec:
  selector:
    app: e-track-app

  type: LoadBalancer

  sessionAffinity: None

  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 10800

  ports:
  - name: e-track-app-svc
    port: 8080
    targetPort: 8080
```

---

# What is a Kubernetes Service?

A **Service** is a Kubernetes resource that provides a stable network endpoint for accessing one or more Pods.

Pods are **ephemeral**, meaning they can be:

- Deleted
- Recreated
- Assigned new IP addresses

Instead of connecting directly to Pods, clients connect to a Service, which automatically forwards traffic to healthy Pods.

In this project, the Service exposes the **Spring Boot Expense Tracker application** running in the Deployment.

---

# Why Use a Service?

Without a Service:

```
Internet
     │
     ▼
Pod IP

10.244.0.10
```

If the Pod restarts:

```
Pod IP changes

10.244.0.15
```

The application becomes unreachable.

With a Service:

```
Internet
     │
     ▼
Service
     │
     ▼
Pods
```

The Service IP remains constant even if Pods are recreated.

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
- ConfigMap
- Secret
- Namespace
- PersistentVolumeClaim

---

## kind

```yaml
kind: Service
```

Creates a Kubernetes Service resource.

A Service provides a stable endpoint for accessing Pods.

---

## metadata

### Name

```yaml
name: e-track-app-svc
```

Defines the Service name.

Applications inside the cluster can reach it using:

```
e-track-app-svc
```

or

```
e-track-app-svc.e-track-dev.svc.cluster.local
```

---

### Namespace

```yaml
namespace: e-track-dev
```

Creates the Service inside the **e-track-dev** namespace.

---

# spec Section

Defines how traffic is routed to Pods.

---

## selector

```yaml
selector:
  app: e-track-app
```

The selector identifies which Pods receive traffic.

Every Pod with the label:

```yaml
labels:
  app: e-track-app
```

becomes an endpoint for this Service.

Example:

```
Deployment

Pod 1
Label:
app=e-track-app

Pod 2
Label:
app=e-track-app
```

The Service automatically forwards requests to these Pods.

---

## type

```yaml
type: LoadBalancer
```

Creates an **Azure Load Balancer**.

This makes the application accessible from outside the AKS cluster.

Workflow:

```
Internet
     │
     ▼
Azure Load Balancer
     │
     ▼
Kubernetes Service
     │
     ▼
Application Pods
```

Azure automatically provisions:

- Public IP Address
- Azure Load Balancer
- Health Probes
- Backend Pool

---

# Service Types

| Type | Description |
|--------|-------------|
| ClusterIP | Internal access only (default) |
| NodePort | Exposes the application on every AKS node |
| LoadBalancer | Creates an Azure Load Balancer with a public IP |
| ExternalName | Maps the Service to an external DNS name |

Your application uses **LoadBalancer** because it must be accessible from the internet.

---

## sessionAffinity

```yaml
sessionAffinity: None
```

Controls whether client requests are consistently routed to the same Pod.

### None

```
Client Request 1
      │
      ▼
Pod 1

Client Request 2
      │
      ▼
Pod 2
```

Each request can be routed to any healthy Pod.

This provides better load balancing and resource utilization.

---

### ClientIP

If configured as:

```yaml
sessionAffinity: ClientIP
```

Then:

```
Client A
     │
     ▼
Always Pod 1

Client B
     │
     ▼
Always Pod 2
```

This is useful for applications requiring session persistence.

---

## sessionAffinityConfig

```yaml
sessionAffinityConfig:
  clientIP:
    timeoutSeconds: 10800
```

Specifies the duration (in seconds) that a client remains associated with the same Pod when `sessionAffinity: ClientIP` is enabled.

```
10800 seconds = 3 hours
```

### Note

Since your manifest sets:

```yaml
sessionAffinity: None
```

this configuration is **ignored**.

You only need `sessionAffinityConfig` when using:

```yaml
sessionAffinity: ClientIP
```

---

# Ports

```yaml
ports:
```

Defines how traffic is forwarded.

---

## Name

```yaml
name: e-track-app-svc
```

Provides a name for the Service port.

Useful when exposing multiple ports.

---

## Port

```yaml
port: 8080
```

The Service listens on port **8080**.

Applications inside or outside the cluster connect to:

```
http://<LoadBalancer-IP>:8080
```

---

## targetPort

```yaml
targetPort: 8080
```

The Service forwards incoming traffic to container port **8080**.

The Spring Boot application listens on:

```
8080
```

inside each Pod.

Traffic flow:

```
LoadBalancer

Port 8080
      │
      ▼
Service

Port 8080
      │
      ▼
Spring Boot Pod

Container Port 8080
```

---

# Request Flow

```
Browser
    │
    ▼
Azure Public IP
    │
    ▼
Azure Load Balancer
    │
    ▼
Kubernetes Service
    │
    ▼
Application Pods
```

---

# Relationship with Deployment

```
                  Deployment
               e-track-app-dep
                      │
          Creates Two Pods
        ┌─────────────┴─────────────┐
        ▼                           ▼
Spring Boot Pod              Spring Boot Pod
Label: app=e-track-app  Label: app=e-track-app
        │                           │
        └─────────────┬─────────────┘
                      │
               Kubernetes Service
                e-track-app-svc
                      │
             Azure Load Balancer
                      │
                 Public Internet
```

The Service automatically discovers Pods based on the label selector.

---

# Azure AKS Networking

When the Service is created:

```bash
kubectl apply -f service.yaml
```

Azure automatically creates:

```
Azure Public IP
        │
        ▼
Azure Load Balancer
        │
        ▼
Kubernetes Service
        │
        ▼
Application Pods
```

You can view the external IP using:

```bash
kubectl get svc -n e-track-dev
```

Example:

```
NAME              TYPE           EXTERNAL-IP
e-track-app-svc   LoadBalancer   20.50.xxx.xxx
```

Access the application:

```
http://20.50.xxx.xxx:8080
```

---

# Useful Commands

## Create the Service

```bash
kubectl apply -f service.yaml
```

---

## View Services

```bash
kubectl get svc -n e-track-dev
```

---

## Describe the Service

```bash
kubectl describe svc e-track-app-svc -n e-track-dev
```

---

## View Endpoints

```bash
kubectl get endpoints e-track-app-svc -n e-track-dev
```

Example:

```
NAME              ENDPOINTS
e-track-app-svc   10.244.0.5:8080,10.244.0.6:8080
```

---

## Test from Inside the Cluster

```bash
kubectl exec -it <pod-name> -n e-track-dev -- curl http://e-track-app-svc:8080
```

---

# Architecture Diagram

```text
                     Internet
                         │
                         ▼
               Azure Public IP
                         │
                         ▼
             Azure Load Balancer
                         │
                         ▼
              Kubernetes Service
               e-track-app-svc
                         │
          ┌──────────────┴──────────────┐
          ▼                             ▼
   Spring Boot Pod               Spring Boot Pod
   Port: 8080                    Port: 8080
          │                             │
          └──────────────┬──────────────┘
                         │
                         ▼
                  MySQL StatefulSet
                         │
                         ▼
                Azure Managed Disk
```

---

# Best Practices

- Use **LoadBalancer** only for applications that need external access.
- Ensure the Service selector matches the Pod labels exactly.
- Expose only the required ports.
- Use **ClusterIP** for internal-only services, such as MySQL.
- Use **sessionAffinity: None** unless your application requires sticky sessions.
- Protect internet-facing applications with an **Ingress Controller** and TLS in production.

---

# Summary

| Field | Description |
|--------|-------------|
| `apiVersion: v1` | Uses the Kubernetes Core API |
| `kind: Service` | Creates a Kubernetes Service |
| `metadata.name` | Service name (`e-track-app-svc`) |
| `namespace` | Deploys the Service in the `e-track-dev` namespace |
| `selector` | Routes traffic to Pods labeled `app=e-track-app` |
| `type: LoadBalancer` | Creates an Azure Load Balancer with a public IP |
| `sessionAffinity: None` | Distributes requests across all healthy Pods |
| `sessionAffinityConfig` | Sticky session timeout (used only with `ClientIP`) |
| `port: 8080` | Port exposed by the Service |
| `targetPort: 8080` | Port on the Spring Boot container that receives traffic |
| **Purpose** | Exposes the Spring Boot application to users through an Azure Load Balancer, providing a stable endpoint and distributing traffic across multiple application Pods running in the AKS cluster. |