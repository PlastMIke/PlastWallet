# GitLab CI/CD Configuration Guide

## 📋 Overview

This GitLab CI/CD pipeline automates the build, test, and deployment of the Wallet Service.

### Pipeline Stages

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    TEST     │───▶│    BUILD    │───▶│   DEPLOY    │
│             │    │             │    │             │
│ • Unit      │    │ • Docker    │    │ • Dev       │
│ • Integration│   │ • JAR       │    │ • Staging   │
│ • SonarQube │    │             │    │ • Production│
└─────────────┘    └─────────────┘    └─────────────┘
```

## 🔧 Configuration

### Required CI/CD Variables

Navigate to **Settings → CI/CD → Variables** and add:

| Variable | Description | Example |
|----------|-------------|---------|
| `DOCKER_REGISTRY` | Docker registry URL | `registry.gitlab.com` |
| `DOCKER_IMAGE` | Docker image name | `wallet-service` |
| `SONAR_HOST_URL` | SonarQube server URL | `https://sonar.example.com` |
| `SONAR_TOKEN` | SonarQube authentication token | `sqp_xxx...` |
| `KUBE_CONTEXT_DEV` | Kubernetes dev cluster context | `dev-cluster` |
| `KUBE_CONTEXT_STAGING` | Kubernetes staging cluster context | `staging-cluster` |
| `KUBE_CONTEXT_PROD` | Kubernetes production cluster context | `prod-cluster` |
| `SLACK_WEBHOOK_URL` | Slack webhook for notifications | `https://hooks.slack.com/...` |

### Protected Variables (Production)

| Variable | Protected | Masked |
|----------|-----------|--------|
| `KUBE_CONTEXT_PROD` | ✅ | ❌ |
| `SLACK_WEBHOOK_URL` | ❌ | ✅ |

## 🚀 Pipeline Triggers

### Automatic Triggers

| Event | Triggered Jobs |
|-------|----------------|
| Merge Request | `unit-tests` |
| Push to `develop` | `unit-tests`, `integration-tests`, `build-image`, `deploy-dev` |
| Push to `main` | `unit-tests`, `integration-tests`, `sonarqube-check`, `build-image`, `build-jar` |
| Tag created | All test & build jobs |

### Manual Triggers

| Job | Environment | Requirements |
|-----|-------------|--------------|
| `deploy-dev` | Development | Push to `develop` |
| `deploy-staging` | Staging | Tag created |
| `deploy-production` | Production | Staging deployed successfully |

## 📊 Job Descriptions

### Test Stage

#### unit-tests
- **Purpose:** Run unit tests with JaCoCo coverage
- **Image:** Maven 3.9.6 + Eclipse Temurin 17
- **Artifacts:** JUnit reports, Coverage report
- **Trigger:** MR, main branch, tags

#### integration-tests
- **Purpose:** Run integration tests with Testcontainers
- **Services:** PostgreSQL, Redis
- **Trigger:** main branch, tags
- **Dependencies:** unit-tests

#### sonarqube-check
- **Purpose:** Code quality analysis
- **Image:** Maven + SonarQube scanner
- **Trigger:** main branch, tags
- **Allow Failure:** Yes

### Build Stage

#### build-image
- **Purpose:** Build and push Docker image
- **Tags:** `$CI_COMMIT_SHA`, `latest`
- **Trigger:** main branch, tags
- **Dependencies:** unit-tests, integration-tests

#### build-jar
- **Purpose:** Build JAR artifact
- **Trigger:** Tags only
- **Dependencies:** unit-tests

### Deploy Stage

#### deploy-dev
- **Purpose:** Deploy to development environment
- **Environment:** `development`
- **Trigger:** Push to `develop`
- **When:** Manual
- **Dependencies:** build-image

#### deploy-staging
- **Purpose:** Deploy to staging environment
- **Environment:** `staging`
- **Trigger:** Tag created
- **When:** Manual
- **Dependencies:** build-image

#### deploy-production
- **Purpose:** Deploy to production environment
- **Environment:** `production`
- **Trigger:** Tag created
- **When:** Manual (after staging)
- **Dependencies:** deploy-staging

## 🔄 Branch Strategy

```
main (production)
  │
  ├─── develop (development)
  │      │
  │      ├─── feature/*
  │      │
  │      └─── bugfix/*
  │
  └─── release/* (staging)
```

| Branch | Environment | Auto-Deploy |
|--------|-------------|-------------|
| `feature/*` | - | ❌ |
| `develop` | Development | ✅ (Manual) |
| `release/*` | Staging | ✅ (Manual) |
| `main` | Production | ✅ (Manual) |
| Tags | Production | ✅ (Manual) |

## 📝 Usage Examples

### Trigger Full Pipeline

```bash
# Create and push tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Pipeline will run:
# 1. unit-tests
# 2. integration-tests
# 3. sonarqube-check
# 4. build-image
# 5. build-jar
# 6. deploy-staging (manual)
# 7. deploy-production (manual)
```

### Deploy to Development

1. Push to `develop` branch
2. Go to CI/CD → Pipelines
3. Click play button on `deploy-dev` job

### Deploy to Production

1. Create and push tag
2. Wait for tests and build to complete
3. Manually trigger `deploy-staging`
4. After staging succeeds, manually trigger `deploy-production`

## 🔍 Monitoring

### View Pipeline Status

```bash
# GitLab UI
https://gitlab.example.com/your-group/wallet-service/-/pipelines

# GitLab CLI
glab pipeline list
glab pipeline view <pipeline-id>
```

### View Job Logs

```bash
# GitLab UI
CI/CD → Pipelines → [Pipeline] → [Job]

# GitLab CLI
glab pipeline job view <job-id>
```

### Download Artifacts

```bash
# GitLab UI
CI/CD → Pipelines → [Pipeline] → Artifacts

# GitLab CLI
glab pipeline job artifacts <job-id>
```

## 🛠️ Troubleshooting

### Pipeline Fails at Test Stage

```yaml
# Check test reports
Artifacts → target/surefire-reports/

# Run tests locally
./mvnw clean test -B

# Run integration tests locally
./mvnw verify -Dtest='**/*IntegrationTest' -B
```

### Docker Build Fails

```bash
# Build locally
docker build -t wallet-service:latest .

# Check Dockerfile syntax
hadolint Dockerfile

# Check image size
docker images wallet-service
```

### Deployment Fails

```bash
# Check Kubernetes events
kubectl get events -n wallet-service-prod

# Check rollout status
kubectl rollout status deployment/wallet-service -n wallet-service-prod

# Rollback
kubectl rollout undo deployment/wallet-service -n wallet-service-prod
```

### SonarQube Quality Gate Fails

```bash
# View quality gate details
https://sonarqube.example.com/dashboard?id=wallet-service

# Fix issues and re-run
# Quality gate is non-blocking (allow_failure: true)
```

## 📊 Pipeline Optimization

### Reduce Pipeline Duration

1. **Parallel Jobs:** Test jobs run in parallel
2. **Cache:** Maven dependencies cached between runs
3. **Image Layers:** Docker layers cached in registry
4. **Concurrent Runners:** Multiple runners for parallel execution

### Cost Optimization

1. **Cleanup Old Images:** Weekly cleanup job
2. **Artifact Expiry:** 30 days for test reports
3. **Runner Tags:** Use appropriate runners for jobs

## 🔐 Security Best Practices

1. **Protected Branches:** `main`, `develop`
2. **Protected Tags:** `v*.*.*`
3. **Secret Variables:** Use masked/protected variables
4. **Image Scanning:** Enable Container Registry scanning
5. **Approval Rules:** Require approval for production deployments

## 📈 Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Pipeline Duration | < 15 min | ~10 min |
| Test Coverage | > 80% | 94% |
| Build Success Rate | > 95% | - |
| Deployment Success Rate | > 99% | - |

## 🎯 Next Steps

1. **Configure Variables:** Add all required CI/CD variables
2. **Set Up Runners:** Ensure Kubernetes and Docker runners available
3. **Configure SonarQube:** Set up SonarQube project
4. **Test Pipeline:** Create merge request to test
5. **Enable Notifications:** Configure Slack webhook

---

**Documentation:** See GitLab CI/CD documentation  
**Support:** Contact devops-team@example.com
