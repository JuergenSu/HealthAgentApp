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

    public Set<String> getRequestablePermissions(boolean includeOptional) {
        HealthPermissionSnapshot snapshot = getSnapshot();
        Set<String> result = new LinkedHashSet<>();
        if (!snapshot.isHealthConnectAvailable()) return result;

        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            if (spec.isOptional() && !includeOptional) continue;
            if (snapshot.getState(spec) != HealthPermissionSnapshot.State.UNSUPPORTED) {
                result.add(spec.getPermission());
            }
        }
        return result;
    }

    /**
     * Called by synchronization code before every read. A denied permission simply disables
     * that record type; it is not treated as a global synchronization failure.
     */
    public boolean canRead(HealthPermissionSpec permission) {
        if (permission.isOptional()) return false;
        return getSnapshot().canRead(permission);
    }
}
