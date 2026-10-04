# Agent Context Card

Project: `#28 kafka-streams-demo`
Current phase: release-candidate preparation
Canonical handoff: `.portfolio-control/CURRENT_HANDOFF.md`

## Read order

1. `AGENTS.md`
2. `.portfolio-control/CURRENT_HANDOFF.md`
3. `project.yaml`
4. `sdd/spec.md`
5. `sdd/architecture-decision.md`
6. `sdd/technical-decision.md`
7. `sdd/benchmark-plan.md`
8. `reuse.manifest.yaml`

## Non-negotiable rules

- Finish this repository before starting another.
- Preserve dirty work and inspect Git state before edits.
- Keep real-broker and TopologyTestDriver evidence separate.
- Do not expose secrets or private chain-of-thought; record facts, decisions, alternatives, commands, and evidence.
- Checkpoint at each phase boundary and immediately on a quota/limit warning.
- Do not merge without explicit authorization.

## Current stack

Kotlin 2.4.10, Java 21, Gradle Wrapper 9.3.0, Kafka Streams 4.3.1, Testcontainers 2.0.5, Kafka Native 4.3.1, Docker Compose.
