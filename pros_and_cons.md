### PROS and CONS

## Computation Scale

AWS EKS on Fargate
```
PROS (+) 
  * Management: No management, cluster simplicity with Fargate profiles.
  * Isolation: No cluster nodes, each pod runs in a micro-VM with Firecracker.
  * Billing: No capacity plan, pay per use, CPU and memory usage only.
  * Scaling: Auto scaling only in pods, more efficient and faster than node election.

CONS (+)
  * Customization: No DaemonSets, there are no EC2 nodes, and also a limited ephemeral disk (~20 GiB).
  * Observability: Agents must be sidecars on pods (instead of cluster-wide DaemonSets).
  * Cost: It can be more expensive than EC2 if you have heavy batch jobs or high compute throughput.
```

AWS ECS on Fargate
```
PROS (+) 
  * Zero infrastructure management: AWS handles provisioning, scaling, patching.
  * Isolation: Strong security boundary per task (ideal for multi-tenancy).
  * Pay-as-you-go: Billing is per vCPU and GiB-hour, no wasted capacity.
  * Auto-scaling: Tasks scale without cluster capacity planning.

CONS (+)
  * Feature limits: No privileged containers, host networking, GPUs.
  * Cost: More expensive for sustained or heavy workloads than EC2.
  * Quotas: Fargate launch throttles and ephemeral storage (~20 GiB default).
  * Observability: No DaemonSets — logging/monitoring agents must run as sidecars (extra cost overhead).
```
---

## Database

AWS RDS PostgreSQL
```
PROS (+) 
  * Scalability: Scales vertically and horizontally via Aurora read replicas.
  * Performance: Strong OLTP performance with indexes and joins.
  * Latency: Low-latency reads in multiple Regions
  
CONS (+)
  * Limitation: Must use RDS Proxy or pooling for apps with millions of users.
  * Operation: Still need to think about version upgrades, storage, scaling thresholds, and failover planning.
  * Multi-Region writes: Global database only supports fast cross-Region reads; writes go to one Region.
```

AWS DynamoDB
```
PROS (+) 
  * Performance: Single-digit millisecond latency at any scale(SSD-backed).
  * Scalability: Horizontal Scaling included by design, Automatic partitioning, trillions of items, 10M+ req/sec.
  * Availability: Built-in multi-AZ replication(multi-region, active-active).

CONS (+)
  * Performance: No Relational queries (no joins, no OR conditions)
  * Flexibility: Schema Less, requires careful (Partition Key/Sort Key) upfront data modeling, Query patterns must be known early.
  * Cost: Expensive if: high read/write rates, inefficient partition keys, large items >400kb
```
  
