# Wallet Service - Kubernetes Deployment Guide

## 📋 Prerequisites

- Kubernetes cluster 1.25+
- kubectl configured
- Helm 3.0+ (optional, for dependencies)
- NGINX Ingress Controller
- Prometheus Operator (optional, for monitoring)
- Metrics Server (for HPA)

## 🚀 Quick Start

### 1. Create Namespace and Apply Manifests

```bash
# Navigate to k8s directory
cd k8s

# Apply all manifests
kubectl apply -k .

# Or apply individually
kubectl apply -f 00-namespace.yaml
kubectl apply -f 01-configmap.yaml
kubectl apply -f 02-secrets.yaml
kubectl apply -f 10-deployment.yaml
kubectl apply -f 11-service.yaml
kubectl apply -f 12-ingress.yaml
```

### 2. Verify Deployment

```bash
# Check namespace
kubectl get namespace wallet-service

# Check pods
kubectl get pods -n wallet-service

# Check services
kubectl get svc -n wallet-service

# Check ingress
kubectl get ingress -n wallet-service

# Check HPA
kubectl get hpa -n wallet-service
```

### 3. Access Application

```bash
# Port forward for local access
kubectl port-forward svc/wallet-service -n wallet-service 8080:80

# Access Swagger UI
open http://localhost:8080/swagger-ui.html

# Or access via Ingress (if configured)
open https://wallet.example.com
```

## 📊 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              wallet-service Namespace                 │   │
│  │                                                       │   │
│  │  ┌─────────────┐     ┌─────────────┐                │   │
│  │  │   Ingress   │────▶│   Service   │                │   │
│  │  │  (NGINX)    │     │  (ClusterIP)│                │   │
│  │  └─────────────┘     └──────┬──────┘                │   │
│  │                             │                        │   │
│  │              ┌──────────────┼──────────────┐        │   │
│  │              │              │              │        │   │
│  │         ┌────▼────┐   ┌────▼────┐   ┌────▼────┐    │   │
│  │         │  Pod 1  │   │  Pod 2  │   │  Pod 3  │    │   │
│  │         │  :8080  │   │  :8080  │   │  :8080  │    │   │
│  │         └────┬────┘   └────┬────┘   └────┬────┘    │   │
│  │              │              │              │        │   │
│  │              └──────────────┼──────────────┘        │   │
│  │                             │                        │   │
│  │         ┌───────────────────┼───────────────────┐   │   │
│  │         │                   │                   │   │   │
│  │    ┌────▼────┐       ┌─────▼─────┐      ┌─────▼─────┐│   │
│  │    │ Postgres │       │   Redis   │      │   Kafka   ││   │
│  │    │  :5432   │       │   :6379   │      │   :9092   ││   │
│  │    └─────────┘       └───────────┘      └───────────┘│   │
│  │                                                       │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Configuration

### Update ConfigMap

```bash
# Edit ConfigMap
kubectl edit configmap wallet-service-config -n wallet-service

# Or apply updated file
kubectl apply -f 01-configmap.yaml

# Restart pods to pick up changes
kubectl rollout restart deployment/wallet-service -n wallet-service
```

### Update Secrets

```bash
# Edit Secret
kubectl edit secret wallet-service-secrets -n wallet-service

# Or create from literal
kubectl create secret generic wallet-service-secrets \
  --from-literal=SPRING_DATASOURCE_PASSWORD=secret \
  --from-literal=APP_JWT_SECRET=your-secret \
  -n wallet-service \
  --dry-run=client -o yaml | kubectl apply -f -
```

### Scale Application

```bash
# Manual scaling
kubectl scale deployment wallet-service --replicas=5 -n wallet-service

# Or update HPA
kubectl edit hpa wallet-service-hpa -n wallet-service
```

## 📈 Monitoring

### View Logs

```bash
# All pods
kubectl logs -l app=wallet-service -n wallet-service

# Specific pod
kubectl logs deployment/wallet-service -n wallet-service

# Follow logs
kubectl logs -f deployment/wallet-service -n wallet-service
```

### Check Metrics

```bash
# Pod metrics
kubectl top pods -n wallet-service

# Node metrics
kubectl top nodes

# HPA status
kubectl get hpa wallet-service-hpa -n wallet-service
```

### Prometheus Metrics

```bash
# Access Prometheus
kubectl port-forward svc/prometheus -n monitoring 9090:9090

# Query metrics
# http_requests_total{namespace="wallet-service"}
# jvm_memory_used_bytes{namespace="wallet-service"}
```

## 🛠️ Troubleshooting

### Pod Won't Start

```bash
# Check pod status
kubectl get pods -n wallet-service

# Describe pod for events
kubectl describe pod <pod-name> -n wallet-service

# Check logs
kubectl logs <pod-name> -n wallet-service

# Check init containers
kubectl logs <pod-name> -c wait-for-postgres -n wallet-service
```

### Service Not Accessible

```bash
# Check endpoints
kubectl get endpoints wallet-service -n wallet-service

# Test from within cluster
kubectl run test --rm -it --image=busybox -n wallet-service -- wget -qO- http://wallet-service.wallet-service.svc.cluster.local/actuator/health
```

### Ingress Not Working

```bash
# Check ingress controller
kubectl get pods -n ingress-nginx

# Check ingress resource
kubectl describe ingress wallet-service-ingress -n wallet-service

# Check ingress controller logs
kubectl logs -l app.kubernetes.io/name=ingress-nginx -n ingress-nginx
```

### Database Connection Issues

```bash
# Test PostgreSQL connection
kubectl run test --rm -it --image=postgres:15-alpine -n wallet-service -- psql -h postgres-service -U postgres -d wallet_db

# Check PostgreSQL logs
kubectl logs -l app=postgres -n wallet-service
```

## 🔄 Deployment Strategies

### Rolling Update

```bash
# Update image
kubectl set image deployment/wallet-service wallet-service=wallet-service:v2 -n wallet-service

# Monitor rollout
kubectl rollout status deployment/wallet-service -n wallet-service

# Rollback if needed
kubectl rollout undo deployment/wallet-service -n wallet-service
```

### Blue-Green Deployment

```bash
# Deploy green version
kubectl apply -f deployment-green.yaml -n wallet-service

# Switch service to green
kubectl patch service wallet-service -p '{"spec":{"selector":{"version":"green"}}}' -n wallet-service

# Delete blue deployment
kubectl delete deployment wallet-service-blue -n wallet-service
```

### Canary Deployment

```yaml
# In ingress, add canary annotations
annotations:
  nginx.ingress.kubernetes.io/canary: "true"
  nginx.ingress.kubernetes.io/canary-weight: "10"
```

## 🔐 Security Best Practices

1. **Use Secrets Management**: Vault, AWS Secrets Manager
2. **Enable Network Policies**: Restrict pod-to-pod communication
3. **Use Pod Security Policies**: Restrict privileged containers
4. **Enable RBAC**: Limit service account permissions
5. **Scan Images**: Use Trivy or similar tools
6. **Enable TLS**: For all external communication
7. **Regular Updates**: Keep images and dependencies updated

```bash
# Scan image
trivy image wallet-service:latest

# Check security context
kubectl get pods -n wallet-service -o jsonpath='{.items[*].spec.securityContext}'
```

## 📊 Resource Planning

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-----------|-------------|-----------|----------------|--------------|
| app | 500m | 2000m | 512Mi | 2Gi |
| postgres | 500m | 1000m | 512Mi | 1Gi |
| redis | 100m | 500m | 128Mi | 256Mi |
| kafka | 500m | 1000m | 512Mi | 1Gi |

## 🎯 Production Checklist

- [ ] Use managed database (RDS, Cloud SQL)
- [ ] Use managed Redis (ElastiCache, Memorystore)
- [ ] Use managed Kafka (MSK, Confluent Cloud)
- [ ] Enable TLS for Ingress
- [ ] Configure proper secrets management
- [ ] Set up monitoring and alerting
- [ ] Configure backup strategy
- [ ] Set up log aggregation
- [ ] Enable audit logging
- [ ] Configure network policies
- [ ] Set up CI/CD pipeline
- [ ] Test disaster recovery

## 📝 Useful Commands

```bash
# Export all resources
kubectl get all -n wallet-service -o yaml > backup.yaml

# Delete all resources
kubectl delete -k k8s/

# Dry run
kubectl apply -k k8s/ --dry-run=client

# Validate manifests
kubeval k8s/*.yaml

# Check resource usage
kubectl top pods -n wallet-service

# Exec into pod
kubectl exec -it deployment/wallet-service -n wallet-service -- sh
```

---

**Documentation:** See `docs/` folder for detailed guides  
**Support:** Contact kubernetes-team@example.com
