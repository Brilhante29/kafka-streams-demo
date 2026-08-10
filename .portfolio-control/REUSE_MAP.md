# Reuse Map: #28 kafka-streams-demo

| Concern | Kit source | Project use |
|---|---|---|
| Benchmark contract | `contracts/benchmark-result-v2.schema.json` | raw samples, provenance, comparability, canonical evidence digest |
| JVM governance | `templates/validate-gradle-project.ps1` | wrapper/checksum/toolchain/CI alignment |
| Architecture decision | `decision-brain/kafka-streams-matrix.yaml` | Kafka vs RabbitMQ and broker/driver separation |
| Agent skills | `.codex/skills/kafka-streams`, `.claude/skills/kafka-streams` | shared implementation/review rules |
| Continuity | `decision-brain/continuity-protocol.yaml` | phase/limit checkpoints and canonical handoff |
| Specification | OpenSpec/AITmpl templates | tool-agnostic artifact graph |

Project-owned: event contracts, topic names, topology, fixtures, wire DTOs, and benchmark business invariants.

Kit candidates proven in this project:

- canonical benchmark artifact hashing plus cross-language tamper validation;
- tested secret-ignore baseline that keeps `.env.example` trackable;
- scanner policy validation that rejects implicit suppression;
- domain/wire DTO boundary guidance;
- dedicated executable JNI tmpfs under an otherwise `noexec` runtime.

Generic broker orchestration remains a candidate until a second streaming consumer proves the same boundary without inheriting project semantics.
