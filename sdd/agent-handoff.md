# Agent Handoff

Project: `28 - kafka-streams-demo`

## Principal Agent Summary

- Objective: provar processamento streaming com enriquecimento e agregaÃ§Ã£o.
- Portfolio program: `mlops-data-platform`.
- Public proof claim: `messages_per_second` em topologia determinÃ­stica.
- Primary benchmark: `1000` mensagens, seed `42`.
- Default runnable path: `gradle run --args="benchmark 1000 benchmarks/results/latest.json"` ou Docker equivalente.

## Decisions

| Role | Decision | Evidence Path | Status |
|---|---|---|---|
| program-planner | MLOps/data streaming | `project.yaml`, `sdd/spec.md` | complete |
| architecture-selector | event-driven + hexagonal edge | `sdd/architecture-decision.md` | complete |
| engineering-principles-reviewer | SOLID/KISS/YAGNI documented | `sdd/technical-decision.md` | complete |
| stack-decision-agent | Kotlin/JVM/Gradle | `build.gradle.kts` | complete |
| api-style-agent | CLI | `src/main/kotlin/com/portfolio/streaming/App.kt` | complete |
| cloud-local-first-agent | no cloud dependency; Kumo future adapter | `sdd/technical-decision.md` | complete |
| messaging-agent | Kafka Streams, broker-free default | `sdd/spec.md` | complete |
| language-profile-agent | Kotlin + Java 21 | `project.yaml` | complete |
| benchmark-harness-agent | fixed fixture and JSON | `sdd/benchmark-plan.md` | complete |
| design-system-agent | topology diagram and README structure | `docs/topology.md` | complete |
| security-reuse-reviewer | no credentials/default secrets | `README.md`, `REFERENCES.md` | complete |
| release-ci-publisher | CI/Docker ready; no push | `.github/workflows/ci.yml` | complete |

## Local-First Runtime

- Docker command: `docker build -t kafka-streams-demo . && docker run --rm kafka-streams-demo`
- Local services: none on default path.
- Kumo services: none; no cloud behavior is required.
- Real target: Kafka-compatible infrastructure via `run` and `KAFKA_BOOTSTRAP_SERVERS`.
- Default path requires paid secret: no.

## Architecture Boundaries

- Domain: serializable event contracts and pure enrichment/summary policy.
- Use case: topology composition is the streaming application boundary.
- Ports/adapters: `TopologyFactory` and `KafkaStreams` runtime edge; driver is a deterministic test adapter.
- Direction: infrastructure depends on domain; domain never imports Kafka or cloud.

## Benchmark Handoff

- Metric: `messages_per_second`.
- Unit: `messages/s`.
- Higher is better for throughput; lower is better for latency.
- Result path: `benchmarks/results/baseline.json`.
- Fixture: seed `42`, fixed profile list and generated purchase batch.

## Open Risks

- A numeric baseline still needs a Java/Docker execution in an environment where those tools are available.
- Real deployment must decide security, partitions, retention, EOS/retry and schema evolution.

## Publication Gates

- [x] Docker path is defined.
- [x] Benchmark command and JSON schema are defined.
- [x] README starts with number and claim.
- [x] References are documented.
- [x] No secret is needed in default path.
- [ ] Numeric benchmark evidence updated after execution.
