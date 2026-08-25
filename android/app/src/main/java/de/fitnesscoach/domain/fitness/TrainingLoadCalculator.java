package de.fitnesscoach.domain.fitness;

import java.util.List;
import de.fitnesscoach.data.entity.WorkoutEntity;

public final class TrainingLoadCalculator {
 public double workoutLoad(WorkoutEntity w){if(w==null||w.durationMinutes==null||w.durationMinutes<=0)return 0;double intensity=1.0;if(w.rpe!=null)intensity=Math.max(.5,Math.min(2.0,w.rpe/5.0));else if(w.averageHeartRate!=null)intensity=Math.max(.6,Math.min(1.8,w.averageHeartRate/120.0));return w.durationMinutes*intensity;}
 public double total(List<WorkoutEntity> workouts){double v=0;for(WorkoutEntity w:workouts)v+=workoutLoad(w);return v;}
 public double acuteChronicRatio(double sevenDayLoad,double twentyEightDayLoad){double chronic=twentyEightDayLoad/4d;return chronic<=0?0:sevenDayLoad/chronic;}
}
