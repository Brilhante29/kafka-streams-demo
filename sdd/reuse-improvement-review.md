# Reuse Improvement Review

Project: `#28 kafka-streams-demo`
Kit: `portfolio-reuse-kit` 1.2.0

## Consumed assets

- Benchmark result V2 schema and provenance model.
- Gradle Wrapper/JVM validator and Kotlin/JVM decision profile.
- Kafka Streams decision matrix and agent skill.
- Continuity checkpoint protocol shared by Codex and Claude.
- OpenSpec/AITmpl artifact graph and project manifest schema.

See `reuse.manifest.yaml` for file-level traceability.

## Findings

| Finding | Classification | Action | State |
|---|---|---|---|
| Wrapper checksum/toolchain validation is reusable across JVM projects. | reuse | Consume kit validator unchanged. | resolved |
| Broker and topology-driver evidence were previously conflated. | generic gap | Keep separate benchmark IDs/modes and propose a reusable streaming harness after this implementation is stable. | pending review |
| RocksDB JNI requires executable temp storage even when `/tmp` is hardened. | generic Docker lesson | Document dedicated executable tmpfs pattern; do not weaken all `/tmp`. | pending review |
| Exact weekly quota is not observable from repository code. | operational constraint | Checkpoint at phase boundaries and immediately on any usage warning. | resolved |
| Kafka-specific topic fixtures and business event models are project-owned. | reject reuse | Keep them out of the kit. | resolved |

## Improvement rule

A contribution returns to the kit only if another repository can consume it without inheriting Kafka business semantics. The likely reusable unit is the evidence orchestration and hardened JVM temp-storage pattern, not this topology.

## Final gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects broker/driver separation, JVM wrapper integrity, and continuity checkpoints.
- [x] Existing kit assets were consulted before creating local assets.
- [x] Local overrides have explicit reasons.
- [x] Project-specific code was not copied into the kit.
- [x] Concrete generic gaps are recorded.
- [ ] Contribution-back is tested in a second consumer or accepted as a kit patch.
