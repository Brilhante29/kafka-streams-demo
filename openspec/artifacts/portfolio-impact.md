# Portfolio Impact

This repository is the streaming/stateful-processing proof inside `mlops-data-platform`. It demonstrates Kotlin/JVM interoperability, Kafka stream-table semantics, broker-backed integration, `exactly_once_v2`, canonical reproducible evidence, Docker hardening, SBOM review, and CI security gates.

Its bounded public result is 4,965.35 records/s median with 267.48 ms batch p95 and invariant 1.0 across five local broker-backed samples. Raw values and 13.47% throughput CV prevent the number from being mistaken for an SLA.

Reusable outputs are canonical evidence validation, scanner no-suppression policy, secret-ignore tests, domain/wire DTO guidance, the JVM validator, the Kafka Streams decision record, and continuity protocol. Business topics/events remain project-specific.
