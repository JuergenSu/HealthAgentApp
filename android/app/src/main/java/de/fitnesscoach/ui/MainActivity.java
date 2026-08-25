package de.fitnesscoach.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.contracts.HealthPermissionsRequestContract;
import androidx.lifecycle.ViewModelProvider;

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
import de.fitnesscoach.ui.today.TodayDiagnosticsUiState;
import de.fitnesscoach.ui.today.TodayDiagnosticsViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HealthPermissionManager healthPermissionManager;
    private ActivityResultLauncher<Set<? extends String>> healthPermissionLauncher;
    private TodayDiagnosticsViewModel todayDiagnosticsViewModel;
    private final ExecutorService healthExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        healthPermissionManager = new HealthPermissionManager(this);
        todayDiagnosticsViewModel = new ViewModelProvider(this).get(TodayDiagnosticsViewModel.class);
        todayDiagnosticsViewModel.getState().observe(this, this::renderTodayDiagnostics);
        binding.todayPreviousDay.setOnClickListener(v -> todayDiagnosticsViewModel.previousDay());
        binding.todayNextDay.setOnClickListener(v -> todayDiagnosticsViewModel.nextDay());

        healthPermissionLauncher = registerForActivityResult(
                new HealthPermissionsRequestContract(HealthConnectClient.DEFAULT_PROVIDER_PACKAGE_NAME),
                granted -> {
                    refreshHealthPermissionStatus();
                    triggerInitialSyncIfNeeded();
                    todayDiagnosticsViewModel.refresh();
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
        todayDiagnosticsViewModel.refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHealthPermissionStatus();
        refreshSyncStatus();
        if (todayDiagnosticsViewModel != null) todayDiagnosticsViewModel.refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        healthExecutor.shutdownNow();
    }

    private void showDestination(int itemId) {
        binding.healthPermissionsPanel.setVisibility(itemId == R.id.nav_profile ? View.VISIBLE : View.GONE);
        binding.todayDiagnosticsPanel.setVisibility(itemId == R.id.nav_today ? View.VISIBLE : View.GONE);
        if (itemId == R.id.nav_today) {
            setPlaceholder(R.string.nav_today, R.string.placeholder_today);
            todayDiagnosticsViewModel.refresh();
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

    private void renderTodayDiagnostics(TodayDiagnosticsUiState state) {
        if (binding == null || state == null) return;
        binding.todaySelectedDate.setText(state.date.toString());
        binding.todayNextDay.setEnabled(state.date.isBefore(java.time.LocalDate.now()));
        binding.todayHealthConnectStatus.setText(state.healthConnectAvailable
                ? "Health Connect: available"
                : "Health Connect: unavailable");

        StringBuilder sync = new StringBuilder();
        sync.append("Last attempt: ").append(formatInstantOrNever(state.lastAttemptAt)).append('\n');
        sync.append("Last success: ").append(formatInstantOrNever(state.lastSuccessfulSyncAt)).append('\n');
        sync.append("Aggregated data for selected day: ").append(state.hasDailyData ? "yes" : "no");
        if (state.lastSyncError != null && !state.lastSyncError.isBlank()) {
            sync.append("\nLast error: ").append(state.lastSyncError);
        }
        binding.todaySyncDiagnostics.setText(sync.toString());

        binding.todayEmptyState.setVisibility(state.hasDailyData ? View.GONE : View.VISIBLE);
        binding.todayEmptyState.setText(state.emptyMessage == null ? "" : state.emptyMessage);

        StringBuilder metrics = new StringBuilder();
        for (TodayDiagnosticsUiState.MetricRow row : state.metrics) {
            metrics.append(row.label).append("\n")
                    .append("  Value: ").append(row.value).append('\n')
                    .append("  Quality: ").append(row.quality).append('\n')
                    .append("  ").append(row.explanation).append("\n\n");
        }
        binding.todayHealthMetrics.setText(metrics.toString().trim());
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
                runOnUiThread(() -> {
                    binding.healthSyncStatus.setText(
                            result.isSuccessful() ? formatLastSync(result.getCompletedAt()) : getString(R.string.health_sync_failed));
                    todayDiagnosticsViewModel.refresh();
                });
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
        return getString(R.string.health_sync_last_success, formatInstant(instant));
    }

    private String formatInstantOrNever(java.time.Instant instant) {
        return instant == null ? "never" : formatInstant(instant);
    }

    private String formatInstant(java.time.Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
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
