# VortexPay ⚡

VortexPay is a production-grade, distributed payment gateway backend modeled after industry standards like Razorpay. Designed with a zero-trust security paradigm and event-driven distributed system principles. It enables merchants to onboard seamlessly, tokenize cards in a PCI-compliant vault, execute multi-method payments, and receive reliable asynchronous webhook updates.

---

## 🛠️ Key Features

* **💳 Multi-Method Payment Strategy:** Pluggable adapter layer utilizing the **Strategy Pattern** to support Card, UPI, NetBanking, and Wallet payment processing.
* **🛡️ PCI-Compliant AES-256 Card Vault:** Tokenizes sensitive card details using **AES-256 envelope encryption** via Spring Security Crypto, retaining raw PANs in-memory for under **50ms**.
* **⚡ High-Performance Caching (Redis):** Frequently accessed merchant API keys are cached in Redis to minimize database lookups and reduce overall API latency.
* **🛑 API Rate Limiting (Redis):** Enforces rate limits on API key requests per merchant using Redis to protect the system against abuse.
* **🔄 Idempotency Control (Redis):** Employs Redis for idempotency checks to ensure critical operations, such as order creation, are processed exactly once.
* **🔒 Pessimistic Database Locking:** Applies pessimistic database locks during critical payment state updates to handle high concurrency and prevent race conditions.
* **📣 Transactional Outbox Pattern (Kafka):** Atomically writes events to an Outbox database table within the payment transaction, then streams them to Apache Kafka for reliable, asynchronous delivery to merchant webhooks.

---

## 💻 Tech Stack & Infrastructure

* **Framework & Batch:** Java 17+, Spring Boot 3.x
* **Security & Auth:** Spring Security, JJWT, HMAC-SHA256, AES-256 Envelope Encryption
* **Database & Persistence:** PostgreSQL, Spring Data JPA / Hibernate, Pessimistic Locking
* **Caching & Rate Limiting:** Redis
* **Messaging & Event Streaming:** Apache Kafka, Transactional Outbox
* **Containerization & Deployment:** Docker, Docker Compose

---

## 🔐 Security & Authentication Architecture

VortexPay enforces strict zero-trust boundaries using custom **Spring Security Filter Chains**:

### 1. Merchant Dashboard Security (JWT)
* **Target:** Endpoints intended for merchant administrative workflows (e.g., login, viewing metrics, managing keys).
* **Flow:** Authenticates via credentials, issuing short-lived signed JSON Web Tokens (JWT). Requests pass this token via `Authorization: Bearer`.

### 2. Server-to-Server API Security (API Key)
* **Target:** Programmatic payment endpoints invoked by merchant servers (e.g., create order, tokenize card, initialize payment).
* **Flow:** Merchants generate an `API_KEY` and `KEY_SECRET` pair via the dashboard. For each request, the merchant sends the `API_KEY` and `KEY_SECRET` in the header authentication along with the request.
* **Verification:** VortexPay recalculates and verifies the `KEY_SECRET` locally against the stored secret to authenticate and authorize the request.

---
