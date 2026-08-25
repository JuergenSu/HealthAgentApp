package de.fitnesscoach.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.GoalEntity;

/** One-time bridge from the #10 onboarding draft to the Room goal model. */
public final class GoalOnboardingMigrator {
    private static final String PREFS = "onboarding_state";
    private static final String KEY_GOAL = "primary_goal";
    private static final String KEY_MIGRATED = "goal_migrated_to_room";
    private GoalOnboardingMigrator() {}

    public static void migrateIfNeeded(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_MIGRATED, false)) return;
        GoalRepository repository = new GoalRepository(FitnessCoachDatabase.getInstance(context));
        if (repository.getActive() == null) {
            String draft = prefs.getString(KEY_GOAL, "");
            if (draft != null && !draft.trim().isEmpty()) {
                GoalEntity goal = new GoalEntity();
                goal.type = DomainEnums.GoalType.CUSTOM;
                goal.title = draft.trim();
                repository.saveAsPrimary(goal, "Migrated from onboarding draft", "ONBOARDING");
            }
        }
        prefs.edit().putBoolean(KEY_MIGRATED, true).apply();
    }
}
