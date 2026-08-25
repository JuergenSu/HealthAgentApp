package de.fitnesscoach.ui.onboarding;

import java.util.Set;

public final class OnboardingValidator {
    private OnboardingValidator() {}

    public static boolean hasFitnessLevel(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean hasSportPreference(Set<String> values) {
        return values != null && !values.isEmpty();
    }

    public static boolean hasTrainingAvailability(Set<String> days, int durationMinutes) {
        return days != null && !days.isEmpty() && durationMinutes > 0;
    }

    public static boolean hasPrimaryGoal(String goal) {
        return goal != null && !goal.trim().isEmpty();
    }
}
