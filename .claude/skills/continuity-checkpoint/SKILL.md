---
name: continuity-checkpoint
description: Preserve auditable project state before quota, context, interruption, or agent limits, so another AI can continue without repeating work.
---

# Continuity Checkpoint

Use decision-brain/continuity-protocol.yaml.

## Trigger

Run this skill when the user reports quota pressure, before a long-running operation, after two repeated failures, before context compaction, before switching repositories, and after a benchmark, CI, PR, merge, or blocker changes. Exact account quota is not observable, so milestone checkpoints are mandatory even without a warning.

## Procedure

1. Finish the smallest safe unit already in progress.
2. Run tools/capture-continuity-state.ps1 for every active repository worktree.
3. Update .portfolio-control/CURRENT_HANDOFF.md with facts, decisions, rejected alternatives, validation, blockers, dirty files, and strict next actions.
4. Verify the handoff contains no secret and no claim unsupported by a current artifact.
5. Run git diff --check and the affected validator.
6. Stop spawning work until the checkpoint is readable by a new agent.

## Output Contract

Do not expose private chain-of-thought. Preserve the useful engineering trace:

- what was observed
- why a decision was selected
- which alternatives were rejected and why
- what changed
- what was verified
- what remains
- the exact next command

Keep one canonical handoff. Do not create competing personal notes.