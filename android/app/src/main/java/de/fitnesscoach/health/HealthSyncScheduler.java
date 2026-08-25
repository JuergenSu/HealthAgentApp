package de.fitnesscoach.health;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Owns the single periodic Health Connect background job. */
public final class HealthSyncScheduler {
    public static final String PERIODIC_WORK_NAME = "health-connect-periodic-sync";
    static final Duration PERIOD = Duration.ofHours(24);
    static final Duration BACKOFF = Duration.ofMinutes(30);

    private HealthSyncScheduler() {}

    public static void reconcile(Context context) {
        Context app = context.getApplicationContext();
        HealthPermissionSnapshot snapshot = new HealthPermissionManager(app).getSnapshot();
        WorkManager workManager = WorkManager.getInstance(app);

        if (!HealthSyncPolicy.canRunInBackground(snapshot)) {
            workManager.cancelUniqueWork(PERIODIC_WORK_NAME);
            return;
        }

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                HealthSyncWorker.class, PERIOD.toHours(), TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF.toMinutes(), TimeUnit.MINUTES)
                .build();

        workManager.enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request);
    }
}
