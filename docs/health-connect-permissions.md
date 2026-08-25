# Health Connect permission model

HealthAgentApp requests read-only access to the MVP data types independently: steps, distance, exercise, heart rate, resting heart rate, sleep, weight and active calories.

The application must never treat the permission set as all-or-nothing. Before each record-type read, synchronization code checks `HealthPermissionManager.canRead(...)`; denied or revoked types are skipped while other granted types remain usable.

`READ_HEALTH_DATA_IN_BACKGROUND` and `READ_HEALTH_DATA_HISTORY` are optional capabilities. They are only requestable when supported by the Android/Health Connect module and are shown separately in the UI.

Permission state is refreshed in `MainActivity.onResume()` because users can change grants outside HealthAgentApp in Health Connect settings.

State semantics:
- `GRANTED`: record type may be read.
- `DENIED`: user has not granted or has revoked access; this is not a zero measurement.
- `UNSUPPORTED`: Health Connect or the optional capability is unavailable.

The manifest intentionally declares no Health Connect write permissions.
