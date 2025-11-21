# Electronic Voting System - AWS Architecture

## Overview

This document presents a scalable AWS architecture designed to support **300 million users** with peak handling capacity of **250,000 requests per second (RPS)** using **Scala** microservices.

## Architecture Strategy

### Horizontal Scaling Approach

The system leverages AWS managed services for maximum scalability and reliability:

- **Amazon EKS (Elastic Kubernetes Service)**: Orchestrates containerized Scala microservices across multiple availability zones
- **AWS Fargate**: Provides serverless compute for containers, eliminating infrastructure management overhead
- **Auto-scaling**: Dynamic pod scaling based on CPU/memory utilization and custom metrics (RPS, active connections)

### High-Level Architecture Components

```
┌─────────────────────────────────────────────────────────────────┐
│                    Route 53 (DNS + Health Checks)               │
└────────────────────────────────┬────────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────────┐
│              CloudFront CDN (Global Edge Caching)               │
└────────────────────────────────┬────────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────────┐
│        AWS WAF + Shield Advanced (DDoS Protection)              │
└────────────────────────────────┬────────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────────┐
│    Application Load Balancer (Multi-AZ, Auto-scaling)           │
└────────────────────────────────┬────────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────────┐
│                         EKS Cluster                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Fargate Pods (Scala/Akka HTTP Voting Service)           │   │
│  │  • Min: 500 pods  • Max: 5000+ pods                      │   │
│  │  • Target: 500 RPS per pod                               │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
┌───────────────────▼──────────┐  ┌──────────▼─────────────────────┐
│   ElastiCache Redis          │  │   Amazon DynamoDB              │
│   (Session + Rate Limiting)  │  │   (Vote Storage - Write)       │
│   • Multi-AZ Cluster         │  │   • On-Demand Capacity         │
│   • 100k ops/sec per node    │  │   • Global Tables (Multi-AZ)   │
└──────────────────────────────┘  └────────────────────────────────┘
                                              │
                                  ┌───────────▼────────────┐
                                  │  Amazon S3 + Athena    │
                                  │  (Analytics Archive)   │
                                  └────────────────────────┘
```

## Handling 300M Users

### Regional Distribution

- **Multi-region deployment**: Deploy across 3-5 AWS regions (US-East, US-West, EU-West, AP-Southeast, SA-East)
- **CloudFront**: Routes users to nearest edge location, reducing latency
- **Route 53**: Geo-routing and latency-based routing policies

### Compute Scaling Strategy

**EKS with Fargate Configuration:**

- **Base capacity**: 500 Fargate pods per region
- **Peak capacity**: 5,000+ pods per region (auto-scales in <2 minutes)
- **Pod specification**: 2 vCPU, 4GB RAM per pod (JVM heap: 3GB)
- **Throughput**: 500 RPS per pod = 2.5M RPS total capacity across regions
- **Scala runtime**: GraalVM Native Image or OpenJDK 21 with optimized GC (ZGC/Shenandoah)

### Rate Limiting & Authentication

- **Redis (ElastiCache)**: Distributed rate limiting (1 vote per user)
- **Cognito**: User authentication and identity management for 300M users
- **API Gateway**: Additional throttling layer (10,000 RPS per account per region)

## Technology Stack

### Backend Services (Scala)

- **Language**: Scala 3.x
- **Build Tool**: sbt 1.9+
- **HTTP Framework**: Akka HTTP or http4s
- **Concurrency**: Cats Effect 3.x or Akka Actors
- **JSON**: Circe or Play JSON
- **Database Client**: AWS SDK for Java 2.x (DynamoDB), Redis4Cats



## Database Trade-offs

### Primary Choice: **Amazon DynamoDB**

**Why DynamoDB:**
- ✅ **Horizontal scalability**: Auto-scales to handle millions of writes/sec
- ✅ **Low latency**: Single-digit millisecond response times
- ✅ **Serverless**: No infrastructure management, pay-per-request
- ✅ **High availability**: Multi-AZ replication by default
- ✅ **Global tables**: Cross-region replication for disaster recovery

**Trade-offs:**
- ❌ **Limited query flexibility**: No complex joins or aggregations
- ❌ **Cost at scale**: High write volume = higher costs (\$1.25 per million writes)
- ❌ **Eventual consistency**: Default read consistency is eventual (configurable to strong)

### Alternative: **Amazon Aurora PostgreSQL with Read Replicas**

**Why Aurora:**
- ✅ **ACID compliance**: Strong consistency guarantees
- ✅ **Complex queries**: SQL support for analytics and reporting
- ✅ **Cost-effective**: Lower cost for read-heavy workloads

**Trade-offs:**
- ❌ **Write scalability**: Limited to 15 read replicas, single write endpoint
- ❌ **Operational overhead**: Requires connection pooling (RDS Proxy, HikariCP)
- ❌ **Cold start**: Scaling takes 5-15 minutes vs. DynamoDB's instant scaling

### Recommended Hybrid Approach

1. **DynamoDB**: Primary write path for vote ingestion (handles 250k RPS)
2. **DynamoDB Streams → Lambda → S3**: Archive votes to S3 for compliance
3. **Athena**: Query S3 data lake for analytics and reporting
4. **ElastiCache Redis**: Cache vote tallies and user voting status

## Cost Estimation (Monthly)

| Service | Configuration | Estimated Cost |
|---------|--------------|----------------|
| EKS | 5 clusters × \$73 | \$365 |
| Fargate | 2,500 avg pods × 2 vCPU | \$180,000 |
| DynamoDB | 250B writes, 50B reads | \$312,500 |
| ElastiCache | 10 cache.r6g.4xlarge nodes | \$12,000 |
| CloudFront | 500TB transfer | \$42,500 |
| ALB | 5 ALBs, 250M LCU-hours | \$18,750 |
| **Total** | | **~\$566,115/month** |

## Security Considerations

- **Encryption**: All data encrypted at rest (KMS) and in transit (TLS 1.3)
- **Network isolation**: Private subnets for EKS, VPC endpoints for AWS services
- **Audit logging**: CloudTrail, CloudWatch Logs, and VPC Flow Logs
- **Compliance**: LGPD, SOC 2, ISO 27001 certified infrastructure
- **Secrets Management**: AWS Secrets Manager for database credentials and API keys

## Monitoring & Observability

- **CloudWatch**: Metrics, logs, and dashboards
- **X-Ray**: Distributed tracing across microservices
- **Prometheus + Grafana**: Container-level metrics within EKS
- **Kamon**: JVM and Akka-specific metrics
- **PagerDuty/Opsgenie**: Incident management and alerting



---
