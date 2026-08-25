package de.fitnesscoach.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.contracts.HealthPermissionsRequestContract;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import de.fitnesscoach.R;
import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;
import de.fitnesscoach.databinding.ActivityMainBinding;
import de.fitnesscoach.health.HealthPermissionManager;
import de.fitnesscoach.health.HealthPermissionSnapshot;
import de.fitnesscoach.health.HealthPermissionSpec;
import de.fitnesscoach.health.HealthSyncResult;
import de.fitnesscoach.health.HealthSyncService;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HealthPermissionManager healthPermissionManager;
    private ActivityResultLauncher<Set<? extends String>> healthPermissionLauncher;
    private final ExecutorService healthExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        healthPermissionManager = new HealthPermissionManager(this);
        healthPermissionLauncher = registerForActivityResult(
                new HealthPermissionsRequestContract(HealthConnectClient.DEFAULT_PROVIDER_PACKAGE_NAME),
                granted -> {
                    refreshHealthPermissionStatus();
                    triggerInitialSyncIfNeeded();
                });

        binding.requestHealthPermissions.setOnClickListener(v -> {
            Set<String> missing = healthPermissionManager.getMissingRequiredPermissions();
            if (!missing.isEmpty()) healthPermissionLauncher.launch(missing);
        });
        binding.requestOptionalHealthPermissions.setOnClickListener(v -> {
            Set<String> missing = healthPermissionManager.getMissingOptionalPermissions();
            if (!missing.isEmpty()) healthPermissionLauncher.launch(missing);
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            showDestination(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_today);
        }
        triggerInitialSyncIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHealthPermissionStatus();
        refreshSyncStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        healthExecutor.shutdownNow();
    }

    private void showDestination(int itemId) {
        binding.healthPermissionsPanel.setVisibility(itemId == R.id.nav_profile ? View.VISIBLE : View.GONE);
        if (itemId == R.id.nav_today) {
            setPlaceholder(R.string.nav_today, R.string.placeholder_today);
        } else if (itemId == R.id.nav_plan) {
            setPlaceholder(R.string.nav_plan, R.string.placeholder_plan);
        } else if (itemId == R.id.nav_progress) {
            setPlaceholder(R.string.nav_progress, R.string.placeholder_progress);
        } else if (itemId == R.id.nav_coach) {
            setPlaceholder(R.string.nav_coach, R.string.placeholder_coach);
        } else if (itemId == R.id.nav_profile) {
            setPlaceholder(R.string.nav_profile, R.string.placeholder_profile);
            refreshHealthPermissionStatus();
            refreshSyncStatus();
        }
    }

    private void refreshHealthPermissionStatus() {
        if (binding == null || healthPermissionManager == null) return;
        HealthPermissionSnapshot snapshot = healthPermissionManager.getSnapshot();
        if (!snapshot.isHealthConnectAvailable()) {
            binding.healthPermissionsStatus.setText(R.string.health_connect_unavailable);
            binding.requestHealthPermissions.setEnabled(false);
            binding.requestOptionalHealthPermissions.setEnabled(false);
            return;
        }

        StringBuilder text = new StringBuilder("Health Connect\n\n");
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            text.append(statusSymbol(snapshot.getState(spec)))
                    .append(' ')
                    .append(spec.getDisplayName());
            if (spec.isOptional()) text.append(" (optional)");
            text.append('\n');
        }
        binding.healthPermissionsStatus.setText(text.toString().trim());
        binding.requestHealthPermissions.setEnabled(!snapshot.allRequiredGranted());
        binding.requestOptionalHealthPermissions.setEnabled(
                !healthPermissionManager.getMissingOptionalPermissions().isEmpty());
    }

    private void triggerInitialSyncIfNeeded() {
        HealthPermissionSnapshot snapshot = healthPermissionManager.getSnapshot();
        if (!hasAnyRequiredGrant(snapshot) || !syncInProgress.compareAndSet(false, true)) return;

        healthExecutor.execute(() -> {
            try {
                HealthSyncStateEntity state = FitnessCoachDatabase.getInstance(this).healthSyncDao().getState();
                if (state != null && state.lastSuccessfulSyncAt != null) return;
                runOnUiThread(() -> binding.healthSyncStatus.setText(R.string.health_sync_running));
                HealthSyncResult result = new HealthSyncService(this).sync();
                runOnUiThread(() -> binding.healthSyncStatus.setText(
                        result.isSuccessful() ? formatLastSync(result.getCompletedAt()) : getString(R.string.health_sync_failed)));
            } finally {
                syncInProgress.set(false);
            }
        });
    }

    private boolean hasAnyRequiredGrant(HealthPermissionSnapshot snapshot) {
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            if (!spec.isOptional() && snapshot.getState(spec) == HealthPermissionSnapshot.State.GRANTED) return true;
        }
        return false;
    }

    private void refreshSyncStatus() {
        healthExecutor.execute(() -> {
            HealthSyncStateEntity state = FitnessCoachDatabase.getInstance(this).healthSyncDao().getState();
            runOnUiThread(() -> {
                if (state == null || state.lastSuccessfulSyncAt == null) {
                    binding.healthSyncStatus.setText(state != null && state.lastError != null
                            ? R.string.health_sync_failed : R.string.health_sync_never);
                } else {
                    binding.healthSyncStatus.setText(formatLastSync(state.lastSuccessfulSyncAt));
                }
            });
        });
    }

    private String formatLastSync(java.time.Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());
        return getString(R.string.health_sync_last_success, formatter.format(instant));
    }

    private String statusSymbol(HealthPermissionSnapshot.State state) {
        switch (state) {
            case GRANTED: return "✓";
            case DENIED: return "–";
            default: return "·";
        }
    }

    private void setPlaceholder(int titleResource, int bodyResource) {
        binding.screenTitle.setText(titleResource);
        binding.screenBody.setText(bodyResource);
    }
}
