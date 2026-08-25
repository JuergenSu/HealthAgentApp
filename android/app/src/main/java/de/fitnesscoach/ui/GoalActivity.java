package de.fitnesscoach.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.GoalEntity;
import de.fitnesscoach.data.repository.GoalRepository;
import de.fitnesscoach.databinding.ActivityGoalBinding;

public class GoalActivity extends AppCompatActivity {
    private ActivityGoalBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private GoalRepository repository;
    private GoalEntity activeGoal;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new GoalRepository(FitnessCoachDatabase.getInstance(this));
        binding.goalType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, DomainEnums.GoalType.values()));
        binding.goalSave.setOnClickListener(v -> saveWithConfirmation());
        binding.goalAchieved.setOnClickListener(v -> changeStatus(DomainEnums.GoalStatus.ACHIEVED));
        binding.goalPause.setOnClickListener(v -> changeStatus(DomainEnums.GoalStatus.PAUSED));
        binding.goalCancel.setOnClickListener(v -> changeStatus(DomainEnums.GoalStatus.CANCELLED));
        load();
    }

    private void load() {
        executor.execute(() -> { activeGoal = repository.getActive(); runOnUiThread(this::render); });
    }

    private void render() {
        if (activeGoal == null) {
            binding.goalStatus.setText("No active primary goal");
            binding.goalStatusActions.setVisibility(View.GONE);
            return;
        }
        binding.goalType.setSelection(activeGoal.type == null ? 0 : activeGoal.type.ordinal());
        binding.goalTitle.setText(n(activeGoal.title));
        binding.goalTargetDate.setText(activeGoal.targetDate == null ? "" : activeGoal.targetDate.toString());
        binding.goalTargetValue.setText(activeGoal.targetValue == null ? "" : String.valueOf(activeGoal.targetValue));
        binding.goalTargetUnit.setText(n(activeGoal.targetUnit));
        binding.goalStartingValue.setText(activeGoal.startingValue == null ? "" : String.valueOf(activeGoal.startingValue));
        binding.goalStartingUnit.setText(n(activeGoal.startingUnit));
        binding.goalStartingSituation.setText(n(activeGoal.startingSituation));
        binding.goalStatus.setText("Status: " + activeGoal.status);
        binding.goalStatusActions.setVisibility(View.VISIBLE);
    }

    private void saveWithConfirmation() {
        GoalEntity candidate = readCandidate();
        if (candidate == null) return;
        if (activeGoal != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Change primary goal?")
                    .setMessage("Changing the active primary goal may invalidate an existing training plan. The plan will be re-evaluated when plan generation is available. Continue?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Change goal", (d, w) -> persist(candidate))
                    .show();
        } else persist(candidate);
    }

    private GoalEntity readCandidate() {
        binding.goalError.setVisibility(View.GONE);
        String title = binding.goalTitle.getText().toString().trim();
        if (title.isEmpty()) return error("Enter a goal description.");
        GoalEntity g = new GoalEntity();
        if (activeGoal != null) g.id = activeGoal.id;
        g.type = (DomainEnums.GoalType) binding.goalType.getSelectedItem();
        g.title = title;
        String date = binding.goalTargetDate.getText().toString().trim();
        if (!date.isEmpty()) try { g.targetDate = LocalDate.parse(date); } catch (Exception e) { return error("Target date must use YYYY-MM-DD."); }
        g.targetValue = parseOptionalDouble(binding.goalTargetValue.getText().toString(), "Target value must be a number."); if (parseFailed) return null;
        g.targetUnit = blankToNull(binding.goalTargetUnit.getText().toString());
        g.startingValue = parseOptionalDouble(binding.goalStartingValue.getText().toString(), "Starting value must be a number."); if (parseFailed) return null;
        g.startingUnit = blankToNull(binding.goalStartingUnit.getText().toString());
        g.startingSituation = blankToNull(binding.goalStartingSituation.getText().toString());
        return g;
    }

    private boolean parseFailed;
    private Double parseOptionalDouble(String value, String error) {
        parseFailed = false; String s = value.trim(); if (s.isEmpty()) return null;
        try { return Double.valueOf(s); } catch (NumberFormatException e) { parseFailed = true; showError(error); return null; }
    }

    private void persist(GoalEntity goal) {
        binding.goalSave.setEnabled(false);
        executor.execute(() -> { repository.saveAsPrimary(goal, activeGoal == null ? "Primary goal created" : "Primary goal edited by user", "USER"); activeGoal = repository.getActive(); runOnUiThread(() -> { binding.goalSave.setEnabled(true); render(); }); });
    }

    private void changeStatus(DomainEnums.GoalStatus status) {
        if (activeGoal == null) return;
        long id = activeGoal.id;
        executor.execute(() -> { repository.changeStatus(id, status, "Goal status changed by user", "USER"); activeGoal = repository.getActive(); runOnUiThread(this::render); });
    }

    private GoalEntity error(String message) { showError(message); return null; }
    private void showError(String message) { binding.goalError.setText(message); binding.goalError.setVisibility(View.VISIBLE); }
    private String blankToNull(String value) { String s = value.trim(); return s.isEmpty() ? null : s; }
    private String n(String value) { return value == null ? "" : value; }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdownNow(); }
}
