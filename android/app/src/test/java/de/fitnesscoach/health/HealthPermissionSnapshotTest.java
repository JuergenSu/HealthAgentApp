package de.fitnesscoach.health;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.EnumMap;

public class HealthPermissionSnapshotTest {

    @Test
    public void partialGrantKeepsGrantedRecordReadable() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states = deniedStates();
        states.put(HealthPermissionSpec.STEPS, HealthPermissionSnapshot.State.GRANTED);
        HealthPermissionSnapshot snapshot = new HealthPermissionSnapshot(true, states);

        assertTrue(snapshot.canRead(HealthPermissionSpec.STEPS));
        assertFalse(snapshot.canRead(HealthPermissionSpec.SLEEP));
        assertTrue(snapshot.hasAnyRequiredGrant());
        assertFalse(snapshot.allRequiredGranted());
    }

    @Test
    public void revokedPermissionStopsOnlyThatRecordType() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states = grantedRequiredStates();
        HealthPermissionSnapshot before = new HealthPermissionSnapshot(true, states);
        assertTrue(before.canRead(HealthPermissionSpec.SLEEP));
        assertTrue(before.canRead(HealthPermissionSpec.STEPS));

        states.put(HealthPermissionSpec.SLEEP, HealthPermissionSnapshot.State.DENIED);
        HealthPermissionSnapshot after = new HealthPermissionSnapshot(true, states);
        assertFalse(after.canRead(HealthPermissionSpec.SLEEP));
        assertTrue(after.canRead(HealthPermissionSpec.STEPS));
    }

    @Test
    public void unavailableHealthConnectMakesReadsUnavailable() {
        HealthPermissionSnapshot snapshot = new HealthPermissionSnapshot(false, grantedRequiredStates());
        assertFalse(snapshot.canRead(HealthPermissionSpec.STEPS));
    }

    private EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> deniedStates() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states =
                new EnumMap<>(HealthPermissionSpec.class);
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            states.put(spec, HealthPermissionSnapshot.State.DENIED);
        }
        return states;
    }

    private EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> grantedRequiredStates() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states = deniedStates();
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            if (!spec.isOptional()) states.put(spec, HealthPermissionSnapshot.State.GRANTED);
        }
        return states;
    }
}
