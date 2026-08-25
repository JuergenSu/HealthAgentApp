# HealthAgentApp

Local-first Android fitness coach using Health Connect and OpenAI.

The application reads Health Connect data into a local Android data model, calculates fitness/recovery/training-plan state deterministically on-device, and adds a conversational OpenAI coach through a stateless Java/Spring Boot gateway.

## Start here
- [Complete project plan](PROJECT_PLAN.md)
- [Codex implementation rules](AGENTS.md)
- [GitHub Issues](../../issues)

## Core architecture

```text
Health Connect
     |
     v
Android Java App
  Room + Domain Engine
  Baselines / Load / Recovery / Plan / Safety
     |
     | minimal structured CoachContext + tool protocol
     v
Spring Boot Coach Gateway
     |
     v
OpenAI Responses API
```

The LLM explains, converses and requests controlled tools. It does not own health state and cannot bypass local plan/safety rules.

## MVP
The MVP is a running-focused personal coach with onboarding, Health Connect sync, baselines, recovery, training plan, Today/Plan/Progress views, workout reconciliation, daily adaptation, conversational coaching, controlled agent tools, persistent confirmed coach memory, privacy controls and safety validation.

See `PROJECT_PLAN.md` for phases, dependency chains, release criteria and V1/later scope.
