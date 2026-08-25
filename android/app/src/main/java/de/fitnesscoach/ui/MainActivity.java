package de.fitnesscoach.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.contracts.HealthPermissionsRequestContract;

import java.util.Set;

import de.fitnesscoach.R;
import de.fitnesscoach.databinding.ActivityMainBinding;
import de.fitnesscoach.health.HealthPermissionManager;
import de.fitnesscoach.health.HealthPermissionSnapshot;
import de.fitnesscoach.health.HealthPermissionSpec;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HealthPermissionManager healthPermissionManager;
    private ActivityResultLauncher<Set<? extends String>> healthPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        healthPermissionManager = new HealthPermissionManager(this);
        healthPermissionLauncher = registerForActivityResult(
                new HealthPermissionsRequestContract(HealthConnectClient.DEFAULT_PROVIDER_PACKAGE_NAME),
                granted -> refreshHealthPermissionStatus());

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Permissions may have been revoked or granted in Health Connect settings while away.
        refreshHealthPermissionStatus();
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
