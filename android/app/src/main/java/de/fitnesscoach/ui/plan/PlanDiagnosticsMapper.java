package de.fitnesscoach.ui.plan;

import java.time.*;
import java.util.*;

import de.fitnesscoach.data.entity.*;
import de.fitnesscoach.data.repository.FitnessCoachRepository;
import de.fitnesscoach.domain.plan.DefaultPlanAdaptationPolicy;
import de.fitnesscoach.domain.plan.TrainingPlanGenerator;
import de.fitnesscoach.domain.plan.WorkoutMatcher;

public final class PlanDiagnosticsMapper {
    private PlanDiagnosticsMapper() {}

    public static PlanDiagnosticsUiState map(LocalDate weekStart, FitnessCoachRepository repo) {
        LocalDate weekEnd = weekStart.plusDays(6);
        UserProfileEntity profile = repo.getProfile();
        GoalEntity goal = repo.getActiveGoal();
        List<TrainingAvailabilityEntity> availability = repo.getAvailability();
        BaselineEntity loadBaseline = repo.getBaseline("trainingLoad", 28);
        List<PlannedWorkoutEntity> persistedPlan = repo.getPlannedWorkouts(weekStart, weekEnd);
        List<WorkoutEntity> actual = repo.getWorkouts();

        TrainingPlanGenerator.Result evaluated = new TrainingPlanGenerator().generate(
                weekStart, profile, goal, availability, loadBaseline);

        StringBuilder b = new StringBuilder();
        b.append("EPIC 04 TRAINING PLAN DIAGNOSTICS\nWeek: ").append(weekStart).append(" – ").append(weekEnd).append("\n\n");

        b.append("1. INPUT DATA\n");
        b.append("Fitness level: ").append(profile == null || profile.fitnessLevel == null ? "missing" : profile.fitnessLevel).append('\n');
        if (goal == null) b.append("Primary goal: missing\n");
        else b.append("Primary goal: ").append(goal.type).append(" | ").append(n(goal.title))
                .append(" | target=").append(goal.targetValue == null ? "missing" : goal.targetValue + " " + n(goal.targetUnit))
                .append(" | date=").append(goal.targetDate == null ? "missing" : goal.targetDate).append('\n');
        b.append("Training-load baseline 28d: ");
        if (loadBaseline == null || loadBaseline.value == null) b.append("missing / insufficient\n");
        else b.append(fmt(loadBaseline.value)).append(" | samples=").append(loadBaseline.sampleCount)
                .append(" | confidence=").append(loadBaseline.confidence).append('\n');
        b.append("Availability:\n");
        if (availability == null || availability.isEmpty()) b.append("  missing\n");
        else for (TrainingAvailabilityEntity a : availability) {
            b.append("  ").append(DayOfWeek.of(a.dayOfWeek)).append(": ")
                    .append(a.available ? "available" : "unavailable")
                    .append(a.available ? " | max=" + (a.maxDurationMinutes == null ? "missing" : a.maxDurationMinutes + " min") : "")
                    .append(a.preferredTime == null ? "" : " | preferred=" + a.preferredTime).append('\n');
        }

        b.append("\n2. PRODUCTION GENERATOR EVALUATION\n");
        b.append("Generator confidence: ").append(evaluated.confidence).append('\n');
        b.append("Selected session count: ").append(evaluated.workouts.size()).append('\n');
        if (evaluated.workouts.isEmpty()) b.append("No sessions produced. Typical cause: no available day with at least 20 minutes.\n");
        for (PlannedWorkoutEntity p : evaluated.workouts) appendWorkout(b, "  evaluated", p);
        if (loadBaseline == null || loadBaseline.confidence == null || loadBaseline.confidence == DomainEnums.Confidence.INSUFFICIENT)
            b.append("Conservative fallback: YES — training-load baseline missing/insufficient; generator reports LOW confidence.\n");
        else b.append("Conservative fallback: no baseline-confidence fallback required.\n");
        b.append("Intensity-neighbour guard: production generator converts adjacent high-intensity selections to EASY when needed.\n");
        b.append("Duration guard: each produced duration is capped by the selected day's max duration.\n");

        b.append("\n3. PERSISTED PLAN STATE\n");
        if (persistedPlan == null || persistedPlan.isEmpty()) b.append("No persisted workouts for selected week.\n");
        else for (PlannedWorkoutEntity p : persistedPlan) appendWorkout(b, "  persisted", p);

        b.append("\n4. RECOVERY ADAPTATION EVALUATION\n");
        if (persistedPlan == null || persistedPlan.isEmpty()) b.append("No planned workout available to evaluate.\n");
        else {
            DefaultPlanAdaptationPolicy policy = new DefaultPlanAdaptationPolicy();
            for (int i = 0; i < persistedPlan.size(); i++) {
                PlannedWorkoutEntity current = persistedPlan.get(i);
                PlannedWorkoutEntity previous = i > 0 ? persistedPlan.get(i - 1) : null;
                PlannedWorkoutEntity next = i + 1 < persistedPlan.size() ? persistedPlan.get(i + 1) : null;
                RecoveryEntity recovery = repo.getRecovery(current.date);
                DomainEnums.RecoveryRecommendation rec = recovery == null || recovery.recommendation == null
                        ? DomainEnums.RecoveryRecommendation.UNKNOWN : recovery.recommendation;
                PlannedWorkoutEntity after = policy.adapt(current, rec, previous, next);
                b.append(current.date).append(" recommendation=").append(rec)
                        .append(" | previous=").append(previous == null ? "none" : previous.workoutType)
                        .append(" | next=").append(next == null ? "none" : next.workoutType).append('\n');
                b.append("  before: ").append(snapshot(current)).append('\n');
                b.append("  after:  ").append(snapshot(after)).append('\n');
                b.append("  action: ").append(after.status == DomainEnums.WorkoutStatus.ADAPTED ? "adaptation would be applied" : "no adaptation").append('\n');
            }
        }

        b.append("\n5. IMPORTED WORKOUT MATCHING\n");
        WorkoutMatcher matcher = new WorkoutMatcher();
        boolean anyActual = false;
        if (actual != null) for (WorkoutEntity w : actual) {
            if (w.startTime == null) continue;
            LocalDate d = w.startTime.atZone(ZoneId.systemDefault()).toLocalDate();
            if (d.isBefore(weekStart) || d.isAfter(weekEnd)) continue;
            anyActual = true;
            WorkoutMatcher.Match match = matcher.best(w, persistedPlan == null ? Collections.emptyList() : persistedPlan);
            b.append(d).append(" actual id=").append(w.id)
                    .append(" | sport=").append(w.sportType)
                    .append(" | type=").append(w.workoutType)
                    .append(" | duration=").append(v(w.durationMinutes)).append(" min")
                    .append(" | distance=").append(v(w.distanceKm)).append(" km\n")
                    .append("  best score=").append(fmt(match.score))
                    .append(" | confidence=").append(match.confidence)
                    .append(" | candidate=").append(match.planned == null ? "none" : match.planned.id)
                    .append(" | auto-match=").append(match.confidence == WorkoutMatcher.MatchConfidence.HIGH && match.planned != null ? "YES" : "NO")
                    .append(" | persisted link=").append(w.plannedWorkoutId == null ? "none" : w.plannedWorkoutId).append('\n');
        }
        if (!anyActual) b.append("No imported workouts in selected week.\n");
        b.append("Matching dimensions are evaluated by the production WorkoutMatcher: same day, sport compatibility, duration, distance and workout type. LOW confidence is never auto-matched.\n");

        b.append("\n6. COACHDECISION AUDIT TRAIL\n");
        List<CoachDecisionEntity> decisions = repo.getDecisions();
        boolean anyDecision = false;
        if (decisions != null) for (CoachDecisionEntity d : decisions) {
            if (!isPlanDecision(d)) continue;
            anyDecision = true;
            b.append(d.timestamp == null ? "unknown-time" : d.timestamp).append(" | ").append(d.decisionType)
                    .append(" | target=").append(d.targetEntityId)
                    .append(" | source=").append(n(d.source)).append('\n')
                    .append("  reason: ").append(n(d.reasonCode)).append(" — ").append(n(d.reasonText)).append('\n')
                    .append("  before: ").append(n(d.beforeJson)).append('\n')
                    .append("  after:  ").append(n(d.afterJson)).append('\n');
        }
        if (!anyDecision) b.append("No plan-related CoachDecision records yet.\n");

        return new PlanDiagnosticsUiState(weekStart, b.toString().trim());
    }

    private static boolean isPlanDecision(CoachDecisionEntity d) {
        if (d == null || d.decisionType == null) return false;
        switch (d.decisionType) {
            case CREATE_WORKOUT:
            case REDUCE_WORKOUT:
            case CANCEL_WORKOUT:
            case REPLACE_WORKOUT:
                return true;
            default:
                return false;
        }
    }

    private static void appendWorkout(StringBuilder b, String prefix, PlannedWorkoutEntity p) {
        b.append(prefix).append(" id=").append(p.id).append(" | ").append(p.date).append(" | ").append(p.sportType)
                .append(" | ").append(p.workoutType).append(" | duration=").append(v(p.plannedDurationMinutes)).append(" min")
                .append(" | distance=").append(v(p.plannedDistanceKm)).append(" km")
                .append(" | HR=").append(v(p.targetHeartRateMin)).append("-").append(v(p.targetHeartRateMax))
                .append(" | pace=").append(v(p.targetPaceMinSecKm)).append("-").append(v(p.targetPaceMaxSecKm))
                .append(" | status=").append(p.status).append(" | v").append(p.version)
                .append(" | original=").append(p.originalWorkoutId == null ? "none" : p.originalWorkoutId).append('\n')
                .append("    ").append(n(p.title)).append(" — ").append(n(p.description)).append('\n');
    }

    private static String snapshot(PlannedWorkoutEntity p) {
        return "type=" + p.workoutType + ", sport=" + p.sportType + ", duration=" + p.plannedDurationMinutes
                + ", status=" + p.status + ", version=" + p.version;
    }
    private static String n(String s) { return s == null || s.isBlank() ? "missing" : s; }
    private static String v(Object o) { return o == null ? "missing" : String.valueOf(o); }
    private static String fmt(double d) { return String.format(Locale.ROOT, "%.2f", d); }
}
