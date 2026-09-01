# System Design: E-Commerce Microservices Backend

> Last verified: 2026-08-31. Every detail in this document is cross-referenced against the actual source code at HEAD.

---

## 1. Executive Summary

This is a Spring Boot e-commerce backend built as a distributed microservices system. The stack targets **Java 25**, **Spring Boot 4.0.7/4.1.0**, and **Spring Cloud 2025.1.2**. Each service owns its data store, authenticates through Keycloak (OAuth2 JWT), and registers with Eureka for discovery. Configuration is externalized through a git-backed Spring Cloud Config Server. Order events flow asynchronously through Apache Kafka. The system is containerized with Docker Compose and built using Jib (distroless images).

**Core architectural decisions:**
- Database-per-service (MongoDB for users, PostgreSQL for products and orders)
- API Gateway (Spring Cloud Gateway WebFlux) with Redis token-bucket rate limiting
- OAuth2 Resource Server validation in each downstream service (gateway does not validate JWTs)
- Kafka KRaft single-node broker for order event publishing
- Spring Cloud Bus over RabbitMQ for config refresh propagation
- Distributed tracing via Micrometer + Zipkin at 100% sample rate

---

## 2. High-Level Architecture Diagram

```
                              Browser / SPA
                                   │
                                   ▼
                         ┌───────────────────┐
                         │   API Gateway     │
                         │      :8080        │
                         └─────┬─────┬─────┬─┘
                               │     │     │
                 ┌─────────────┘     │     └─────────────┐
                 ▼                   ▼                    ▼
        ┌──────────────┐   ┌───────────────┐   ┌───────────────┐
        │  UserModule  │   │ ProductModule │   │  OrderModule  │
        │    :8082     │   │     :8083     │   │     :8084     │
        └──────┬───────┘   └───────┬───────┘   └───────┬───────┘
               │                   │                    │
               ▼                   ▼                    ▼
     ┌──────────────┐    ┌──────────────┐     ┌─────────────────┐
     │  MongoDB     │    │  PostgreSQL  │     │  PostgreSQL     │
     │  Atlas       │    │  (Neon)      │     │  (Neon)         │
     └──────────────┘    └──────────────┘     └────────┬────────┘
                                                       │
                                                       ▼
                                             ┌─────────────────┐
                                             │     Kafka       │
                                             │     :9092       │
                                             └────────┬────────┘
                                                      │
                                                      ▼
                                           ┌────────────────────┐
                                           │ NotificationModule │
                                           │       :8085        │
                                           └────────┬───────────┘
                                                    │
                                                    ▼
                                           ┌──────────────────┐
                                           │   SMTP Server    │
                                           │  (Gmail/TLS)     │
                                           └──────────────────┘

            ┌──────────────────┐     ┌──────────────────┐
            │   EurekaServer   │     │  ConfigServer    │
            │      :8761       │     │     :8888        │
            └──────────────────┘     └────────┬────────┘
                                              │
                                              ▼
                                    ┌──────────────────┐
                                    │     RabbitMQ     │
                                    │  (Spring Cloud   │
                                    │       Bus)       │
                                    └──────────────────┘

            ┌──────────────────┐     ┌──────────────────┐
            │    Keycloak      │     │      Redis       │
            │     :8443        │     │  (rate limiter)  │
            └──────────────────┘     └──────────────────┘

            ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
            │     Zipkin       │     │   Prometheus     │     │     Grafana      │
            │     :9411        │     │     :9090        │     │     :3000        │
            └──────────────────┘     └──────────────────┘     └──────────────────┘

            ┌──────────────────┐     ┌──────────────────┐
            │      Loki        │     │      Alloy       │
            │     :3100        │     │  (log shipper)   │
            └──────────────────┘     └──────────────────┘
```

---

## 3. Service Inventory

| Service | Port | Framework | Persistence | Key Dependencies | Docker Image |
|---|---|---|---|---|---|
| **Gateway** | 8080 | Spring Cloud Gateway (WebFlux, reactive) | Redis (rate limit state) | Eureka Client, Resilience4j, Zipkin | `ishonpanda/ecomm-gateway` |
| **ConfigServer** | 8888 | Spring MVC | None (git backend) | Config Server, Spring Cloud Bus (RabbitMQ), Eureka Client | `ishonpanda/ecomm-config` |
| **EurekaServer** | 8761 | Spring MVC | None | Eureka Server (standalone) | `ishonpanda/ecomm-eureka` |
| **UserModule** | 8082 | Spring MVC | MongoDB Atlas (`userDB`) | Keycloak Admin Client, Eureka Client, Zipkin | `ishonpanda/ecomm-user` |
| **ProductModule** | 8083 | Spring MVC | PostgreSQL (Neon) | JPA, Eureka Client, Zipkin | `ishonpanda/ecomm-product` |
| **OrderModule** | 8084 | Spring MVC | PostgreSQL (Neon) | JPA, Kafka Binder, Resilience4j, Eureka Client, Zipkin | `ishonpanda/ecomm-order` |
| **NotificationModule** | 8085 | Spring (no web) | None (DB-less) | Kafka Binder, JavaMailSender, Eureka Client, Zipkin | `ishonpanda/ecomm-notification` |

---

## 4. Request Flow: Checkout (Critical Path)

The checkout flow is the most architecturally significant operation, touching every layer of the system:

```
Browser/SPA
  │
  │  POST /api/order  (Authorization: Bearer <JWT>)
  ▼
Gateway :8080
  │  1. Redis rate limiter → key = JWT.sub (or "anonymous")
  │  2. Circuit breaker check (gatewayBreaker)
  │  3. Route → lb://ORDERMODULE
  ▼
OrderModule :8084  (createOrder)
  │
  │  ── @Transactional starts ──
  │
  │  4. Resolve user via HTTP:
  │     GET /api/user/keycloak/{sub}  →  lb://USERMODULE
  │     [@CircuitBreaker("userBreaker") + @Retry("userRetry")]
  │
  │  5. Load cart from PostgreSQL
  │
  │  6. Batch-fetch products via HTTP:
  │     POST /api/product/batch  {productIds}  →  lb://PRODUCTMODULE
  │     [@CircuitBreaker("productBreaker") + @Retry("productRetry")]
  │
  │  7. Per cart item:
  │     a. Compute discounted price: price × (1 - discount/100) × quantity
  │     b. Decrement stock via HTTP:
  │        PUT /api/product/dec/{id}/{qty}  →  lb://PRODUCTMODULE
  │     c. Create OrderItem entity
  │
  │  8. Persist Order + OrderItems (saveAndFlush)
  │
  │  9. Clear cart (orphan removal)
  │
  │  ── @Transactional commits ──
  │
  │  10. TransactionSynchronization.afterCommit():
  │      streamBridge.send("createOrder-out-0", OrderEvent)
  │      → Kafka topic "ecomm.exchange.order"
  ▼
NotificationModule :8085  (Consumer group: "notification")
  │  11. orderEventConsumer receives OrderEvent
  │  12. Builds email body from event payload
  │  13. JavaMailSender.send() → SMTP (Gmail/MailerSend)
  ▼
Email sent to user
```

**Resilience guarantees on this path:**
- If UserModule is down → userBreaker fallback → 400 "USER service down"
- If ProductModule is down → productBreaker fallback → 400 "PRODUCT service down"
- Stock decrement uses atomic conditional update (`WHERE stock >= quantity`)
- Order and Cart use `@Version` for optimistic locking
- Kafka event is published only after successful DB commit (transactional outbox pattern via `TransactionSynchronization`)

---

## 5. Synchronous Inter-Service Communication

All inter-service HTTP calls use Spring 6 `RestClient` with Eureka load balancing:

```
┌─────────────────────────────────────────────────────────────┐
│                    OrderModule :8084                         │
│                                                             │
│  ┌───────────────────────┐   ┌───────────────────────┐     │
│  │  UserServiceClient    │   │  ProductServiceClient  │     │
│  │  (@HttpExchange)      │   │  (@HttpExchange)       │     │
│  │                       │   │                        │     │
│  │  GET /api/user/       │   │  GET  /api/product/    │     │
│  │    keycloak/{id}      │   │    {id}                │     │
│  │                       │   │  POST /api/product/    │     │
│  │                       │   │    batch               │     │
│  │                       │   │  PUT  /api/product/    │     │
│  │                       │   │    dec/{id}/{qty}      │     │
│  │                       │   │  PUT  /api/product/    │     │
│  │                       │   │    inc/{id}/{qty}      │     │
│  └───────────┬───────────┘   └───────────┬────────────┘     │
│              │                           │                  │
│              │  RestClient.builder()     │                  │
│              │  .requestFactory(         │                  │
│              │    JdkClientHttpRequest   │                  │
│              │    Factory.builder()      │                  │
│              │    .connectTimeout(5000)  │                  │
│              │    .readTimeout(10000)    │                  │
│              │    .build())             │                  │
└──────────────┼───────────────────────────┼──────────────────┘
               │                           │
               ▼                           ▼
         lb://USERMODULE             lb://PRODUCTMODULE
         (Eureka resolved)           (Eureka resolved)
```

**Timeouts:** Connect = 5000ms, Response = 10000ms.

**Circuit breakers:** `productBreaker`, `productRetry`, `userBreaker`, `userRetry` (Resilience4j, configured in `application.yaml` via ConfigServer).

---

## 6. Asynchronous Event Flow (Kafka)

```
┌──────────────────────────────────────────────────────────────────┐
│                        KAFKA CLUSTER                             │
│                   (KRaft single-node, :9092)                     │
│                                                                  │
│  Topic: ecomm.exchange.order                                     │
│  Partitions: default (1)                                         │
│  Replication factor: 1                                           │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  Producers (OrderModule)                               │     │
│  │                                                        │     │
│  │  Binding: createOrder-out-0                            │     │
│  │    Trigger: checkout (POST /api/order)                 │     │
│  │    Condition: after DB commit                          │     │
│  │                                                        │     │
│  │  Binding: cancelOrder-out-0                            │     │
│  │    Trigger: cancellation (POST /api/order/cancel)      │     │
│  │    Condition: after DB commit                          │     │
│  │                                                        │     │
│  │  Binding: updateOrderStatus-out-0                      │     │
│  │    Trigger: status change (PUT /api/order/{status})    │     │
│  │    Header: target_key = order.delivered /              │     │
│  │            order.shipped / order.confirmed             │     │
│  │    Condition: after DB commit                          │     │
│  └────────────────────────┬───────────────────────────────┘     │
│                           │                                      │
│                           ▼                                      │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  Consumer (NotificationModule)                         │     │
│  │                                                        │     │
│  │  Binding: orderEventConsumer-in-0                      │     │
│  │  Consumer group: "notification"                        │     │
│  │  Function bean: Consumer<OrderEvent>                   │     │
│  │                                                        │     │
│  │  Dispatches on OrderStatus:                            │     │
│  │    CREATED/CONFIRMED → "Order confirmed" email         │     │
│  │    SHIPPED           → "Order shipped" email           │     │
│  │    DELIVERED         → "Order delivered" email         │     │
│  │    CANCELLED         → "Order cancelled" email         │     │
│  └────────────────────────┬───────────────────────────────┘     │
│                           │                                      │
└───────────────────────────┼──────────────────────────────────────┘
                            │
                            ▼
                   JavaMailSender → SMTP
                   (Gmail TLS :587)
```

**OrderEvent payload:**
```json
{
  "id": "uuid",
  "userId": "mongo-doc-id",
  "email": "user@example.com",
  "totalAmount": 99.99,
  "status": "PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED|CREATED",
  "orderItems": [
    { "id": "...", "productId": "...", "quantity": 2, "totalPrice": 49.99 }
  ],
  "createdAt": "2026-08-31T12:00:00"
}
```

---

## 7. Identity and Access Control

```
┌─────────────────────────────────────────────────────────────────────┐
│                         KEYCLOAK :8443                              │
│                                                                     │
│  Realm: ecomm-app                                                   │
│                                                                     │
│  ┌─────────────────────────────┐  ┌──────────────────────────────┐ │
│  │  Client: oauth2-ecomm-pkce  │  │  Client: ecomm-manage-user   │ │
│  │  (Public, PKCE)             │  │  (Confidential, client-cred) │ │
│  │                             │  │                              │ │
│  │  Used by: SPA/Frontend      │  │  Used by: UserModule only    │ │
│  │  Flow: Auth Code + PKCE     │  │  Purpose: Create/Delete      │ │
│  │  Roles in access token:     │  │  Keycloak users via Admin API│ │
│  │  resource_access[client]    │  │                              │ │
│  └─────────────────────────────┘  └──────────────────────────────┘ │
│                                                                     │
│  Roles: USER, SELLER, ADMIN                                         │
│  (mapped to Spring authorities: ROLE_USER, ROLE_SELLER, ROLE_ADMIN)│
└─────────────────────────────────────────────────────────────────────┘
         │
         │  JWT issued
         ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  DOWNSTREAM SERVICE (Resource Server)               │
│                                                                     │
│  Each module has identical SecurityConfig:                          │
│    1. NimbusJwtDecoder validates JWT signature + issuer             │
│    2. JwtAuthenticationConverter extracts roles from:              │
│       jwt.getClaimAsMap("resource_access")                         │
│         -> ["oauth2-ecomm-pkce"].roles                             │
│         -> Stream<String> -> SimpleGrantedAuthority("ROLE_" + r)   │
│    3. @PreAuthorize or HttpSecurity matchers enforce role access    │
│                                                                     │
│  Identity propagation:                                              │
│    - JWT sub == Keycloak user ID                                   │
│    - Controllers: @AuthenticationPrincipal Jwt jwt                 │
│    - Internal calls: forward Authorization header + trace headers   │
└─────────────────────────────────────────────────────────────────────┘
```

**Route-level authorization matrix:**

| Endpoint | Gateway | UserModule | ProductModule | OrderModule |
|---|---|---|---|---|
| `POST /api/user` | Rate limit only | PUBLIC | - | - |
| `GET /api/user/**` | Rate limit only | USER/SELLER/ADMIN | - | - |
| `PUT /api/user` | Rate limit only | JWT (self) | - | - |
| `DELETE /api/user` | Rate limit only | JWT (self) | - | - |
| `POST /api/address/user` | Rate limit only | USER | - | - |
| `GET/PUT/DEL /api/address/**` | Rate limit only | USER | - | - |
| `GET /api/product/**` | Rate limit only | - | PUBLIC | - |
| `GET /api/category/**` | Rate limit only | - | PUBLIC | - |
| `POST /api/product/**` | Rate limit only | - | SELLER | - |
| `PUT /api/product/**` | Rate limit only | - | SELLER | - |
| `DELETE /api/product/**` | Rate limit only | - | SELLER | - |
| `POST /api/cart/**` | Rate limit only | - | - | USER |
| `GET /api/cart` | Rate limit only | - | - | USER |
| `GET/POST /api/order/**` | Rate limit only | - | - | USER/SELLER/ADMIN |

---

## 8. Gateway Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    GATEWAY :8080 (WebFlux)                       │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Programmatic RouteLocator                   │   │
│  │                                                          │   │
│  │  Route("USERMODULE")                                     │   │
│  │    Path: /api/user/**, /api/address/**                   │   │
│  │    Filters:                                              │   │
│  │      1. CircuitBreaker(gatewayBreaker, /fallback/USER)   │   │
│  │      2. RequestRateLimiter(redisRateLimiter, jwtKey)     │   │
│  │    URI: lb://USERMODULE                                  │   │
│  │                                                          │   │
│  │  Route("PRODUCTMODULE")                                  │   │
│  │    Path: /api/product/**, /api/category/**               │   │
│  │    Filters:                                              │   │
│  │      1. CircuitBreaker(gatewayBreaker, /fallback/PRODUCT)│   │
│  │      2. RequestRateLimiter(redisRateLimiter, jwtKey)     │   │
│  │    URI: lb://PRODUCTMODULE                               │   │
│  │                                                          │   │
│  │  Route("ORDERMODULE")                                    │   │
│  │    Path: /api/cart/**, /api/order/**                     │   │
│  │    Filters:                                              │   │
│  │      1. CircuitBreaker(gatewayBreaker, /fallback/ORDER)  │   │
│  │      2. RequestRateLimiter(redisRateLimiter, jwtKey)     │   │
│  │    URI: lb://ORDERMODULE                                 │   │
│  │                                                          │   │
│  │  Route("Eureka-server")                                  │   │
│  │    Path: /eureka/main → rewrite /                        │   │
│  │    Filters: CircuitBreaker → /fallback/EUREKA            │   │
│  │    URI: lb://EUREKASERVER                                │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌──────────────────────────────┐  ┌────────────────────────┐  │
│  │  Redis Rate Limiter          │  │  Fallback Controller   │  │
│  │                              │  │                        │  │
│  │  Algorithm: Token Bucket     │  │  /fallback/{service}   │  │
│  │  Replenish rate: 100 rps     │  │  → 503 + JSON body:    │  │
│  │  Burst capacity: 250         │  │  { status, message,    │  │
│  │  Key: JWT.sub (parsed from   │  │    service, request,   │  │
│  │       Bearer token)          │  │    timestamp }         │  │
│  │  Fallback key: "anonymous"   │  │                        │  │
│  └──────────────────────────────┘  └────────────────────────┘  │
│                                                                 │
│  No JWT validation. No CORS configuration. Stateless.          │
│  HTTP client: connect=5000ms, response=10000ms                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 9. Data Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DATA STORES                                  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │  MongoDB Atlas (MONGODB_URL)                                  │ │
│  │  Database: userDB                                             │ │
│  │  Collections: users, addresses                                │ │
│  │  Used by: UserModule                                          │ │
│  │  Access pattern: Keycloak user ID lookup, CRUD, pagination    │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │  PostgreSQL (Neon serverless)                                 │ │
│  │  Database: neondb                                             │ │
│  │  Used by: ProductModule, OrderModule                          │ │
│  │                                                               │ │
│  │  ProductModule tables:                                        │ │
│  │    products (id, name, seller_id, price, discount,            │ │
│  │              stock_quantity, image_url, active, category_id)  │ │
│  │    categories (id, name, created_at)                          │ │
│  │    Soft-delete: @SQLRestriction("active = true")              │ │
│  │                                                               │ │
│  │  OrderModule tables:                                          │ │
│  │    orders (id, user_id, total_amount, status, version, ...)  │ │
│  │    order_items (id, order_id, product_id, quantity,           │ │
│  │                 total_price, seller_id, status)               │ │
│  │    carts (id, user_id, version)                               │ │
│  │    cart_items (id, cart_id, product_id, quantity, version)    │ │
│  │    Optimistic locking: @Version on Order, Cart, CartItem      │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │  Redis (RedisLabs cloud)                                      │ │
│  │  Used by: Gateway only                                        │ │
│  │  Purpose: Token bucket rate limiter state                     │ │
│  │  Key format: "{route_id}.{jwt_sub}" or "{route_id}.anonymous" │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │  RabbitMQ (CloudAMQP)                                         │ │
│  │  Used by: Spring Cloud Bus only                               │ │
│  │  Purpose: Config refresh propagation (/busrefresh endpoint)   │ │
│  │  NOT used for application messaging (Kafka replaced it)       │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  Key principle: No shared databases between services.               │
│  Each service owns its data and exposes it only through its API.    │
└─────────────────────────────────────────────────────────────────────┘
```

**Service-to-database ownership:**

| Service | Database | Type | Why |
|---|---|---|---|
| UserModule | MongoDB Atlas (userDB) | Document | Flexible schema for user profiles, nested addresses |
| ProductModule | PostgreSQL (Neon) | Relational | Structured catalog, category joins, stock management |
| OrderModule | PostgreSQL (Neon) | Relational | Transactional consistency for orders, carts, order items |
| NotificationModule | None | - | Stateless event consumer, no persistence needed |

---

## 10. Resilience and Fault Tolerance

```
┌─────────────────────────────────────────────────────────────────┐
│                    RESILIENCE PATTERNS                           │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Gateway Layer                                            │ │
│  │                                                           │ │
│  │  1. Redis Token-Bucket Rate Limiting                      │ │
│  │     - Per-user (JWT.sub) throttling                       │ │
│  │     - Shared "anonymous" bucket for unauthenticated       │ │
│  │     - Configurable: replenish-rate, burst-capacity        │ │
│  │                                                           │ │
│  │  2. Resilience4j Circuit Breaker                          │ │
│  │     - Shared instance: gatewayBreaker                     │ │
│  │     - Fallback: forward:/fallback/{SERVICE} → 503         │ │
│  │     - Applied to all service routes + Eureka              │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  OrderModule (checkout/cancel inter-service calls)        │ │
│  │                                                           │ │
│  │  @CircuitBreaker("productBreaker") + @Retry("productRetry")│
│  │    - decreaseProductQuantity                              │ │
│  │    - increaseProductQuantity                              │ │
│  │    - getBatch (product fetch)                             │ │
│  │                                                           │ │
│  │  @CircuitBreaker("userBreaker") + @Retry("userRetry")    │ │
│  │    - getUserByKeycloakId                                  │ │
│  │                                                           │ │
│  │  Failure modes:                                           │ │
│  │    Circuit open → fallback method → 400 "SERVICE down"    │ │
│  │    No retry on stock-changing calls (idempotency risk)    │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Data-Level Consistency                                   │ │
│  │                                                           │ │
│  │  Optimistic locking (@Version):                           │ │
│  │    - Order, Cart, CartItem entities                       │ │
│  │    - Concurrent modification → stale data exception       │ │
│  │                                                           │ │
│  │  Atomic stock operations:                                 │ │
│  │    - decreaseStock: UPDATE WHERE stock >= quantity        │ │
│  │    - No retry on stock calls (avoids double-decrement)    │ │
│  │                                                           │ │
│  │  Atomic cancellation claim:                               │ │
│  │    - UPDATE orders SET status=CANCELLED                   │ │
│  │      WHERE status IN (PENDING,CONFIRMED,CREATED,SHIPPED)  │ │
│  │      AND user_id = ? AND version = ?                      │ │
│  │    - Returns affected rows; 0 = lost race                 │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Event Publishing (Transactional Outbox Pattern)          │ │
│  │                                                           │ │
│  │  TransactionSynchronizationManager.registerSynchronization│ │
│  │    afterCommit() {                                        │ │
│  │      streamBridge.send("createOrder-out-0", event);       │ │
│  │    }                                                      │ │
│  │                                                           │ │
│  │  Guarantees:                                              │ │
│  │    - Event published only after DB commit                 │ │
│  │    - No duplicate events on rollback                      │ │
│  │    - No lost events on application crash (trade-off)      │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 11. Configuration Management

```
┌─────────────────────────────────────────────────────────────────┐
│                    CONFIG FLOW                                   │
│                                                                 │
│  ┌─────────────────────────────────────────┐                   │
│  │  Git Repository (ecomm-config)           │                   │
│  │  github.com/ishonpanda12161/ecomm-config │                   │
│  │                                          │                   │
│  │  config/                                 │                   │
│  │    gateway-dev.yaml                      │                   │
│  │    usermodule-dev.yaml                   │                   │
│  │    productmodule-dev.yaml                │                   │
│  │    ordermodule-dev.yaml                  │                   │
│  │    notificationmodule-dev.yaml           │                   │
│  │    eurekaserver-dev.yaml                 │                   │
│  │    configserver-dev.yaml                 │                   │
│  └────────────────────┬────────────────────┘                   │
│                       │ git clone                               │
│                       ▼                                         │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  ConfigServer :8888                                    │    │
│  │                                                        │    │
│  │  Profile: native (set in yaml) but git backend wins   │    │
│  │  Serves: /{application}/{profile}                      │    │
│  │  Eureka-registered (fetches registry every 5s)         │    │
│  │  Spring Cloud Bus (RabbitMQ) for /busrefresh           │    │
│  │  Exposed actuator: health, info, metrics,              │    │
│  │                    busrefresh, refresh, shutdown        │    │
│  └────────────────────┬───────────────────────────────────┘    │
│                       │ HTTP                                    │
│                       ▼                                         │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Each Service's application.yaml:                      │    │
│  │                                                        │    │
│  │  spring:                                               │    │
│  │    application:                                        │    │
│  │      name: {ServiceName}                               │    │
│  │    profiles:                                           │    │
│  │      active: dev                                       │    │
│  │    config:                                             │    │
│  │      import: optional:configserver:${CONFIG_SERVER_URL} │    │
│  │                                                        │    │
│  │  Effective properties resolved from ConfigServer       │    │
│  │  (overrides local application.yaml)                    │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                 │
│  Secret management: .env file (gitignored)                     │
│  All secrets (DB creds, Keycloak secret, SMTP pass) in .env    │
│  Passed as environment variables in docker-compose              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 12. Containerization and Deployment

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOCKER COMPOSE STACK                          │
│                    Network: backend (bridge)                     │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Application Services (Jib-built, distroless/java25)      │ │
│  │                                                           │ │
│  │  Gateway         :8080  (mem_limit: 512m)                │ │
│  │  UserModule      :8082  (mem_limit: 512m)                │ │
│  │  ProductModule   :8083  (mem_limit: 512m)                │ │
│  │  OrderModule     :8084  (mem_limit: 800m)                │ │
│  │  NotificationMod :8085  (mem_limit: 512m)                │ │
│  │                                                           │ │
│  │  All depend on: eureka-server, config-server              │ │
│  │  Gateway additionally depends on all 4 services           │ │
│  │  restart: unless-stopped                                  │ │
│  │  Volumes: ../Logs:/workspace/Logs (shared log dir)        │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Infrastructure Services                                  │ │
│  │                                                           │ │
│  │  ConfigServer   :8888  (mem_limit: 512m, restart)        │ │
│  │  EurekaServer   :8761  (mem_limit: 512m, restart)        │ │
│  │  Keycloak       :8443  (mem_limit: 800m)                 │ │
│  │                   start-dev mode, bootstrap admin/admin   │ │
│  │  Kafka          :9092  (mem_limit: 800m, KRaft)          │ │
│  │                   confluentinc/cp-kafka:8.0.7             │ │
│  │                   dual listeners: host + docker           │ │
│  │                   Volume: kafka_data                      │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  External Dependencies (not containerized)                │ │
│  │                                                           │ │
│  │  PostgreSQL: Neon serverless (cloud)                      │ │
│  │  MongoDB:    Atlas cluster (cloud)                        │ │
│  │  Redis:      RedisLabs cloud                              │ │
│  │  RabbitMQ:   CloudAMQP (Spring Cloud Bus only)            │ │
│  │  Zipkin:     Standalone jar (:9411)                       │ │
│  │  SMTP:       Gmail / MailerSend                           │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Build: Jib Maven plugin → distroless/java25 base              │
│  No Dockerfile required. Push to Docker Hub (ishonpanda/*)     │
│  Startup order enforced via depends_on in docker-compose       │
└─────────────────────────────────────────────────────────────────┘
```

**Startup dependency graph:**

```
ConfigServer ─────────────────────────┐
       │                              │
       ▼                              ▼
EurekaServer                    (all services)
       │                         depend on both
       ├── UserModule
       ├── ProductModule
       ├── OrderModule (also depends on User + Product)
       ├── NotificationModule
       └── Gateway (depends on all 4 services)
```

---

## 13. Observability Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    THREE PILLARS                                 │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  1. TRACING                                               │ │
│  │                                                           │ │
│  │  Stack: Micrometer Tracing + Brave + Zipkin               │ │
│  │  Sample rate: 1.0 (100% of requests)                      │ │
│  │  Endpoint: Zipkin :9411                                    │ │
│  │                                                           │ │
│  │  Every service exports spans via:                         │ │
│  │    spring-boot-starter-zipkin                             │ │
│  │    micrometer-tracing-bridge-brave                        │ │
│  │                                                           │ │
│  │  Cross-service trace propagation:                         │ │
│  │    - HTTP: W3C traceparent headers forwarded              │ │
│  │    - Kafka: trace headers carried in message headers      │ │
│  │                                                           │ │
│  │  Log correlation:                                         │ │
│  │    Pattern: %X{traceId} in logging format                 │ │
│  │    Every log line includes trace ID for correlation       │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  2. METRICS                                               │ │
│  │                                                           │ │
│  │  Stack: Prometheus + Grafana                              │ │
│  │                                                           │ │
│  │  Prometheus scrapes:                                      │ │
│  │    - ProductModule /actuator/prometheus  (3s interval)    │ │
│  │    - UserModule    /actuator/prometheus  (3s interval)    │ │
│  │    - OrderModule   /actuator/prometheus  (3s interval)    │ │
│  │                                                           │ │
│  │  Grafana datasources:                                     │ │
│  │    - Prometheus (default)                                 │ │
│  │    - Loki (log search)                                    │ │
│  │                                                           │ │
│  │  Exposed actuator endpoints (per service):                │ │
│  │    - /actuator/health/** (public)                         │ │
│  │    - /actuator/info (public)                              │ │
│  │    - /actuator/prometheus (public)                        │ │
│  │    - /actuator/** (ADMIN role only)                       │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  3. LOGGING                                               │ │
│  │                                                           │ │
│  │  Stack: Alloy (log shipper) + Loki + Grafana              │ │
│  │                                                           │ │
│  │  Each service writes to:                                  │ │
│  │    Logs/${spring.application.name}.log                    │ │
│  │                                                           │ │
│  │  Alloy ships logs to Loki (:3100)                         │ │
│  │  Loki stores in MinIO (S3-compatible)                     │ │
│  │  Grafana queries via Loki datasource                      │ │
│  │                                                           │ │
│  │  Log format:                                              │ │
│  │    %d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread]       │ │
│  │    [%X{traceId}] %logger{36} - %msg%n                    │ │
│  │                                                           │ │
│  │  Log level: WARN (root), detailed for debugging          │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 14. Known Gaps and Risks

| # | Severity | Issue | Impact |
|---|---|---|---|
| 1 | **Critical** | No CORS configuration anywhere | Browser frontend cannot call API without dev proxy |
| 2 | **High** | `GET /api/user/keycloak/{id}` uses `compareTo` instead of equality | Can leak other users' data or wrongly 401 the owner |
| 3 | **High** | Stock decrement before order insert; no saga/outbox | Order fails but stock already decremented (not rolled back remotely) |
| 4 | **High** | `sellerId` on product creation comes from request body, not JWT | Any user can claim to be seller of any product |
| 5 | **Medium** | Address PUT/DELETE have no ownership check | Any authenticated user can modify any address |
| 6 | **Medium** | Address endpoints reject ADMIN/SELLER tokens | Admins cannot manage addresses |
| 7 | **Medium** | `PUT /api/user` forces password on every update | Profile updates always reset password |
| 8 | **Medium** | No dead-letter queue or retry for failed Kafka events | Lost notifications on transient SMTP failures |
| 9 | **Low** | Duplicated `JwtAuthenticationConverter` in every module | Renaming Keycloak client requires coordinated edits in 3+ files |
| 10 | **Low** | `ddl-auto: update` in production | No migration tracking, schema drift risk |
| 11 | **Low** | Leaked credentials in git history (Aiven Kafka) | History rewrite recommended |
| 12 | **Low** | Gateway rate limits all anonymous traffic into one bucket | DDoS from unauthenticated requests |
| 13 | **Low** | ConfigServer Spring Boot 4.0.7 vs 4.1.0 elsewhere | Version drift, potential subtle incompatibilities |

---

## 15. Future Evolution

| Phase | Deliverable | Rationale |
|---|---|---|
| **P1** | CORS at gateway, fix keycloakId lookup, address ownership, enforce seller identity from JWT | Security hardening before any public exposure |
| **P2** | Inventory reservation/compensation, transactional outbox for Kafka, order state machine | Protect core business transaction consistency |
| **P3** | Flyway/Liquibase migrations, Testcontainers, CI pipeline, API versioning, OpenAPI specs | Engineering baseline for safe iteration |
| **P4** | Container health probes, central secrets manager, pinned image tags, network policies | Production-ready deployment |
| **P5** | Payment provider integration, fulfilment workflows, multi-channel notifications | Commercial completeness |
| **P6** | Product search (Elasticsearch/OpenSearch), variant/SKU model, reviews, seller onboarding | Catalogue depth |
| **P7** | Redis cache-aside, Kubernetes migration, SLOs, chaos testing, load testing | Scale and reliability |

---

## Appendix A: Port Map

| Component | Internal Port | External Port | Notes |
|---|---|---|---|
| Gateway | 8080 | 8080 | Public entry point |
| UserModule | 8082 | 8082 | Users, addresses |
| ProductModule | 8083 | 8083 | Catalogue, categories, stock |
| OrderModule | 8084 | 8084 | Carts, orders, checkout |
| NotificationModule | 8085 | 8085 | Event consumer, email |
| ConfigServer | 8888 | 8888 | Git-backed config |
| EurekaServer | 8761 | 8761 | Discovery UI at `/` |
| Keycloak | 8080 (container) | 8443 | Realm `ecomm-app` |
| Kafka | 9092 (host) / 29092 (internal) | 9092 | KRaft single-node |
| Zipkin | 9411 | 9411 | Tracing UI |
| Prometheus | 9090 | 9090 | Metrics |
| Grafana | 3000 | 3000 | Dashboards |
| Loki | 3100 | 3100 | Log aggregation |

## Appendix B: API Contract Summary

All endpoints accept optional pagination: `pageNum` (default 0), `pageSize` (default 5), `sortBy` (default `id`), `sortDir` (default `asc`). Paginated responses use: `{ listField, pageNum, pageSize, totalElements, totalPages, lastPage }`.

**UserModule** (`/api/user`, `/api/address`):
- `POST /api/user` → PUBLIC → 201 `UserResponseDTO`
- `GET /api/user` → ADMIN → 200 `UserSearchResponseDTO`
- `GET /api/user/{id}` → USER/SELLER/ADMIN → 200 `UserResponseDTO`
- `GET /api/user/keycloak/{keycloakId}` → JWT → 200 `UserResponseDTO`
- `PUT /api/user` → JWT (self) → 201 `UserResponseDTO`
- `DELETE /api/user` → JWT (self) → 204
- `POST /api/address/user` → USER → 201 `Address`
- `GET /api/address/user` → USER → 200 `List<AddressDTO>`
- `PUT /api/address/{id}` → USER → 201
- `DELETE /api/address/{id}` → USER → 204

**ProductModule** (`/api/product`, `/api/category`):
- `GET /api/product` → PUBLIC → 200 `List<ProductResponseDTO>`
- `GET /api/product/{id}` → PUBLIC → 200 `ProductResponseDTO`
- `GET /api/product/search` → PUBLIC → 200 `ProductSearchResponseDTO`
- `GET /api/product/search/{keyword}` → PUBLIC → 200 paged
- `GET /api/product/search/category/{category}` → PUBLIC → 200 paged
- `POST /api/product/batch` → USER/SELLER/ADMIN → 200 `List<ProductResponseDTO>`
- `POST /api/product/{categoryId}` → SELLER → 201 `ProductResponseDTO`
- `PUT /api/product/{id}` → SELLER → 200
- `PUT /api/product/dec/{id}/{qty}` → USER/SELLER/ADMIN → 204
- `PUT /api/product/inc/{id}/{qty}` → USER/SELLER/ADMIN → 204
- `DELETE /api/product/{id}` → SELLER → 204
- `POST /api/category` → ADMIN/SELLER → 201
- `GET /api/category`, `GET /api/category/{id}` → PUBLIC → 200
- `PUT /api/category/{id}` → ADMIN/SELLER → 201
- `DELETE /api/category/{id}` → ADMIN/SELLER → 204

**OrderModule** (`/api/cart`, `/api/order`):
- `POST /api/cart` → USER → 201 `true` / 400 `false`
- `GET /api/cart` → USER → 200 `CartDTO`
- `PUT /api/cart/{productId}/{operation}` → USER → 200 `Boolean`
- `DELETE /api/cart/{productId}` → USER → 204
- `POST /api/order` → USER/SELLER/ADMIN → 201 `OrderResponseDTO`
- `GET /api/order` → USER/SELLER/ADMIN → 200 `OrderSearchResponseDTO`
- `GET /api/order/admin/all` → ADMIN → 200 `OrderSearchResponseDTO`
- `GET /api/order/seller/all` → ADMIN/SELLER → 200 `OrderItemSearchResponseDTO`
- `POST /api/order/cancel/{orderId}` → owner → 202 `Boolean`
- `PUT /api/order/{status}/{orderId}` → USER/SELLER/ADMIN → 200 `Boolean`

**Effective sale price formula:** `price * (1 - discount/100) * quantity` (BigDecimal, HALF_UP to 2dp).
