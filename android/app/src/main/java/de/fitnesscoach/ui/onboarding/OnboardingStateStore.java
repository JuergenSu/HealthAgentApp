package de.fitnesscoach.ui.onboarding;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Persists onboarding progress and draft answers until #11/#12 move confirmed profile/goal
 * values into the final Room domain model.
 */
public final class OnboardingStateStore {
    private static final String PREFS = "onboarding_state";
    private static final String KEY_STEP = "step";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_FITNESS_LEVEL = "fitness_level";
    private static final String KEY_SPORTS = "sports";
    private static final String KEY_AVAILABILITY_DAYS = "availability_days";
    private static final String KEY_DURATION = "duration_minutes";
    private static final String KEY_GOAL = "primary_goal";

    private final SharedPreferences preferences;

    public OnboardingStateStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getStep() { return preferences.getInt(KEY_STEP, 0); }
    public void setStep(int step) { preferences.edit().putInt(KEY_STEP, step).apply(); }

    public boolean isCompleted() { return preferences.getBoolean(KEY_COMPLETED, false); }
    public void markCompleted() {
        preferences.edit().putBoolean(KEY_COMPLETED, true).putInt(KEY_STEP, OnboardingStep.values().length - 1).apply();
    }

    public String getFitnessLevel() { return preferences.getString(KEY_FITNESS_LEVEL, ""); }
    public void setFitnessLevel(String value) { preferences.edit().putString(KEY_FITNESS_LEVEL, value).apply(); }

    public Set<String> getSports() {
        return new LinkedHashSet<>(preferences.getStringSet(KEY_SPORTS, new LinkedHashSet<>()));
    }
    public void setSports(Set<String> values) {
        preferences.edit().putStringSet(KEY_SPORTS, new LinkedHashSet<>(values)).apply();
    }

    public Set<String> getAvailabilityDays() {
        return new LinkedHashSet<>(preferences.getStringSet(KEY_AVAILABILITY_DAYS, new LinkedHashSet<>()));
    }
    public void setAvailabilityDays(Set<String> values) {
        preferences.edit().putStringSet(KEY_AVAILABILITY_DAYS, new LinkedHashSet<>(values)).apply();
    }

    public int getDurationMinutes() { return preferences.getInt(KEY_DURATION, 45); }
    public void setDurationMinutes(int value) { preferences.edit().putInt(KEY_DURATION, value).apply(); }

    public String getPrimaryGoal() { return preferences.getString(KEY_GOAL, ""); }
    public void setPrimaryGoal(String value) { preferences.edit().putString(KEY_GOAL, value).apply(); }
}
