# Architecture Record

Selected: event-driven topology with minimal inward boundaries.

- Domain: immutable contracts and pure policy; no Kafka/framework imports.
- Infrastructure: Serdes, topics, stores, topology, runtime properties.
- Benchmark: fixtures, producer/consumer/store checks, evidence.
- Entrypoint: CLI selection only.

Rejected: MVC/MVVM, microservices, Spring Boot, and a custom wrapper interface around every Kafka DSL type. These shapes do not reduce a requirement-driven coupling here. SOLID is applied at real change boundaries; KISS/YAGNI blocks decorative infrastructure.
