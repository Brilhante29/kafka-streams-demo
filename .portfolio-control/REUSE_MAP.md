# Reuse Map: #28 kafka-streams-demo

| Concern | Kit source | Project use |
|---|---|---|
| Benchmark contract | `contracts/benchmark-result-v2.schema.json` | raw samples, provenance, comparability |
| JVM governance | `templates/validate-gradle-project.ps1` | wrapper/checksum/toolchain/CI alignment |
| Architecture decision | `decision-brain/kafka-streams-matrix.yaml` | Kafka vs RabbitMQ and broker/driver separation |
| Agent skills | `.codex/skills/kafka-streams`, `.claude/skills/kafka-streams` | shared implementation/review rules |
| Continuity | `decision-brain/continuity-protocol.yaml` | phase/limit checkpoints and canonical handoff |
| Specification | OpenSpec/AITmpl templates | tool-agnostic artifact graph |

Project-owned: event contracts, topic names, topology, fixtures, and benchmark business invariants.

Potential contribution: generic broker evidence orchestration and dedicated executable JVM JNI tmpfs pattern. It remains `pending_review` until useful beyond this repository.
