package de.fitnesscoach.health;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

public class HealthSyncPolicyTest {

    @Test
    public void appStartSyncRunsWhenNeverSynchronized() {
        assertTrue(HealthSyncPolicy.shouldSyncOnAppStart(null, Instant.parse("2026-08-25T12:00:00Z")));
    }

    @Test
    public void appStartSyncWaitsUntilSixHoursOld() {
        Instant last = Instant.parse("2026-08-25T10:00:00Z");
        assertFalse(HealthSyncPolicy.shouldSyncOnAppStart(last, Instant.parse("2026-08-25T15:59:59Z")));
        assertTrue(HealthSyncPolicy.shouldSyncOnAppStart(last, Instant.parse("2026-08-25T16:00:00Z")));
    }

    @Test
    public void backgroundRequiresBackgroundGrantAndAtLeastOneHealthReadGrant() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states = deniedStates();
        states.put(HealthPermissionSpec.BACKGROUND_READ, HealthPermissionSnapshot.State.GRANTED);
        HealthPermissionSnapshot noHealthRead = new HealthPermissionSnapshot(true, states);
        assertFalse(HealthSyncPolicy.canRunInBackground(noHealthRead));

        states.put(HealthPermissionSpec.STEPS, HealthPermissionSnapshot.State.GRANTED);
        HealthPermissionSnapshot permitted = new HealthPermissionSnapshot(true, states);
        assertTrue(HealthSyncPolicy.canRunInBackground(permitted));

        states.put(HealthPermissionSpec.BACKGROUND_READ, HealthPermissionSnapshot.State.DENIED);
        HealthPermissionSnapshot backgroundDenied = new HealthPermissionSnapshot(true, states);
        assertFalse(HealthSyncPolicy.canRunInBackground(backgroundDenied));
    }

    @Test
    public void workerRetriesOnlyUntilAttemptLimit() {
        HealthSyncResult failure = new HealthSyncResult(false, Instant.EPOCH, Instant.EPOCH, Map.of(), "failed");
        HealthSyncResult success = new HealthSyncResult(true, Instant.EPOCH, Instant.EPOCH, Map.of(), null);

        assertTrue(HealthSyncPolicy.shouldRetryWorker(failure, 0));
        assertTrue(HealthSyncPolicy.shouldRetryWorker(failure, 1));
        assertFalse(HealthSyncPolicy.shouldRetryWorker(failure, 2));
        assertFalse(HealthSyncPolicy.shouldRetryWorker(success, 0));
    }

    private EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> deniedStates() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states =
                new EnumMap<>(HealthPermissionSpec.class);
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            states.put(spec, HealthPermissionSnapshot.State.DENIED);
        }
        return states;
    }
}
