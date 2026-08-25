package de.fitnesscoach.domain.fitness;

import java.util.List;
import de.fitnesscoach.data.entity.WorkoutEntity;

public interface TrainingLoadCalculator {
    double workoutLoad(WorkoutEntity workout);
    double total(List<WorkoutEntity> workouts);
    Double acuteChronicRatio(double sevenDayLoad, double twentyEightDayLoad);
}
