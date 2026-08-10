# ADR-001: Event-driven topology with minimal inward boundaries

Status: accepted

## Context

The repository proves a Kafka Streams stream-table join, named state stores, and aggregation. Domain complexity is low, integration/state pressure is high, and there is no request/view or independent service-deployment boundary.

## Decision

Use one event-driven Kafka Streams topology. Keep contracts and business policy in `domain`; keep Kafka DSL, Serdes, stores, and runtime properties in `infra`; keep evidence orchestration in `benchmark`.

Dependency direction is `entrypoint/benchmark/infra -> domain`. Domain never imports Kafka or infrastructure types.

## Consequences

- The topology is readable and tested without a broker.
- The same topology runs in Testcontainers and Compose.
- Kafka remains an explicit application dependency because it is the measured capability.
- Infrastructure substitution is limited to where a real alternative exists; no speculative port hierarchy is added.

## Rejected alternatives

| Alternative | Rejection reason |
|---|---|
| MVC/MVVM | No request/view or client-state lifecycle exists. |
| Microservices | One topology has no independent deployment boundary; distribution would add network and consistency failure modes. |
| Full clean/hexagonal wrappers around Kafka DSL | They would hide the topology and duplicate Kafka abstractions without enabling a required second adapter. |
| Spring Boot | No HTTP, DI graph, persistence, or actuator behavior contributes to the claim. |

## Principle check

SRP is visible in package ownership. DIP applies at the domain boundary. OCP is used through topology composition and configuration, not an abstract class for every Kafka type. ISP and LSP are not invoked where no polymorphic port exists; future port implementations must preserve failure and consistency semantics. KISS/YAGNI prevent framework, cloud, database, and service boundaries that the problem does not require.
