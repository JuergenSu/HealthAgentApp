# HealthAgentApp – Project Plan

## Product vision
HealthAgentApp is a local-first Android fitness coach for a German-speaking audience. Health and activity data is read from Android Health Connect, normalized and stored locally. Deterministic domain logic calculates baselines, training load, recovery and training-plan adaptations. OpenAI provides the conversational coaching layer through a stateless PHP/Symfony gateway, but is not the source of truth for health calculations or safety decisions.

## Architecture principles
1. **Local first:** Health-derived state, goals, plans, check-ins, memory and decisions are stored on the Android device.
2. **Deterministic fitness engine:** Baselines, load, recovery, workout matching and safety constraints are calculated locally.
3. **LLM as coach, not database:** OpenAI receives only a minimal structured context and may request explicitly defined tools.
4. **No OpenAI key in Android:** Android calls a stateless PHP gateway; only the gateway owns the API credential.
5. **Missing is not zero:** Missing, partial and suspect measurements remain explicit states throughout the domain model.
6. **Fail closed for plan changes:** Agent-requested writes must pass local domain and safety validation.
7. **Auditable coaching:** Material plan adaptations create CoachDecision records with reason and before/after state.
8. **Explicit memory:** Persistent CoachMemory is user-visible and only stored after confirmation.
9. **German product language:** All user-visible Android copy, tester diagnostics, validation/errors and normal Coach prose are German by default. Internal enum/database/API/tool identifiers may remain English, but presentation layers must map them to German labels. A different language requires a future explicit user language preference.

## Target stack
### Android
- Java
- minSdk 34
- XML layouts + ViewBinding
- MVVM + Repository Pattern
- Room
- WorkManager
- Health Connect SDK
- German string resources as the default presentation language

### Gateway
- PHP 8.3+
- Symfony 7.4 LTS
- Composer
- OpenAI Responses API through an application-owned adapter
- `openai-php/client` may be used as the community-maintained PHP client behind that adapter
- PHPUnit
- Stateless; no health database
- Coach prompt requests German prose by default

## MVP product scope
The first releasable product focuses on structured running coaching. Walking, cycling, strength and other imported exercise can contribute as cross-training/general activity, but full structured multi-sport planning is deferred.

MVP includes:
- Guided onboarding and profile
- One active primary fitness goal
- Health Connect permissions and synchronization
- Steps, distance, exercise, HR/resting HR, sleep, weight and active calories
- Local daily aggregates and data-quality states
- 7/28/90-day personal baselines
- Relative training load
- Subjective daily check-in
- Recovery score/category/confidence
- Conservative running plan
- Planned/actual workout matching
- Deterministic daily plan adaptation
- Today and Plan experiences
- Post-workout RPE/review
- Progress screen
- Secure OpenAI Coach Gateway
- Structured agent contracts and local tools
- Conversational Coach UI
- Confirmed persistent CoachMemory
- Safety/privacy controls
- Notifications
- End-to-end acceptance tests
- German user-facing experience across all MVP flows

## Delivery roadmap

### Phase 0 – Foundation
**Outcome:** Both applications build and the local domain model exists.

Issues: #1, #2, #3, #27

Exit criteria:
- Android launches on API 34+
- PHP gateway starts with the documented Symfony/Composer commands
- Room database and repositories exist
- OpenAI key cannot appear in Android artifacts

### Phase 1 – Health data foundation
**Outcome:** Health Connect data can be imported reliably and represented honestly.

Issues: #4, #5, #6, #7, #8

Exit criteria:
- Partial permissions supported
- Initial and incremental sync are idempotent
- Daily metrics distinguish missing/partial/suspect data
- Scheduled and manual synchronization use the same implementation

### Phase 2 – User context and goals
**Outcome:** The system knows who it is coaching and what the user wants to achieve.

Issues: #9, #10, #11, #12

Exit criteria:
- Onboarding persists across restarts
- Profile and weekly availability are editable
- Exactly one primary active goal exists in MVP

### Phase 3 – Fitness intelligence
**Outcome:** Local deterministic logic can assess the current training situation without OpenAI.

Issues: #13, #14, #15, #16, #17

Exit criteria:
- 7/28/90-day baselines calculated with confidence
- Relative training load calculated
- Daily check-in available
- Recovery calculation is deterministic and handles missing inputs

### Phase 4 – Training plan
**Outcome:** A real plan exists and adapts to the user's state.

Issues: #18, #19, #20, #21, #22

Exit criteria:
- Conservative initial plan generated
- Weekly plan visible
- Imported exercise matched to planned workouts
- Recovery can modify future plan safely
- Every material adaptation is auditable

### Phase 5 – Daily product experience
**Outcome:** The application is useful as a non-AI fitness app before the chat layer is enabled.

Issues: #23, #24, #25, #42, #43, #54

Exit criteria:
- Today screen works offline
- Workout review/RPE updates load
- Progress trends are visible
- Useful opt-in notifications work
- All user-visible Android product copy and tester diagnostics are German

### Phase 6 – Conversational coach
**Outcome:** The user can converse with an agent grounded in local fitness state through the stateless PHP Coach Gateway.

Issues: #26, #27, #28, #29, #30, #31, #32, #33

Exit criteria:
- PHP/Symfony gateway is stateless and has no health database
- OpenAI integration uses a versioned coach prompt
- Minimal allow-listed context is sent
- Responses/tool calls use structured schemas
- Read tools work
- Write tools cannot bypass local validation
- Chat failure never disables local fitness functionality
- Normal Coach prose, quick prompts and user-visible chat errors are German by default

### Phase 7 – Memory, safety and privacy
**Outcome:** Personalization is durable and the product has enforceable boundaries.

Issues: #34, #35, #37, #38, #39, #40

Exit criteria:
- Persistent memories require confirmation
- Unsafe plan actions fail closed
- Acute-risk statements leave normal workout coaching path
- AI can be disabled independently
- Local data can be cleared
- Raw health measurements are excluded from standard logs

### Phase 8 – MVP release hardening
**Outcome:** Complete MVP scenario is reproducible and resilient.

Issues: #41, #44

Exit criteria:
- Clean checkout builds Android and PHP gateway
- Critical domain rules have automated tests
- End-to-end happy path passes
- Defined failure scenarios pass
- Remaining physical-device tests documented
- End-to-end acceptance verifies no unintended English product copy in the default German experience

### Phase 9 – V1
Issues: #36, #45, #46, #47

Scope:
- CoachMemory management UI
- Advanced source/data-quality reconciliation
- Multi-week periodization and adaptive replanning
- Weekly coach review and richer progress interpretation

### Later / explicitly out of MVP
Issue: #48

WearOS, live workout coaching, GPS recording, voice coach, nutrition, direct vendor integrations, race mode, social features, human-trainer access, family accounts and broad structured multi-sport planning are intentionally excluded unless promoted to separately scoped work.

## Critical dependency chain
`#2 → #3 → #5 → #6 → #8 → #14/#15 → #17 → #19 → #22 → #24`

Agent path:
`#27 → #28 → #29 → #30 → #31/#32 → #33 → #38`

Release convergence:
`local fitness path + agent path + privacy/safety + German product language → #44`

## Codex execution policy
A Codex implementation task should normally correspond to exactly one non-epic issue.

Before coding, Codex should:
1. Read this project plan.
2. Read the target issue and all referenced dependencies.
3. Inspect existing implementation and tests; do not assume dependencies are implemented merely because an issue exists.
4. Keep changes inside the issue scope.
5. Add/update automated tests for changed domain behavior.
6. Run the relevant Android/PHP gateway build and tests.
7. Document intentional deviations in the PR.
8. For any user-visible behavior, verify German presentation and map internal English enum/schema values before display.

Codex must not:
- Move deterministic fitness/safety calculations into the LLM.
- Add the OpenAI API key to Android.
- Treat missing Health Connect values as zero.
- Give the model unrestricted database access.
- Parse free-form LLM prose to perform writes.
- Couple Symfony controllers or domain code directly to a community OpenAI client library; use an application adapter.
- Silently change or discard source health data.
- Implement Later/V1 features while solving an MVP ticket unless required by a documented dependency.
- Add new English user-facing Android copy or default English Coach prose.

## Definition of Ready for implementation issues
An issue is ready when:
- Goal and observable outcome are clear.
- Dependencies are identified.
- Required domain behavior is specified.
- Acceptance criteria are testable.
- No unresolved architectural decision blocks implementation.
- User-visible behavior has German wording/label expectations or explicitly inherits the global German product-language rule.

## Definition of Done
For every implementation issue:
- Acceptance criteria satisfied.
- Relevant automated tests added/updated and passing.
- Existing tests remain green.
- No secrets or raw sensitive health data added to source/logs/fixtures.
- Error/missing-data behavior covered where applicable.
- Documentation/contracts updated when public behavior changes.
- Build succeeds from the documented toolchain.
- PR references and closes the issue.
- User-visible Android and Coach output introduced by the issue is German by default; internal English identifiers are not exposed as product labels.

## MVP release acceptance scenario
1. Install app.
2. Complete onboarding and define a primary running goal.
3. Grant a subset or all requested Health Connect permissions.
4. Import history and generate daily aggregates/baselines.
5. Calculate recovery with explicit confidence.
6. Generate a conservative plan respecting availability.
7. Display today's recommendation without an OpenAI call.
8. Ask the Coach about today's training.
9. Coach receives minimal context through the PHP gateway and may use local read tools.
10. User states a constraint such as only 30 minutes available.
11. Coach requests a structured workout change.
12. Local validation accepts/rejects the change; successful changes create an audit record.
13. Imported completed workout is matched to plan and reviewed with optional RPE.
14. Subsequent recovery/progress uses the updated local state.
15. If gateway/OpenAI is unavailable, Today, Plan, Progress and Health synchronization continue to work.
16. All user-visible steps above are presented in German by default.

## Key technical risks
- **Health Connect source duplication:** Prefer platform aggregation where appropriate and maintain explicit data-quality semantics.
- **Sparse health history:** Use confidence states and conservative planning rather than fabricated precision.
- **LLM nondeterminism:** Structured schemas plus local validation; never trust prose as a write command.
- **Sensitive-data leakage:** Allow-list context construction and no raw-health standard logging.
- **PHP client coupling:** `openai-php/client` is community-maintained, so isolate it behind an adapter to keep replacement feasible.
- **Planner complexity:** Keep MVP running-focused and deterministic; periodization belongs to V1.
- **Background restrictions:** WorkManager synchronization is opportunistic and must not be assumed to run at an exact clock time.
- **Language leakage:** Internal English enums/schema/tool names must not leak into user-visible Android/Coach copy.

## Success criteria for MVP
- User receives a useful daily training recommendation based on their own available data.
- Recommendation remains explainable and usable without AI connectivity.
- Plan reacts conservatively to recovery and completed activity.
- Coach can discuss goals, progress and plan using grounded local context.
- Agent actions cannot bypass deterministic safety rules.
- User controls persistent memory and local/AI data use.
- The default product experience is consistently German.
