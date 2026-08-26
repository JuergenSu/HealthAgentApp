package de.fitnesscoach.domain.plan;

import de.fitnesscoach.data.entity.*;

public interface PlanAdaptationPolicy {
 PlannedWorkoutEntity adapt(PlannedWorkoutEntity workout, DomainEnums.RecoveryRecommendation recovery, PlannedWorkoutEntity previous, PlannedWorkoutEntity next);
}
