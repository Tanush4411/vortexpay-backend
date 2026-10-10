# Vortexpay - A Distributed Payment Gateway inspired by Razorpay

A distributed, Kubernetes-native payment platform covering order creation, payment authorization, bank callback simulation, settlement and webhooks. It is built as 7 microservices using the same architectural patterns that production payment companies (Stripe, Razorpay, Adyen) rely on.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Design Patterns](#design-patterns)
- [Tech Stack](#tech-stack)
- [Load Testing with JMeter](#load-testing-with-jmeter)
- [Running on GKE](#running-on-gke)
- [Summary](#summary)

## Architecture Overview

| Service | Responsibility |
|---|---|
| `api-gateway` | Dual filter chain auth (API key + BCrypt for service endpoints, JWT for merchant endpoints), rate limiting, routing |
| `payment-service` | Orders, payments, state machine, bank callback simulation |
| `merchant-service` | Merchant accounts, API keys, customers, webhooks |
| `operations-service` | Settlements, webhook delivery, outbox relay |
| `vault-service` | Card tokenization, encryption |
| `config-service` | Centralized config (Spring Cloud Config, git-backed) |
| `discovery-service` | Service registry |

Supporting infrastructure:

- **Postgres**: one database per service
- **Redis**: rate limiting, idempotency, caching, distributed locks
- **Kafka**: event bus
- **Observability**: Prometheus, Grafana, Zipkin
- **Kubernetes**: Deployments, StatefulSets, Services, ConfigMaps, Secrets

<!-- TODO: add an architecture diagram, e.g. docs/architecture.png -->

## Design Patterns

| Pattern | Where | What breaks without it at scale |
|---|---|---|
| **Idempotency keys** | `X-Idempotency-Key` header, Redis-backed `IdempotencyFilter` | Retries are constant at scale (timeouts, LB failover, network blips). Without this, retries create duplicate orders and charges. |
| **Distributed scheduler locking** | ShedLock on `OutboxPoller`, `BankCallbackSimulator` | With more than one replica of a `@Scheduled` job, every replica double-processes the same work. |
| **Transactional outbox** | Outbox table + poller, atomic with the business write | "Write to DB" and "publish to Kafka" can't both be guaranteed under partial failure. A real distributed-systems bug, not an edge case. |
| **Stateless services** | Every service; DB and Redis hold all state | The precondition for horizontal scaling. If consecutive requests needed the same pod, you couldn't add replicas. |
| **Dual filter chain (API key + JWT)** | `api-gateway`: API-key auth for service endpoints (server-to-server payment calls), JWT for merchant endpoints (dashboard/merchant users) | Different callers need different trust models. One auth scheme for everything either forces dashboard users to handle API keys or leaves server integrations dependent on user sessions. |
| **Rate limiting** | Redis-backed, per API key (token bucket, sliding window and fixed window implemented) | One misbehaving client can take everyone down. |
| **Circuit breaker + retry** | Resilience4j around the merchant-service Feign call | More scale means more failure surface. Stops one slow dependency cascading into a full outage. |
| **Full observability** | Prometheus + Grafana (per-service CPU/memory), Zipkin tracing | You can't capacity-plan or debug a system you can't see into. |

## Tech Stack

- Java / Spring Boot, Spring Cloud (Config, Discovery, Gateway), Resilience4j, ShedLock
- PostgreSQL, Redis, Apache Kafka
- Docker, Kubernetes
- Prometheus, Grafana, Zipkin
- Apache JMeter (load testing)

## Load Testing with JMeter

| Scenario | Replicas | Throughput (TPS) | p95 latency | Error rate |
|---|---|---|---|---|
| _TBD_ | | | | |

## Running on GKE

The service images are already pushed to Docker Hub as part of the build, so GKE needs no separate image registry setup. The **same Kubernetes manifests used locally on kind** deploy unchanged to GKE: you only change which cluster `kubectl` points at.

Then expose the `api-gateway` via a `LoadBalancer` Service or Ingress to get a public endpoint.

## What Got Built
 
- **A correct, idempotent, distributed payment flow:** order → payment → async bank resolution → settlement → webhook delivery, using the consistency patterns production fintech systems depend on (transactional outbox, distributed locking, idempotency).
- **A real Kubernetes deployment:** 7 services and 3 stateful data stores, defined as actual `Deployment`, `StatefulSet`, `Service`, `ConfigMap` and `Secret` manifests, then applied, restarted, scaled and debugged against a live cluster. It's the same `kubectl` workflow used on any cloud cluster (EKS/GKE/AKS). Moving to a hosted cloud changes which node the API server runs on and which managed services back the stateful pieces, not whether this counts as deployed.
- **A fully wired observability stack:** Prometheus scraping every service, a custom Grafana dashboard built from scratch, and Zipkin distributed tracing. It was used live to find and confirm every bottleneck listed in [Optimisations and Bug Fixes](#optimisations-and-bug-fixes), not just installed and left unused.
.
