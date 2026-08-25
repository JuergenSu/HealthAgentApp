package de.fitnesscoach.health;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum HealthPermissionSpec {
    STEPS("Steps", "android.permission.health.READ_STEPS", false),
    DISTANCE("Distance", "android.permission.health.READ_DISTANCE", false),
    EXERCISE("Exercise", "android.permission.health.READ_EXERCISE", false),
    HEART_RATE("Heart rate", "android.permission.health.READ_HEART_RATE", false),
    RESTING_HEART_RATE("Resting heart rate", "android.permission.health.READ_RESTING_HEART_RATE", false),
    SLEEP("Sleep", "android.permission.health.READ_SLEEP", false),
    WEIGHT("Weight", "android.permission.health.READ_WEIGHT", false),
    ACTIVE_CALORIES("Active calories", "android.permission.health.READ_ACTIVE_CALORIES_BURNED", false),
    BACKGROUND_READ("Background read", "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND", true),
    HISTORY_READ("Full history", "android.permission.health.READ_HEALTH_DATA_HISTORY", true);

    private final String displayName;
    private final String permission;
    private final boolean optional;

    HealthPermissionSpec(String displayName, String permission, boolean optional) {
        this.displayName = displayName;
        this.permission = permission;
        this.optional = optional;
    }

    public String getDisplayName() { return displayName; }
    public String getPermission() { return permission; }
    public boolean isOptional() { return optional; }

    public static List<HealthPermissionSpec> all() {
        return Collections.unmodifiableList(Arrays.asList(values()));
    }
}
