package de.fitnesscoach.health;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class HealthPermissionSnapshot {
    public enum State { GRANTED, DENIED, UNSUPPORTED }

    private final boolean healthConnectAvailable;
    private final EnumMap<HealthPermissionSpec, State> states;

    public HealthPermissionSnapshot(boolean healthConnectAvailable,
                                    Map<HealthPermissionSpec, State> states) {
        this.healthConnectAvailable = healthConnectAvailable;
        this.states = new EnumMap<>(HealthPermissionSpec.class);
        this.states.putAll(states);
    }

    public boolean isHealthConnectAvailable() { return healthConnectAvailable; }

    public State getState(HealthPermissionSpec permission) {
        State state = states.get(permission);
        return state == null ? State.UNSUPPORTED : state;
    }

    public boolean isGranted(HealthPermissionSpec permission) {
        return getState(permission) == State.GRANTED;
    }

    public boolean canRead(HealthPermissionSpec permission) {
        return healthConnectAvailable && !permission.isOptional() && isGranted(permission);
    }

    public Map<HealthPermissionSpec, State> getStates() {
        return Collections.unmodifiableMap(states);
    }

    public boolean hasAnyRequiredGrant() {
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            if (!spec.isOptional() && isGranted(spec)) return true;
        }
        return false;
    }

    public boolean allRequiredGranted() {
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            if (!spec.isOptional() && !isGranted(spec)) return false;
        }
        return true;
    }
}
