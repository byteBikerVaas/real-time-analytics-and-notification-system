# Real-Time Analytics & Notification System - High Level Design (HLD)

> NOTE: This HLD documents the current scaffolded multi-module Spring Boot project (Java 21, Spring Boot 3.x) and describes what has been implemented, the exact files and signatures, application properties, POM dependencies, Docker Compose setup, and the remaining work. All business logic is intentionally left as TODOs; the codebase contains compile-safe skeletons only.

## Table of Contents
- Overview
- Architecture
- Modules
  - Parent POM (root)
  - `common`
  - `ingest-service`
  - `stream-processor`
  - `websocket-gateway`
- Docker / Local infra
- Build & Run
- Completed Work (file-by-file)
- Remaining Work / Next Steps (detailed)
- Appendix: Key Commands

---

## Overview
This project implements a scaffold for a Real-Time Analytics & Notification System using Java 21 and Spring Boot 3.x. The system is organized as a Maven multi-module project with these conceptual components:

- `common`: shared DTOs, constants and types used across modules.
- `ingest-service`: exposes HTTP endpoints to accept events and publishes them to Kafka.
- `stream-processor`: consumes events from Kafka and performs aggregations/windowing and writes results to Redis/Postgres (scaffolded).
- `websocket-gateway`: subscribes to processed results and pushes updates to connected WebSocket clients.

All modules are intentionally skeletons: method signatures and TODO comments are present, but business logic and integrations are not implemented.

## Architecture
- Transport: Events flow from `ingest-service` (HTTP) -> Kafka (topic `events`) -> `stream-processor` (consumer for aggregation) -> results into Redis/Postgres -> `websocket-gateway` reads results (or subscribes to Kafka) and broadcasts to WebSocket clients.
- Observability: Basic SLF4J / Spring Boot logging across modules.
- Serialization: Jackson JSON; `JsonDeserializer` with `ErrorHandlingDeserializer` wrapper for Kafka consumers.
- Local infra: Docker Compose (Kafka, Zookeeper, Redis, Postgres) for local development.

---

## Modules

**Parent POM (root)**
- File: `pom.xml` (root of the repository)
- Purpose: Multi-module aggregator, defines `java.version` property and dependency management for Spring Boot and common libs.
- Key properties:
  - `java.version`: 21
  - `spring-boot.version`: 3.x (starter used in child modules)
- Modules listed: `common`, `ingest-service`, `stream-processor`, `websocket-gateway` (as they were iteratively added)


**Module: `common`**
- Path: `common/` (Maven module)
- Purpose: Shared DTOs and constants.
- Files:
  - `common/pom.xml`
    - Dependencies: `com.fasterxml.jackson.core:jackson-databind`, `jakarta.validation:jakarta.validation-api` (if used), and other lightweight libs.
  - `src/main/java/com/example/realtime/common/Constants.java`
    - public static final String KAFKA_TOPIC_EVENTS = "events";
    - public static final int KAFKA_PARTITIONS = 3;
  - `src/main/java/com/example/realtime/common/dto/EventDTO.java`
    - Fields: `String eventType`, `String userId`, `Instant timestamp`, `Map<String,Object> metadata`.
    - Getters / setters, default constructor, `toString()`.
  - `src/main/java/com/example/realtime/common/dto/AggregateDTO.java`
    - Fields: `String metricId`, `Instant windowStart`, `Instant windowEnd`, `long count`, `double sum`, `double max`.

Notes: `common` compiled and was installed to the local Maven repository during scaffolding.


**Module: `ingest-service`**
- Path: `ingest-service/`
- Purpose: Provide HTTP endpoints for ingesting events and produce them to Kafka.
- Files:
  - `ingest-service/pom.xml`
    - Dependencies:
      - `org.springframework.boot:spring-boot-starter-web`
      - `org.springframework.kafka:spring-kafka`
      - `com.example.realtime:common:0.1.0-SNAPSHOT` (project module)
  - `src/main/java/com/example/realtime/ingest/IngestServiceApplication.java`
    - Spring Boot `@SpringBootApplication` main class.
  - `src/main/java/com/example/realtime/ingest/controller/EventController.java`
    - REST Controller with:
      - `@PostMapping("/events")` public ResponseEntity<Void> `receiveEvent(@RequestBody EventDTO event)`
        - Logs received event and calls `producerService.sendEvent(event)`
        - Returns `202 Accepted`.
  - `src/main/java/com/example/realtime/ingest/service/KafkaProducerService.java`
    - Skeleton service with `public void sendEvent(EventDTO event)` method.
    - TODO: Inject `KafkaTemplate<String, EventDTO>` and implement send with retries.
  - `src/main/resources/application.yml`
    - server.port: 8080
    - spring.application.name: ingest-service
    - spring.kafka.bootstrap-servers: localhost:9092
    - spring.kafka.producer.key-serializer: StringSerializer
    - spring.kafka.producer.value-serializer: JsonSerializer
    - app.kafka.topic: events
    - logging.level.root: INFO

Status: Builds and runs. The controller logs received events; actual Kafka producer logic left as TODO.


**Module: `stream-processor`**
- Path: `stream-processor/`
- Purpose: Consume events from Kafka, perform windowed aggregations and store intermediate/final results in Redis/Postgres.
- Files:
  - `stream-processor/pom.xml`
    - Dependencies:
      - `org.springframework.boot:spring-boot-starter`
      - `org.springframework.kafka:spring-kafka`
      - `org.springframework.boot:spring-boot-starter-data-redis` (if used)
      - `com.example.realtime:common`
  - `src/main/java/com/example/realtime/processor/StreamProcessorApplication.java`
    - Spring Boot main class.
  - `src/main/java/com/example/realtime/processor/service/KafkaConsumerService.java`
    - `@Service` annotated consumer class
    - `@KafkaListener(topics = "events", groupId = "realtime-analytics-group")`
    - `public void consumeEvents(EventDTO event)`
      - Logs received event
      - TODO: windowing, aggregation, update Redis/Postgres
  - `src/main/resources/application.yml`
    - server.port: 8081
    - spring.kafka.consumer.bootstrap-servers: localhost:9092
    - spring.kafka.consumer.group-id: realtime-analytics-group
    - spring.kafka.consumer.key-deserializer: StringDeserializer
    - spring.kafka.consumer.value-deserializer: ErrorHandlingDeserializer
    - properties:
      - spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
      - spring.json.trusted.packages: "*"
      - spring.json.value.default.type: com.example.realtime.common.dto.EventDTO
    - logging.level.root: INFO

Status: Compiles. Consumer code includes TODOs to implement windowing and persistence.


**Module: `websocket-gateway`**
- Path: `websocket-gateway/`
- Purpose: Expose a WebSocket endpoint, accept client connections and broadcast processed results.
- Files:
  - `websocket-gateway/pom.xml`
    - Dependencies:
      - `org.springframework.boot:spring-boot-starter-websocket`
      - `org.springframework.kafka:spring-kafka`
      - `com.example.realtime:common`
  - `src/main/java/com/example/realtime/gateway/WebsocketGatewayApplication.java`
    - Spring Boot main class.
  - `src/main/java/com/example/realtime/gateway/config/WebSocketConfig.java`
    - Registers handler at `/ws/events` with allowedOrigins="*".
  - `src/main/java/com/example/realtime/gateway/handler/EventWebSocketHandler.java`
    - Extends `TextWebSocketHandler`
    - Manages `ConcurrentHashMap`/`Set` of active sessions
    - `afterConnectionEstablished(WebSocketSession session)` logs and adds session
    - `handleTextMessage(WebSocketSession session, TextMessage message)` logs payload
    - `broadcast(String payload)` iterates sessions and sends `TextMessage(payload)` with TODOs for backpressure/queuing and per-client buffering
  - `src/main/resources/application.yml` (note: this file had formatting issues earlier; canonical contents should be):
    - server.port: 8082
    - spring.application.name: websocket-gateway
    - spring.kafka.consumer.bootstrap-servers: localhost:9092
    - spring.kafka.consumer.group-id: websocket-push-group
    - spring.kafka.consumer.key-deserializer: StringDeserializer
    - spring.kafka.consumer.value-deserializer: ErrorHandlingDeserializer
    - properties:
      - spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
      - spring.json.trusted.packages: "*"
      - spring.json.value.default.type: com.example.realtime.common.dto.EventDTO
    - app.kafka.topic: events
    - logging.level.root: INFO

Status: Compiles. `application.yml` was intermittently malformed during editing but was corrected. The gateway starts but Kafka consumer will only fully initialize when Kafka is reachable on `localhost:9092`.

---

## Docker / Local infra
- Goal: Provide local Kafka, Zookeeper, Redis, Postgres via `docker-compose.yml` for easier development and integration testing.
- Current status: A `docker-compose.yml` skeleton was created and iterated. Confluent `cp-kafka` / `cp-zookeeper` images were used after initial image issues.
- Known issue: `docker compose up` failed on host because ports `2181` and `9092` were already allocated by existing containers. Options:
  - Stop conflicting containers (`docker stop <name>`)
  - Change host port mappings in `docker-compose.yml` (e.g., map 9092->19092) and update `application.yml` accordingly

---

## Build & Run
- Build all modules (from repo root):

  mvn -DskipTests clean package

- Run a single module directly (examples):

  mvn -f ingest-service/pom.xml spring-boot:run
  mvn -f stream-processor/pom.xml spring-boot:run
  mvn -f websocket-gateway/pom.xml spring-boot:run

- Notes:
  - Ensure `common` is installed in local Maven repo (built via `mvn -f common/pom.xml -DskipTests clean install`) before building modules that depend on it.
  - If using Docker Compose, start infra first and verify `localhost:9092` is reachable.

---

## Completed Work (file-by-file)
This list enumerates files currently present in the workspace and their state.

- Root `pom.xml`
  - Multi-module aggregator, `java.version`=21, `modules` include `common`, `ingest-service`, `stream-processor`, `websocket-gateway` (as added during scaffolding).

- `common` module
  - `pom.xml` (declares module and dependencies)
  - `Constants.java` (KAFKA_TOPIC_EVENTS = "events")
  - `EventDTO.java` (POJO with eventType, userId, timestamp, metadata)
  - `AggregateDTO.java` (POJO describing aggregated metrics)

- `ingest-service`
  - `pom.xml` (spring-boot-starter-web, spring-kafka, common dependency)
  - `IngestServiceApplication.java` (main)
  - `EventController.java` (`@PostMapping("/events")`, logs and calls producer)
  - `KafkaProducerService.java` (skeleton with TODOs)
  - `resources/application.yml` (server.port=8080, kafka producer settings)

- `stream-processor`
  - `pom.xml` (spring-kafka, redis starter referenced)
  - `StreamProcessorApplication.java` (main)
  - `KafkaConsumerService.java` (`@KafkaListener` method consuming `EventDTO`, TODOs for aggregation/persistence)
  - `resources/application.yml` (server.port=8081, consumer settings with ErrorHandlingDeserializer and delegate configured)

- `websocket-gateway`
  - `pom.xml` (websocket, spring-kafka, common)
  - `WebsocketGatewayApplication.java` (main)
  - `WebSocketConfig.java` (registers handler at `/ws/events`)
  - `EventWebSocketHandler.java` (manages sessions, broadcast skeleton)
  - `resources/application.yml` (server.port=8082, consumer settings; earlier had duplicate keys and code fences which were corrected)

- `docker-compose.yml` (skeleton): provides Kafka, Zookeeper, Redis, Postgres services. Iteratively updated to use `confluentinc/cp-kafka` and `confluentinc/cp-zookeeper` after `bitnami` image issues.

---

## Remaining Work (Detailed)
All remaining work is intentionally left as TODOs in the code unless otherwise noted.

Priority items (to enable basic E2E manual tests):

1. Kafka infra
   - Resolve Docker port conflicts or point services to an available broker address. Ensure a Kafka broker is available at `localhost:9092`.
   - Create the `events` topic with required partitions and replication (or enable auto.create.topics=true for dev).

2. `ingest-service` producer
   - Implement `KafkaProducerService.sendEvent(EventDTO event)` using `KafkaTemplate<String, EventDTO>`.
   - Add metrics/logging and retry/error handling.
   - Optional: Add request validation and schema enforcement.

3. `stream-processor` aggregation logic
   - Implement windowing (tumbling or sliding) and aggregation logic; consider using Kafka Streams or custom processor.
   - Persist intermediate/final aggregates to Redis for quick lookups, and snapshots to Postgres for long-term storage.
   - Add tests for windowing and correctness.

4. `websocket-gateway` integration
   - Implement a `@KafkaListener` (or Redis pub/sub) that receives aggregated results and calls `EventWebSocketHandler.broadcast(...)`.
   - Implement per-client backpressure and queuing to avoid blocking WebSocket threads.
   - Add authentication/authorization for WebSocket endpoints.

5. Docker Compose enhancements
   - Add healthchecks, proper env variables for Kafka advertised listeners for macOS/Linux differences.
   - Provide a `wait-for` script to ensure Kafka is fully ready before starting consumers.

6. Observability & Testing
   - Add Prometheus metrics and basic Grafana dashboards (optional).
   - Add unit and integration tests for each module. Use Testcontainers for Kafka in CI.

7. Documentation
   - Expand `docs/HLD.md` (this document) with sequence diagrams and data schemas.
   - Add `README.md` with quick start instructions.

---

## Appendix: Key Commands
- Build all modules:

```
mvn -DskipTests clean package
```

- Run a single module:

```
mvn -f ingest-service/pom.xml spring-boot:run
```

- Start Docker infra (after fixing port issues):

```
docker compose up -d
```

---

If you want, I can now:
- Generate the remaining TODOs as issues or tasks.
- Implement one of the remaining items (e.g., implement `KafkaProducerService.sendEvent` as a scaffold with `KafkaTemplate` injection but still no actual send), or
- Fix the gateway `application.yml` (it was malformed earlier; I can make sure it's valid now and re-run the gateway).

Tell me which next step you'd like me to take.
