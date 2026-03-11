# Wallet Service - Observability Guide

## 📋 Overview

This wallet service includes comprehensive observability with:

- **Prometheus** - Metrics collection and alerting
- **Grafana** - Visualization and dashboards
- **Jaeger** - Distributed tracing
- **Spring Boot Actuator** - Application health and metrics

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Observability Stack                         │
│                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │ Prometheus  │◀───│   Grafana   │    │   Jaeger    │     │
│  │  Metrics    │    │ Dashboards  │    │   Tracing   │     │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘     │
│         │                  │                  │            │
│         └──────────────────┼──────────────────┘            │
│                            │                                │
│                   ┌────────▼────────┐                      │
│                   │ Wallet Service  │                      │
│                   │  (Instrumented) │                      │
│                   └─────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Deploy Observability Stack

```bash
# Deploy all observability components
kubectl apply -f observability/stack/

# Or deploy individually
kubectl apply -f observability/prometheus/
kubectl apply -f observability/grafana/
kubectl apply -f observability/jaeger/
```

### Access Dashboards

```bash
# Prometheus
kubectl port-forward svc/prometheus -n observability 9090:9090
# Open: http://localhost:9090

# Grafana
kubectl port-forward svc/grafana -n observability 80:80
# Open: http://localhost:80
# Login: admin / admin

# Jaeger
kubectl port-forward svc/jaeger -n observability 16686:16686
# Open: http://localhost:16686
```

## 📊 Metrics

### Application Metrics

| Metric | Description | Type |
|--------|-------------|------|
| `http_server_requests_seconds` | HTTP request duration | Histogram |
| `http_server_requests_total` | Total HTTP requests | Counter |
| `jvm_memory_used_bytes` | JVM memory usage | Gauge |
| `hikaricp_connections_active` | Active DB connections | Gauge |
| `wallet_transactions_total` | Total wallet transactions | Counter |
| `wallet_balance_total` | Total wallet balance | Gauge |

### Custom Business Metrics

Add to your service:

```java
@Component
public class WalletMetrics {
    private final MeterRegistry meterRegistry;
    
    public WalletMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Counter for transactions
        meterRegistry.counter("wallet.transactions", "type", "deposit");
        
        // Gauge for total balance
        meterRegistry.gauge("wallet.balance.total", this, WalletMetrics::getTotalBalance);
        
        // Timer for transaction processing
        meterRegistry.timer("wallet.transaction.processing");
    }
}
```

## 🔍 Tracing

### Configure OpenTelemetry

```properties
# application.properties
management.tracing.enabled=true
management.tracing.sampling.probability=1.0
management.otlp.tracing.endpoint=http://jaeger:4318/v1/traces
```

### Add Trace IDs to Logs

```java
@Bean
public Filter tracingFilter(TracingFilter tracingFilter) {
    return tracingFilter;
}
```

Logs will include trace and span IDs:
```
2024-01-01 12:00:00 [traceId=abc123, spanId=def456] Processing transaction...
```

## 📈 Grafana Dashboards

### Pre-configured Dashboards

1. **Wallet Service Overview**
   - Request rate
   - Error rate
   - Response time (P50, P95, P99)
   - Instance count

2. **JVM Metrics**
   - Heap/Non-heap memory
   - GC activity
   - Thread count
   - CPU usage

3. **Database Metrics**
   - Connection pool usage
   - Query execution time
   - Transaction count

4. **Business Metrics**
   - Transaction rate by type
   - Wallet creation rate
   - Balance distribution

### Import Dashboard

```bash
# Copy dashboard JSON to Grafana
kubectl cp observability/grafana/dashboards/wallet-service-dashboard.json \
  <grafana-pod>:/var/lib/grafana/dashboards/ -n observability
```

## 🚨 Alerting

### Configure Alerts

Alerts are defined in `observability/prometheus/alerts.yml`:

| Alert | Condition | Severity |
|-------|-----------|----------|
| WalletServiceDown | Instance down for 1m | Critical |
| HighErrorRate | Error rate > 5% for 5m | Warning |
| HighResponseTime | P95 > 2s for 5m | Warning |
| HighMemoryUsage | Heap > 85% for 5m | Warning |
| DatabasePoolExhausted | Connections > 90% for 2m | Critical |

### Alertmanager Configuration

```yaml
# alertmanager.yml
route:
  group_by: ['alertname', 'severity']
  receiver: 'slack-notifications'
  routes:
    - match:
        severity: critical
      receiver: 'pagerduty'
    - match:
        severity: warning
      receiver: 'slack-notifications'
```

## 🔧 Configuration

### Prometheus Scrape Config

Wallet Service is auto-discovered via Kubernetes annotations:

```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/actuator/prometheus"
```

### Application Configuration

```properties
# Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always

# Metrics
management.metrics.tags.application=wallet-service
management.metrics.export.prometheus.enabled=true

# Tracing
management.tracing.sampling.probability=1.0
```

## 🛠️ Troubleshooting

### Metrics Not Showing

```bash
# Check if Prometheus is scraping
kubectl logs -l app=prometheus -n observability | grep wallet-service

# Check application metrics endpoint
kubectl port-forward svc/wallet-service -n wallet-service 8080:80
curl http://localhost:8080/actuator/prometheus

# Check service discovery in Prometheus
# Go to: http://localhost:9090/targets
```

### Traces Not Showing

```bash
# Check Jaeger is running
kubectl get pods -n observability -l app=jaeger

# Check application tracing config
kubectl logs -l app=wallet-service -n wallet-service | grep -i trace

# Verify trace export
kubectl run test --rm -it --image=curlimages/curl -- \
  curl -X POST http://jaeger.observability:4318/v1/traces
```

### Grafana Dashboards Empty

```bash
# Check datasource configuration
kubectl get configmap grafana-datasources -n observability -o yaml

# Check Prometheus connection in Grafana
# Go to: Configuration → Data Sources → Prometheus → Save & Test

# Verify metrics exist
# Go to: Explore → Select Prometheus → Query: up{job="wallet-service"}
```

## 📊 Query Examples

### Prometheus Queries

```promql
# Request rate
sum(rate(http_server_requests_seconds_count{job="wallet-service"}[5m]))

# Error rate
sum(rate(http_server_requests_seconds_count{job="wallet-service", status=~"5.."}[5m])) 
/ sum(rate(http_server_requests_seconds_count{job="wallet-service"}[5m]))

# P95 response time
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="wallet-service"}[5m])) by (le))

# Transaction rate
sum(rate(wallet_transactions_total{job="wallet-service"}[5m]))

# Memory usage
jvm_memory_used_bytes{job="wallet-service", area="heap"} 
/ jvm_memory_max_bytes{job="wallet-service", area="heap"}
```

### Jaeger Search

```
# Find all traces for wallet service
service.name = wallet-service

# Find slow transactions
service.name = wallet-service AND duration > 1s

# Find errors
service.name = wallet-service AND http.status_code = 500
```

## 📈 Best Practices

1. **Sampling Rate**: Adjust based on traffic (1.0 for dev, 0.1 for prod)
2. **Retention**: Keep metrics for 15-30 days, traces for 7 days
3. **Alerts**: Start with critical alerts, add warnings gradually
4. **Dashboards**: Create role-specific views (ops, dev, business)
5. **Logs**: Correlate with traces using trace IDs

## 🔐 Security

1. **Authentication**: Enable for Grafana and Prometheus
2. **TLS**: Use HTTPS for all endpoints
3. **Network Policies**: Restrict access to observability namespace
4. **Secrets**: Store credentials in Kubernetes Secrets

## 📝 Resources

| Component | CPU Request | Memory Request | Storage |
|-----------|-------------|----------------|---------|
| Prometheus | 500m | 1Gi | 50Gi |
| Grafana | 250m | 256Mi | 10Gi |
| Jaeger | 500m | 512Mi | - |

## 🎯 Next Steps

1. **Deploy Stack:** `kubectl apply -f observability/stack/`
2. **Access Grafana:** http://localhost:80 (admin/admin)
3. **Import Dashboard:** Load wallet-service-dashboard.json
4. **Configure Alerts:** Set up Alertmanager for notifications
5. **Customize Metrics:** Add business-specific metrics

---

**Documentation:** See individual component README files  
**Support:** Contact observability-team@example.com
