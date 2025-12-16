# Real-Time Analytics and Notification System

A local, Docker-friendly real-time analytics system designed to demonstrate
streaming data processing, rolling window aggregation, backpressure-aware
fan-out, and distributed systems fundamentals.

## High-Level Design
- Event ingestion via REST
- Kafka-based event streaming
- Stateful stream processing with rolling time windows
- Redis for hot state and window buckets
- PostgreSQL for durable aggregate snapshots
- WebSocket gateway for real-time client updates
- Graceful degradation under burst load

## Tech Stack
- Java 21
- Spring Boot 3.x
- Apache Kafka
- Redis
- PostgreSQL
- WebSockets
- Docker Compose

## Project Status
🚧 Work in progress.

Currently implemented:
- Multi-module Maven scaffold
- Common shared DTOs and constants

Planned:
- Ingest service (REST → Kafka)
- Stream processor (Kafka → windowed aggregates)
- WebSocket gateway (real-time fan-out)
- Observability and load testing

This repository is intentionally built incrementally with a strong focus on
correctness, failure handling, and system design rather than raw scale.
