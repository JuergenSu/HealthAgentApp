package de.fitnesscoach.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.contracts.HealthPermissionsRequestContract;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.fitnesscoach.R;
import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;
import de.fitnesscoach.databinding.ActivityOnboardingBinding;
import de.fitnesscoach.health.HealthPermissionManager;
import de.fitnesscoach.health.HealthPermissionSnapshot;
import de.fitnesscoach.health.HealthSyncCoordinator;
import de.fitnesscoach.health.HealthSyncResult;
import de.fitnesscoach.ui.MainActivity;

public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private OnboardingStateStore store;
    private HealthPermissionManager healthPermissionManager;
    private ActivityResultLauncher<Set<? extends String>> permissionLauncher;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private OnboardingStep step;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new OnboardingStateStore(this);
        if (store.isCompleted()) {
            openMain();
            return;
        }

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        healthPermissionManager = new HealthPermissionManager(this);
        step = safeStep(store.getStep());

        permissionLauncher = registerForActivityResult(
                new HealthPermissionsRequestContract(HealthConnectClient.DEFAULT_PROVIDER_PACKAGE_NAME),
                granted -> render());

        restoreDraft();
        binding.onboardingBack.setOnClickListener(v -> previous());
        binding.onboardingNext.setOnClickListener(v -> next());
        binding.onboardingExit.setOnClickListener(v -> finishAffinity());
        binding.onboardingHealthPermissions.setOnClickListener(v -> requestHealthPermissions());
        render();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private OnboardingStep safeStep(int ordinal) {
        OnboardingStep[] values = OnboardingStep.values();
        if (ordinal < 0 || ordinal >= values.length) return OnboardingStep.WELCOME;
        return values[ordinal];
    }

    private void next() {
        binding.onboardingError.setVisibility(View.GONE);
        if (!persistAndValidateCurrentStep()) return;
        if (step == OnboardingStep.COMPLETE) {
            store.markCompleted();
            openMain();
            return;
        }
        if (step == OnboardingStep.INITIAL_SYNC) {
            runInitialSync();
            return;
        }
        step = OnboardingStep.values()[Math.min(step.ordinal() + 1, OnboardingStep.COMPLETE.ordinal())];
        store.setStep(step.ordinal());
        render();
    }

    private void previous() {
        if (step.ordinal() == 0) return;
        persistDraftWithoutValidation();
        step = OnboardingStep.values()[step.ordinal() - 1];
        store.setStep(step.ordinal());
        render();
    }

    private boolean persistAndValidateCurrentStep() {
        switch (step) {
            case FITNESS_LEVEL:
                String fitness = selectedFitnessLevel();
                if (!OnboardingValidator.hasFitnessLevel(fitness)) return showError("Choose a fitness level to continue.");
                store.setFitnessLevel(fitness);
                return true;
            case SPORT_PREFERENCES:
                Set<String> sports = selectedSports();
                if (!OnboardingValidator.hasSportPreference(sports)) return showError("Choose at least one activity.");
                store.setSports(sports);
                return true;
            case TRAINING_AVAILABILITY:
                Set<String> days = selectedDays();
                int duration = parsedDuration();
                if (!OnboardingValidator.hasTrainingAvailability(days, duration)) {
                    return showError("Choose at least one training day and enter a duration greater than zero.");
                }
                store.setAvailabilityDays(days);
                store.setDurationMinutes(duration);
                return true;
            case PRIMARY_GOAL:
                String goal = binding.primaryGoal.getText().toString().trim();
                if (!OnboardingValidator.hasPrimaryGoal(goal)) return showError("Describe your primary fitness goal.");
                store.setPrimaryGoal(goal);
                return true;
            default:
                return true;
        }
    }

    private void persistDraftWithoutValidation() {
        if (binding == null) return;
        String fitness = selectedFitnessLevel();
        if (!fitness.isEmpty()) store.setFitnessLevel(fitness);
        Set<String> sports = selectedSports();
        if (!sports.isEmpty()) store.setSports(sports);
        Set<String> days = selectedDays();
        if (!days.isEmpty()) store.setAvailabilityDays(days);
        int duration = parsedDuration();
        if (duration > 0) store.setDurationMinutes(duration);
        String goal = binding.primaryGoal.getText().toString().trim();
        if (!goal.isEmpty()) store.setPrimaryGoal(goal);
    }

    private boolean showError(String message) {
        binding.onboardingError.setText(message);
        binding.onboardingError.setVisibility(View.VISIBLE);
        return false;
    }

    private void requestHealthPermissions() {
        Set<String> missing = healthPermissionManager.getMissingRequiredPermissions();
        if (missing.isEmpty()) {
            render();
        } else {
            permissionLauncher.launch(missing);
        }
    }

    private void runInitialSync() {
        HealthPermissionSnapshot snapshot = healthPermissionManager.getSnapshot();
        if (!snapshot.hasAnyRequiredGrant()) {
            step = OnboardingStep.BASELINE_STATUS;
            store.setStep(step.ordinal());
            render();
            return;
        }

        binding.onboardingNext.setEnabled(false);
        binding.onboardingStatus.setVisibility(View.VISIBLE);
        binding.onboardingStatus.setText("Synchronizing Health Connect…");
        executor.execute(() -> {
            HealthSyncResult result = new HealthSyncCoordinator(this).runSync();
            runOnUiThread(() -> {
                if (binding == null) return;
                binding.onboardingNext.setEnabled(true);
                binding.onboardingStatus.setText(result.isSuccessful()
                        ? "Initial Health Connect synchronization completed."
                        : "Synchronization failed. You can continue and retry later from Today.");
                step = OnboardingStep.BASELINE_STATUS;
                store.setStep(step.ordinal());
                render();
            });
        });
    }

    private void render() {
        if (binding == null) return;
        hideInputs();
        binding.onboardingProgress.setText("Setup " + (step.ordinal() + 1) + " of " + OnboardingStep.values().length);
        binding.onboardingBack.setEnabled(step.ordinal() > 0);
        binding.onboardingNext.setText(step == OnboardingStep.COMPLETE ? "Finish" : "Next");
        binding.onboardingExit.setVisibility(step.ordinal() <= OnboardingStep.HEALTH_CONNECT.ordinal() ? View.VISIBLE : View.GONE);

        switch (step) {
            case WELCOME:
                setText("Welcome", "HealthAgentApp will become a personal running-focused fitness coach using your locally processed Health Connect data.");
                break;
            case COACH_BOUNDARIES:
                setText("Coach boundaries", "The coach can help with training, recovery and sustainable goal progress. It is not a medical diagnostic or treatment service, and deterministic safety rules remain local to the app.");
                break;
            case FITNESS_LEVEL:
                setText("Fitness level", "Choose the level that best describes your current training routine.");
                binding.fitnessLevelGroup.setVisibility(View.VISIBLE);
                break;
            case SPORT_PREFERENCES:
                setText("Sport preferences", "Choose at least one activity. Running is the primary structured coaching domain in the MVP.");
                binding.sportsGroup.setVisibility(View.VISIBLE);
                break;
            case TRAINING_AVAILABILITY:
                setText("Training availability", "Select the days you normally have time and enter a typical maximum session duration.");
                binding.availabilityGroup.setVisibility(View.VISIBLE);
                break;
            case PRIMARY_GOAL:
                setText("Primary goal", "Describe the main result you want the coach to optimize for. Detailed goal rules will be finalized in the goal-management step of the project.");
                binding.primaryGoal.setVisibility(View.VISIBLE);
                break;
            case HEALTH_CONNECT:
                setText("Health Connect", "Grant only the health read permissions you are comfortable sharing. You can also continue without granting access and configure it later.");
                binding.onboardingHealthPermissions.setVisibility(View.VISIBLE);
                binding.onboardingStatus.setVisibility(View.VISIBLE);
                HealthPermissionSnapshot permissions = healthPermissionManager.getSnapshot();
                binding.onboardingStatus.setText(!permissions.isHealthConnectAvailable()
                        ? "Health Connect is unavailable on this device."
                        : permissions.hasAnyRequiredGrant() ? "At least one health data permission is granted." : "No health data permission granted yet.");
                break;
            case INITIAL_SYNC:
                setText("Initial synchronization", "If Health Connect access is available, the app will now import available history and build tester-visible daily aggregates.");
                binding.onboardingStatus.setVisibility(View.VISIBLE);
                HealthSyncStateEntity sync = FitnessCoachDatabase.getInstance(this).healthSyncDao().getState();
                binding.onboardingStatus.setText(sync != null && sync.lastSuccessfulSyncAt != null
                        ? "Health data has already been synchronized successfully. Press Next to continue."
                        : "Press Next to start the initial synchronization.");
                break;
            case BASELINE_STATUS:
                setText("Baseline status", "Personal 7/28/90-day baselines are implemented in a later project step. Health history already imported by this setup will be used when that capability is enabled.");
                break;
            case INITIAL_PLAN:
                setText("Initial training plan", "The deterministic running plan generator is implemented in a later project step. Your onboarding answers are saved so that the planner can use them when available.");
                break;
            case COMPLETE:
                setText("Setup complete", "Your onboarding progress and answers are saved. Finish setup to open Today. Health permissions can be changed later from Profile.");
                break;
        }
    }

    private void setText(String title, String body) {
        binding.onboardingTitle.setText(title);
        binding.onboardingBody.setText(body);
    }

    private void hideInputs() {
        binding.fitnessLevelGroup.setVisibility(View.GONE);
        binding.sportsGroup.setVisibility(View.GONE);
        binding.availabilityGroup.setVisibility(View.GONE);
        binding.primaryGoal.setVisibility(View.GONE);
        binding.onboardingHealthPermissions.setVisibility(View.GONE);
        binding.onboardingStatus.setVisibility(View.GONE);
        binding.onboardingError.setVisibility(View.GONE);
    }

    private void restoreDraft() {
        String fitness = store.getFitnessLevel();
        if ("BEGINNER".equals(fitness)) binding.fitnessBeginner.setChecked(true);
        if ("RECREATIONAL".equals(fitness)) binding.fitnessRecreational.setChecked(true);
        if ("REGULAR".equals(fitness)) binding.fitnessRegular.setChecked(true);
        if ("ADVANCED".equals(fitness)) binding.fitnessAdvanced.setChecked(true);

        Set<String> sports = store.getSports();
        binding.sportRunning.setChecked(sports.contains("RUNNING"));
        binding.sportWalking.setChecked(sports.contains("WALKING"));
        binding.sportCycling.setChecked(sports.contains("CYCLING"));
        binding.sportStrength.setChecked(sports.contains("STRENGTH"));

        Set<String> days = store.getAvailabilityDays();
        setDay(binding.dayMon, days, "MONDAY"); setDay(binding.dayTue, days, "TUESDAY");
        setDay(binding.dayWed, days, "WEDNESDAY"); setDay(binding.dayThu, days, "THURSDAY");
        setDay(binding.dayFri, days, "FRIDAY"); setDay(binding.daySat, days, "SATURDAY");
        setDay(binding.daySun, days, "SUNDAY");
        binding.availabilityDuration.setText(String.valueOf(store.getDurationMinutes()));
        binding.primaryGoal.setText(store.getPrimaryGoal());
    }

    private String selectedFitnessLevel() {
        int id = binding.fitnessLevelGroup.getCheckedRadioButtonId();
        if (id == R.id.fitness_beginner) return "BEGINNER";
        if (id == R.id.fitness_recreational) return "RECREATIONAL";
        if (id == R.id.fitness_regular) return "REGULAR";
        if (id == R.id.fitness_advanced) return "ADVANCED";
        return "";
    }

    private Set<String> selectedSports() {
        Set<String> result = new LinkedHashSet<>();
        if (binding.sportRunning.isChecked()) result.add("RUNNING");
        if (binding.sportWalking.isChecked()) result.add("WALKING");
        if (binding.sportCycling.isChecked()) result.add("CYCLING");
        if (binding.sportStrength.isChecked()) result.add("STRENGTH");
        return result;
    }

    private Set<String> selectedDays() {
        Set<String> result = new LinkedHashSet<>();
        addDay(binding.dayMon, result, "MONDAY"); addDay(binding.dayTue, result, "TUESDAY");
        addDay(binding.dayWed, result, "WEDNESDAY"); addDay(binding.dayThu, result, "THURSDAY");
        addDay(binding.dayFri, result, "FRIDAY"); addDay(binding.daySat, result, "SATURDAY");
        addDay(binding.daySun, result, "SUNDAY");
        return result;
    }

    private int parsedDuration() {
        try { return Integer.parseInt(binding.availabilityDuration.getText().toString().trim()); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private void addDay(CheckBox box, Set<String> set, String value) { if (box.isChecked()) set.add(value); }
    private void setDay(CheckBox box, Set<String> set, String value) { box.setChecked(set.contains(value)); }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
