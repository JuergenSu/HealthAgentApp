# Backlog Map

The authoritative work items are GitHub Issues. This file provides a stable map for humans and coding agents.

| Phase | Epic | Implementation issues | Outcome |
|---|---|---|---|
| 0 | #1 | #2, #3, #27 | Buildable Android/gateway and local persistence |
| 1 | #4 | #5, #6, #7, #8 | Reliable Health Connect ingestion |
| 2 | #9 | #10, #11, #12 | Profile, availability and primary goal |
| 3 | #13 | #14, #15, #16, #17 | Deterministic baselines/load/recovery |
| 4 | #18 | #19, #20, #21, #22 | Training plan and adaptation |
| 5 | #23 | #24, #25, #42, #43 | Daily/offline product experience |
| 6 | #26 | #28, #29, #30, #31, #32, #33 | Conversational OpenAI coach |
| 7 | #34/#37 | #35, #38, #39, #40 | Memory, safety and privacy |
| 8 | #41 | #44 | MVP release hardening |
| V1 | — | #36, #45, #46, #47 | Post-MVP capabilities |
| Later | — | #48 | Explicitly deferred scope |

## Recommended implementation order

This order minimizes rework while still allowing parallel Android/gateway development.

### Stream A – Android/domain critical path
1. #2 Android skeleton
2. #3 Room/domain model
3. #5 Health Connect permissions
4. #6 Health synchronization
5. #8 Daily aggregation/data quality
6. #11 Profile/availability
7. #12 Primary goal
8. #14 Baselines
9. #15 Training load
10. #16 Daily check-in
11. #17 Recovery
12. #19 Initial training plan
13. #21 Workout matching
14. #22 Plan adaptation
15. #24 Today screen
16. #25 Post-workout review
17. #20 Plan UI
18. #42 Progress
19. #7 Background/manual sync polish
20. #43 Notifications

### Stream B – Gateway/agent
Can start after foundation and converge after the local domain APIs exist.
1. #27 Gateway skeleton
2. #28 OpenAI Responses integration
3. #29 CoachContextBuilder
4. #30 Structured agent contracts
5. #31 Read-only tools
6. #32 Chat UI/context
7. #33 Validated write tools
8. #35 Confirmed CoachMemory

### Stream C – Safety/release
1. #38 SafetyValidator
2. #39 Medical-boundary handling
3. #40 Privacy/data controls
4. #44 End-to-end acceptance and hardening

## Parallelization guidance
- #27 can be developed in parallel with early Android work.
- #10/#11 can proceed once #3 exists; #10 also consumes Health Connect permission work.
- #14, #15 and #16 are largely parallel after their dependencies.
- #20 can be implemented in parallel with workout matching once #19 exists.
- #28 can proceed with mock contracts before Android coach context is complete.
- Do not start #33 as a real write path before #22 and structured contracts are stable.
- #44 is the convergence gate, not a substitute for unit tests in earlier tickets.

## Priority convention
- **P0:** architectural/critical-path capability required for MVP to function.
- **P1:** MVP capability required for release but not always on the deepest critical path.
- **P2:** polish or lower-risk release work.
- **V1/Later:** not part of MVP release gate.

## Codex-ready convention
`codex-ready` means the issue has sufficient scope and acceptance criteria to be implemented independently once its declared dependencies exist in code. An issue being labeled ready does not mean its dependencies are already complete.
