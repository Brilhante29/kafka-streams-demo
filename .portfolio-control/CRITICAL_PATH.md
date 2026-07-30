# Critical Path: #28 kafka-streams-demo

```text
CI + structural contracts
  -> implementation commit (clean source)
  -> one release-candidate run
  -> clean broker baseline + scans + SBOM
  -> evidence/docs commit
  -> independent P0/P1 review
  -> push + PR + observed green CI
  -> explicit merge decision
  -> next repository
```

Stop rules:

- Do not repeat the full release candidate without a change that invalidates evidence.
- After two equivalent failures, change strategy and record the cause.
- P2/P3 findings enter backlog; only P0/P1 block publication.
- Checkpoint before a quota warning can interrupt a long operation and at every phase boundary.
