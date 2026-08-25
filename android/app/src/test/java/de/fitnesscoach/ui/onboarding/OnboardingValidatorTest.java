package de.fitnesscoach.ui.onboarding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class OnboardingValidatorTest {
    @Test public void fitnessLevelRequiresValue() {
        assertFalse(OnboardingValidator.hasFitnessLevel(" "));
        assertTrue(OnboardingValidator.hasFitnessLevel("REGULAR"));
    }

    @Test public void sportPreferencesRequireAtLeastOneSport() {
        assertFalse(OnboardingValidator.hasSportPreference(Collections.emptySet()));
        assertTrue(OnboardingValidator.hasSportPreference(Collections.singleton("RUNNING")));
    }

    @Test public void availabilityRequiresDayAndPositiveDuration() {
        Set<String> days = new LinkedHashSet<>();
        days.add("MONDAY");
        assertFalse(OnboardingValidator.hasTrainingAvailability(Collections.emptySet(), 45));
        assertFalse(OnboardingValidator.hasTrainingAvailability(days, 0));
        assertTrue(OnboardingValidator.hasTrainingAvailability(days, 45));
    }

    @Test public void goalRequiresNonBlankDescription() {
        assertFalse(OnboardingValidator.hasPrimaryGoal(""));
        assertTrue(OnboardingValidator.hasPrimaryGoal("Run 10 km under 55 minutes"));
    }
}
