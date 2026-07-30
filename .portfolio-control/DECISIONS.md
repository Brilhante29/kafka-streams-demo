# Decision Register: #28 kafka-streams-demo

| Decision | Selected option | Reason | Revisit trigger |
|---|---|---|---|
| Architecture | event-driven, minimal inward boundaries | stateful keyed streaming is the problem | second deployable boundary appears |
| Language | Kotlin 2.4.10 / Java 21 | concise contracts and JVM/Kafka fit | interoperability or staffing evidence changes |
| Framework | none | no HTTP/DI/persistence lifecycle | a real service lifecycle is required |
| Messaging | Kafka Streams 4.3.1 | KTable join, state, replay, changelogs | problem changes to queue routing only |
| Delivery | exactly-once-v2 in broker mode | output/state consistency inside Kafka | external side effects are added |
| API | CLI/events | no client request/query contract | consumers require HTTP, GraphQL, or gRPC |
| Cloud | none | no cloud behavior; Kumo would be decorative | archive/secrets/cloud event use case appears |
| Storage | Kafka state stores | aggregate is topology-owned keyed state | ad hoc query/access pattern appears |
| Public metric | real broker end-to-end records/s | includes producer, broker, committed output, state | benchmark boundary changes |
| Driver metric | separate microbenchmark | deterministic semantics, no broker claim | never merged with broker evidence |

Principles: SRP and DIP at real boundaries; OCP/ISP/LSP only where contracts exist; DRY removes knowledge duplication; KISS/YAGNI and Law of Demeter prevent speculative infrastructure.
