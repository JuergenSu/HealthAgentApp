package de.fitnesscoach.health;

import android.content.Context;
import android.content.pm.PackageManager;
import android.health.connect.HealthConnectManager;
import android.os.Build;
import android.os.ext.SdkExtensions;

import androidx.core.content.ContextCompat;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class HealthPermissionManager {
    static final int OPTIONAL_PERMISSION_EXTENSION_VERSION = 13;

    private final Context context;

    public HealthPermissionManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean isHealthConnectAvailable() {
        return context.getSystemService(HealthConnectManager.class) != null;
    }

    public boolean areOptionalPermissionsSupported() {
        return Build.VERSION.SDK_INT >= 35
                || SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                >= OPTIONAL_PERMISSION_EXTENSION_VERSION;
    }

    public HealthPermissionSnapshot getSnapshot() {
        boolean available = isHealthConnectAvailable();
        boolean optionalSupported = areOptionalPermissionsSupported();
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states =
                new EnumMap<>(HealthPermissionSpec.class);

        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            if (!available || (spec.isOptional() && !optionalSupported)) {
                states.put(spec, HealthPermissionSnapshot.State.UNSUPPORTED);
                continue;
            }
            boolean granted = ContextCompat.checkSelfPermission(context, spec.getPermission())
                    == PackageManager.PERMISSION_GRANTED;
            states.put(spec, granted
                    ? HealthPermissionSnapshot.State.GRANTED
                    : HealthPermissionSnapshot.State.DENIED);
        }
        return new HealthPermissionSnapshot(available, states);
    }

    public Set<String> getMissingRequiredPermissions() {
        return getMissingPermissions(false, false);
    }

    public Set<String> getMissingOptionalPermissions() {
        return getMissingPermissions(true, true);
    }

    private Set<String> getMissingPermissions(boolean includeOptional, boolean optionalOnly) {
        HealthPermissionSnapshot snapshot = getSnapshot();
        Set<String> result = new LinkedHashSet<>();
        if (!snapshot.isHealthConnectAvailable()) return result;

        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            if (optionalOnly && !spec.isOptional()) continue;
            if (!includeOptional && spec.isOptional()) continue;
            if (snapshot.getState(spec) == HealthPermissionSnapshot.State.DENIED) {
                result.add(spec.getPermission());
            }
        }
        return result;
    }

    /**
     * Synchronization must call this before each record-type read. A denied permission only
     * disables that type and never turns a partial grant into a global sync failure.
     */
    public boolean canRead(HealthPermissionSpec permission) {
        if (permission.isOptional()) return false;
        return getSnapshot().canRead(permission);
    }
}
