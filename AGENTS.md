# AGENTS.md

## Project
HealthAgentApp is a local-first Android fitness coach with a PHP/Symfony OpenAI gateway. Read `PROJECT_PLAN.md` before implementing issues.

## Non-negotiable architecture
- Android application code is Java, minSdk 34, XML + ViewBinding.
- Use MVVM + Repository Pattern.
- Room is the local source of persisted application state.
- Health Connect access is read-only for MVP.
- Missing health measurements are nullable/explicitly unavailable; never convert missing data to zero.
- Baselines, training load, recovery, workout matching, plan generation/adaptation and safety validation are deterministic local domain logic.
- OpenAI is a conversational/explanation layer and may request structured tools; it is not the source of truth for calculations.
- Android must never contain the OpenAI API key.
- The gateway is PHP 8.3+ with Symfony 7.4 LTS and Composer.
- The PHP gateway is stateless and must not become a remote health database.
- OpenAI access in PHP must be isolated behind an application-owned adapter; do not couple controllers/domain code directly to a community client library.
- LLM write requests must be structured and pass local domain/safety validation before persistence.
- Do not give the LLM SQL/Room access.
- Do not parse free-form prose to infer write operations.
- **German is the default product language. All user-visible Android UI, validation/error copy, tester diagnostics and normal Coach prose must be German. Internal enum/database/API/tool identifiers may remain English but must be mapped to German presentation labels.**
- Static Android UI copy should live in string resources instead of hard-coded English literals where practical.
- The Coach system prompt must request German responses unless a future explicit user language preference selects another language.

## Working an issue
1. Read the issue, its parent epic, `PROJECT_PLAN.md`, and referenced dependency issues.
2. Inspect the current repository before choosing implementation details.
3. Implement only the requested scope and prerequisites genuinely missing from the codebase.
4. Prefer small domain interfaces around algorithms so they remain independently testable.
5. Add tests for success, missing-data and relevant failure cases.
6. Run affected Android and/or PHP gateway builds/tests before completing the work.
7. In the PR, state what changed, tests run, assumptions and any deviation from the issue.
8. For any user-visible feature, verify German presentation and avoid exposing raw English enum values.

## Testing priorities
Highest-value tests are deterministic domain tests for:
- Health data aggregation/data quality
- Baselines
- Training load
- Recovery score/confidence
- Training-plan generation
- Workout matching
- Plan adaptation
- SafetyValidator
- Agent schema/tool validation
- German presentation mapping for user-visible states

For the PHP gateway use PHPUnit and contract fixtures. Do not make live OpenAI calls in the default test suite.

## Privacy/security
- Never commit secrets.
- Do not log raw health measurements in normal application/gateway logs.
- Send only allow-listed summarized context to the gateway/OpenAI.
- Test fixtures must use synthetic health data.

## Scope discipline
MVP is running-focused. Features listed under Later in `PROJECT_PLAN.md` are not opportunistic additions to MVP issues.
