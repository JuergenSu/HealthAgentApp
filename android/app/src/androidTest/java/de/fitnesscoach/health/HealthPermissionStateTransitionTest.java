package de.fitnesscoach.health;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.EnumMap;

@RunWith(AndroidJUnit4.class)
public class HealthPermissionStateTransitionTest {

    @Test
    public void grantAndRevokeAreRepresentedWithoutGlobalFailure() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states =
                new EnumMap<>(HealthPermissionSpec.class);
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            states.put(spec, spec.isOptional()
                    ? HealthPermissionSnapshot.State.UNSUPPORTED
                    : HealthPermissionSnapshot.State.DENIED);
        }

        states.put(HealthPermissionSpec.STEPS, HealthPermissionSnapshot.State.GRANTED);
        states.put(HealthPermissionSpec.HEART_RATE, HealthPermissionSnapshot.State.GRANTED);
        HealthPermissionSnapshot partial = new HealthPermissionSnapshot(true, states);
        assertTrue(partial.canRead(HealthPermissionSpec.STEPS));
        assertTrue(partial.canRead(HealthPermissionSpec.HEART_RATE));
        assertFalse(partial.canRead(HealthPermissionSpec.SLEEP));

        states.put(HealthPermissionSpec.HEART_RATE, HealthPermissionSnapshot.State.DENIED);
        HealthPermissionSnapshot revoked = new HealthPermissionSnapshot(true, states);
        assertTrue(revoked.canRead(HealthPermissionSpec.STEPS));
        assertFalse(revoked.canRead(HealthPermissionSpec.HEART_RATE));
    }
}
