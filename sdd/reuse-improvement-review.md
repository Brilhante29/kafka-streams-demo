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

| Finding | Classification | Reusable action | State |
|---|---|---|---|
| Evidence could carry a decorative digest. | generic integrity gap | Add canonical payload hashing and cross-language recomputation to the benchmark contract/validator. | proven here; candidate for kit |
| Secret ignore coverage was not an explicit reusable contract. | generic security gap | Supply a language-neutral ignore baseline plus tests that sensitive examples are ignored and `.env.example` is trackable. | proven here; candidate for kit |
| `ignore-unfixed` contradicted the no-suppression policy. | generic policy gap | Validate scanner configuration semantically and require explicit, expiring exception records instead of implicit flags. | proven here; candidate for kit |
| Serialization annotations leaked into domain records. | architecture decision gap | Add a decision rule: domain records stay framework-free; infrastructure-owned wire DTOs map at the boundary when libraries require annotations. | proven here; candidate for kit |
| Broker and topology-driver evidence can be conflated. | benchmark design gap | Keep distinct benchmark IDs, comparability keys, and public claims; extract orchestration only after a second broker consumer. | proven locally; second consumer required |
| RocksDB JNI needs executable temp storage under a hardened runtime. | generic Docker lesson | Document a narrowly scoped executable JNI tmpfs while retaining `/tmp` as `noexec`. | proven here; candidate for kit |
| Runtime-only images can accidentally include benchmark dependencies. | packaging gap | Let reuse profiles separate benchmark tooling from deployable runtime artifacts. | backlog |
| Exact weekly quota is not observable from repository code. | operational constraint | Checkpoint at phase boundaries and immediately on a product usage warning. | resolved |
| Kafka topics, fixtures, and business events are project-owned. | reject reuse | Keep them out of the kit. | resolved |

## Contribution rule

A change returns to the kit only when it is problem-independent, has a validator/test, and does not carry Kafka business semantics. Canonical evidence validation, secret-ignore tests, scanner no-suppression semantics, and domain/wire DTO guidance meet the design bar here; extraction should be committed in the kit only with compatibility tests. Broker orchestration still needs a second consumer.

## Final gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects canonical evidence, scanner policy, architecture boundaries, and continuity checkpoints.
- [x] Existing kit assets were consulted before local assets were added.
- [x] Project-specific code remains project-owned.
- [x] Generic gaps have reproduction evidence and a proposed validator.
- [x] SOLID/DIP and KISS/YAGNI are expressed as enforceable boundaries, not slogans.
- [x] Continuity records facts and decisions without private chain-of-thought.
- [ ] Candidate kit patches are implemented and compatibility-tested in `portfolio-reuse-kit`.
- [ ] Broker harness extraction is proven by a second consumer.
