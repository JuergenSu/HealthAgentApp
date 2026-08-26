# HealthAgent coach contract v1

The Android app is the authority for health data and all plan mutations. The PHP gateway is stateless: it validates the allow-listed request, calls the OpenAI Responses API and returns either a final answer or one validated tool call. It never reads Room/Health Connect directly and never persists health payloads.

## POST `/api/v1/coach`

Request fields: `conversationId`, `message`, `context`, `conversation`, `toolResults`.

`context` allows only: `profile`, `goal`, `today`, `recentTraining`, `baselines`, `memories`. Missing measurements must carry explicit availability and are not zero. Conversation is bounded to 12 entries by the gateway; Android sends at most 8 previous messages. Tool results are structured `{id,name,result}` objects produced locally by Android.

## Responses

FINAL:
```json
{"type":"FINAL","message":"...","suggestedActions":[],"memoryCandidate":null}
```

TOOL_CALL:
```json
{"type":"TOOL_CALL","toolCall":{"id":"call_1","name":"get_today_status","arguments":{}}}
```

Natural-language output is never parsed to infer an action. Unknown tool names or invalid arguments fail closed.

## Read tools

- `get_today_status()`
- `get_health_summary(days?: 1..90)`
- `get_training_plan(weekStart?: YYYY-MM-DD)`
- `get_workout_history(days?: 1..90)`
- `get_active_goal()`
- `get_baseline(metric, windowDays)` where metric is `sleepMinutes|restingHeartRate|steps|distanceKm|trainingLoad` and window is `7|28|90`
- `get_progress(days?: 7..90)`

## Write tools

- `modify_workout(workoutId, durationMinutes?, workoutType?)`
- `reschedule_workout(workoutId, date)`
- `skip_workout(workoutId, reason?)`
- `record_checkin(date, energy?, muscleFatigue?, motivation?, stress?)`

Write tools are requests only. Android validates workout existence/status, configured availability, duration limits and neighbouring intensive workouts before persistence. Plan changes create a `CoachDecision` audit record. A syntactically valid request can therefore still return a structured local rejection.

## Privacy boundary

The default context excludes the Room database as a whole, raw heart-rate time series, raw Health Connect records and unlimited chat history. The Android debug output lists only allow-list section names; it does not dump the health payload.

Shared fixtures are in `docs/agent-contract-fixtures/` and are consumed by PHP and Android contract tests.
