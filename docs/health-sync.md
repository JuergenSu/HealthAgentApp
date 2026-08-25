# Health Connect synchronization

`HealthSyncService` is the single synchronization implementation for the MVP. It is deliberately blocking and must be invoked from a background thread; scheduled/manual orchestration is added by issue #7.

## Import model

- Steps, distance and active calories are read through Health Connect daily aggregation APIs. This lets Health Connect reconcile overlapping sources instead of the app blindly summing records.
- Heart rate, resting heart rate, sleep and weight are cached as idempotent staging records keyed by `<type>:<Health Connect record id>`.
- Exercise sessions are imported into `WorkoutEntity` using the Health Connect record id as the stable external id.
- Issue #8 consumes this staging data and produces final `DailyHealthEntity` values and data-quality states.

## Initial vs incremental

The initial import requests 30 days by default. If full-history permission is granted, the current MVP requests up to 365 days. Subsequent synchronization starts 24 hours before the previous successful watermark so late updates can be observed; upsert keys make that overlap idempotent.

The successful watermark advances only after every granted record type completes. On failure, existing data remains intact and `lastSuccessfulSyncAt` is not changed.

## Partial permissions

Each record type is checked independently through `HealthPermissionManager`. A denied permission skips that type without failing types that are granted.
