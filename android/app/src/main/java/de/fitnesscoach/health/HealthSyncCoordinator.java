package de.fitnesscoach.health;

import android.content.Context;

/** Shared entry point used by foreground/manual sync and WorkManager background sync. */
public class HealthSyncCoordinator {
    private final Context appContext;

    public HealthSyncCoordinator(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public HealthSyncResult runSync() {
        return new HealthSyncService(appContext).sync();
    }
}
