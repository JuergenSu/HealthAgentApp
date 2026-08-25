package de.fitnesscoach.health;

import java.time.Duration;
import java.time.Instant;

/** Pure policy for foreground staleness, background eligibility and bounded retries. */
public final class HealthSyncPolicy {
    public static final Duration APP_START_STALE_AFTER = Duration.ofHours(6);
    public static final int MAX_WORKER_ATTEMPTS = 3;

    private HealthSyncPolicy() {}

    public static boolean shouldSyncOnAppStart(Instant lastSuccessfulSyncAt, Instant now) {
        if (now == null) throw new IllegalArgumentException("now must not be null");
        return lastSuccessfulSyncAt == null
                || !lastSuccessfulSyncAt.plus(APP_START_STALE_AFTER).isAfter(now);
    }

    public static boolean canRunInBackground(HealthPermissionSnapshot snapshot) {
        return snapshot != null
                && snapshot.isHealthConnectAvailable()
                && snapshot.getState(HealthPermissionSpec.BACKGROUND_READ)
                == HealthPermissionSnapshot.State.GRANTED
                && snapshot.hasAnyRequiredGrant();
    }

    public static boolean shouldRetryWorker(HealthSyncResult result, int runAttemptCount) {
        return result != null && !result.isSuccessful() && runAttemptCount + 1 < MAX_WORKER_ATTEMPTS;
    }
}
