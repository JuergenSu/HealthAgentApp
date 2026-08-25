# Daily health aggregation

`DailyHealthAggregator` converts synchronized Health Connect staging data into the stable `DailyHealthEntity` model consumed by later fitness/recovery logic.

## Calendar days and timezone

A day is defined using the device's configured `ZoneId`. Day boundaries are created with `LocalDate.atStartOfDay(zoneId)`, so daylight-saving transitions are handled by the timezone rules rather than assuming every day is 24 hours.

Sleep and workout intervals are clipped to the local day and overlapping intervals are unioned before minutes are calculated. This prevents overlapping sessions/sources from inflating sleep or exercise duration.

## Metric semantics

- Steps: Health Connect daily aggregate, count.
- Distance: Health Connect daily aggregate converted from meters to kilometres.
- Active calories: Health Connect daily aggregate in the platform Energy calorie unit.
- Sleep: union of synchronized sleep-session intervals overlapping the local calendar day.
- Resting heart rate: arithmetic mean of valid resting-HR records for the day.
- Average heart rate: sample-count-weighted mean of synchronized heart-rate records.
- Weight: latest valid measurement in the local calendar day.
- Exercise minutes: union of completed/imported workout intervals overlapping the day.

Missing source measurements remain `null`; they are never converted to zero.

## Per-metric quality

Every `DailyHealthEntity` metric has its own `DataQuality` field:

- `AVAILABLE`: readable source data exists and passes basic plausibility checks.
- `MISSING`: permission is available but no usable measurement exists for the day.
- `PARTIAL`: the metric cannot be fully evaluated (for example permission is not granted or only part of the underlying records is usable). A partial metric may still contain a value when a valid subset exists.
- `SUSPECT`: a value exists but is outside conservative plausibility bounds. The value is preserved; source/staging data is never silently rewritten.

`DailyHealthEntity.dataQuality` is a summary only. Per-metric quality is authoritative. Any suspect metric makes the summary suspect; mixed available/missing/partial metrics produce a partial summary.

## Determinism

For fixed staging rows, workouts, permissions, timezone and `calculatedAt`, aggregation produces the same result. OpenAI is not involved in this process.

## Synchronization integration

After Health Connect source import succeeds, `HealthSyncService` recalculates all local days touched by the sync window before advancing `lastSuccessfulSyncAt`. Incremental sync uses its existing overlap window, so boundary days are recalculated idempotently.
