package de.fitnesscoach.health;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/** Periodic background sync. It never reads Health Connect without the background-read grant. */
public class HealthSyncWorker extends Worker {
    public HealthSyncWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        HealthPermissionManager permissionManager = new HealthPermissionManager(getApplicationContext());
        HealthPermissionSnapshot snapshot = permissionManager.getSnapshot();
        if (!HealthSyncPolicy.canRunInBackground(snapshot)) {
            return Result.success();
        }

        HealthSyncResult result = new HealthSyncCoordinator(getApplicationContext()).runSync();
        if (result.isSuccessful()) return Result.success();
        if (HealthSyncPolicy.shouldRetryWorker(result, getRunAttemptCount())) return Result.retry();
        return Result.failure();
    }
}
