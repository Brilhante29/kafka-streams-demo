# ADR-002: Kotlin/JVM, Kafka Streams, and broker semantics

Status: accepted

## Selected stack

- Kotlin 2.4.10 on Java 21.
- Gradle Wrapper 9.3.0 with Kotlin DSL and dependency locking.
- Kafka Streams 4.3.1 and Kotlin serialization 1.11.0.
- JUnit 5, AssertJ, TopologyTestDriver, and Testcontainers Kafka 2.0.5.
- Official Kafka Native 4.3.1 and pinned Temurin JDK/JRE images.

Kotlin was selected for immutable contracts, concise policies, and Java ecosystem interoperability. Spring was rejected because no lifecycle or web capability is required.

## Messaging decision

Kafka is selected because the behavior requires keyed logs, replay, KTable joins, materialized state, changelogs, and partition-aware ordering. RabbitMQ is appropriate for queue routing and acknowledgements, but not for the stateful stream-table model proved here. Synchronous HTTP removes replay and state restoration from the evidence.

Broker mode uses `exactly_once_v2`, idempotent producer settings, `read_committed` output consumption, three partitions, and replication factor one only for local execution. Production requires replicated transaction/offset/changelog topics.

## Protocol and cloud decisions

The interface is CLI/events, not HTTP, GraphQL, or gRPC. Kumo and AWS are deliberately absent because the use case invokes no cloud service. If a future archive or secret use case appears, application-owned ports must keep provider SDKs outside domain code; Kumo remains the first local adapter candidate.

## Libraries

Each library exposes measured behavior. No dependency is added only for architecture appearance. Versions are fixed in `build.gradle.kts` and `gradle.lockfile`. Wrapper distribution and JAR checksums are validated separately.

## SOLID and simplicity

- SRP: domain policy, topology, runtime, benchmark, and evidence validation change independently.
- OCP: new sinks/metrics can be composed without changing event contracts.
- LSP: no fake adapter hierarchy is claimed; any future implementation must preserve ordering, failures, and consistency.
- ISP: interfaces are introduced only when a second consumer needs a smaller contract.
- DIP: domain is independent; infrastructure depends on domain data/policy.
- DRY: topic/store names and fixtures have one source; incidental syntax is not abstracted.
- KISS/YAGNI: one process, one broker for local evidence, no Spring, database, registry, cloud, or microservice split.
- Law of Demeter: policies operate on their direct event/state inputs.

## Revisit triggers

Revisit when there is a second deployable consumer, a schema compatibility requirement, external side effects, multi-cluster replication, or a measured bottleneck that cannot be addressed through Kafka configuration.
