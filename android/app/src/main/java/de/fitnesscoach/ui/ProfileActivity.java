package de.fitnesscoach.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.TrainingAvailabilityEntity;
import de.fitnesscoach.data.entity.UserProfileEntity;
import de.fitnesscoach.data.repository.ProfileRepository;
import de.fitnesscoach.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ProfileRepository repository;
    private final Map<Integer, DayRow> rows = new HashMap<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new ProfileRepository(FitnessCoachDatabase.getInstance(this).profileDao());
        binding.profileFitnessLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, DomainEnums.FitnessLevel.values()));
        createAvailabilityRows();
        binding.profileSave.setOnClickListener(v -> save());
        executor.execute(() -> { ProfileRepository.ProfileSnapshot snapshot = repository.getSnapshot(); runOnUiThread(() -> render(snapshot)); });
    }

    private void createAvailabilityRows() {
        for (DayOfWeek day : DayOfWeek.values()) {
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
            CheckBox enabled = new CheckBox(this); enabled.setText(day.name().substring(0, 3));
            EditText duration = new EditText(this); duration.setHint("max min"); duration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); duration.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            row.addView(enabled); row.addView(duration); binding.profileAvailabilityRows.addView(row);
            rows.put(day.getValue(), new DayRow(enabled, duration));
        }
    }

    private void render(ProfileRepository.ProfileSnapshot snapshot) {
        UserProfileEntity p = snapshot.profile;
        if (p != null) {
            binding.profileBirthYear.setText(p.birthYear == null ? "" : String.valueOf(p.birthYear));
            binding.profileSex.setText(p.sex == null ? "" : p.sex);
            binding.profileHeight.setText(p.heightCm == null ? "" : String.format(Locale.ROOT, "%.1f", p.heightCm));
            if (p.fitnessLevel != null) binding.profileFitnessLevel.setSelection(p.fitnessLevel.ordinal());
            String sports = p.preferredSports == null ? "" : p.preferredSports;
            binding.profileRunning.setChecked(sports.contains("RUNNING")); binding.profileWalking.setChecked(sports.contains("WALKING"));
            binding.profileCycling.setChecked(sports.contains("CYCLING")); binding.profileStrength.setChecked(sports.contains("STRENGTH"));
        }
        for (TrainingAvailabilityEntity a : snapshot.availability) {
            DayRow row = rows.get(a.dayOfWeek); if (row == null) continue;
            row.enabled.setChecked(a.available); row.duration.setText(a.maxDurationMinutes == null ? "" : String.valueOf(a.maxDurationMinutes));
        }
    }

    private void save() {
        binding.profileError.setVisibility(View.GONE);
        Integer birthYear = parseInteger(binding.profileBirthYear.getText().toString());
        int currentYear = java.time.Year.now().getValue();
        if (birthYear == null || birthYear < 1900 || birthYear > currentYear) { error("Enter a valid birth year."); return; }
        Double height = parseDouble(binding.profileHeight.getText().toString());
        if (!binding.profileHeight.getText().toString().trim().isEmpty() && (height == null || height <= 0)) { error("Height must be greater than zero."); return; }

        UserProfileEntity p = new UserProfileEntity(); p.birthYear = birthYear;
        String sex = binding.profileSex.getText().toString().trim(); p.sex = sex.isEmpty() ? null : sex; p.heightCm = height;
        p.fitnessLevel = (DomainEnums.FitnessLevel) binding.profileFitnessLevel.getSelectedItem();
        List<String> sports = new ArrayList<>(); if (binding.profileRunning.isChecked()) sports.add("RUNNING"); if (binding.profileWalking.isChecked()) sports.add("WALKING"); if (binding.profileCycling.isChecked()) sports.add("CYCLING"); if (binding.profileStrength.isChecked()) sports.add("STRENGTH");
        if (sports.isEmpty()) { error("Choose at least one preferred sport."); return; } p.preferredSports = String.join(",", sports);

        List<TrainingAvailabilityEntity> availability = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            DayRow row = rows.get(day.getValue()); Integer duration = parseInteger(row.duration.getText().toString());
            if (row.enabled.isChecked() && (duration == null || duration <= 0)) { error("Enter a maximum duration for every available day."); return; }
            TrainingAvailabilityEntity a = new TrainingAvailabilityEntity(); a.dayOfWeek = day.getValue(); a.available = row.enabled.isChecked(); a.maxDurationMinutes = a.available ? duration : null; availability.add(a);
        }
        binding.profileSave.setEnabled(false);
        executor.execute(() -> { repository.save(p, availability); runOnUiThread(() -> { binding.profileSave.setEnabled(true); finish(); }); });
    }

    private Integer parseInteger(String v) { try { String s=v.trim(); return s.isEmpty()?null:Integer.valueOf(s); } catch(Exception e){ return null; } }
    private Double parseDouble(String v) { try { String s=v.trim(); return s.isEmpty()?null:Double.valueOf(s); } catch(Exception e){ return null; } }
    private void error(String message) { binding.profileError.setText(message); binding.profileError.setVisibility(View.VISIBLE); }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdownNow(); }
    private static final class DayRow { final CheckBox enabled; final EditText duration; DayRow(CheckBox e, EditText d){enabled=e;duration=d;} }
}
