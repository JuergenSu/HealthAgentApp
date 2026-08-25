package de.fitnesscoach.domain.fitness;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.WorkoutEntity;

public final class DefaultTrainingLoadCalculator implements TrainingLoadCalculator {
    private final Map<DomainEnums.WorkoutType, Integer> fallbackRpe;

    public DefaultTrainingLoadCalculator() { this(defaults()); }
    public DefaultTrainingLoadCalculator(Map<DomainEnums.WorkoutType, Integer> fallbackRpe) { this.fallbackRpe = new EnumMap<>(fallbackRpe); }

    @Override public double workoutLoad(WorkoutEntity w) {
        if (w == null || w.durationMinutes == null || w.durationMinutes <= 0) return 0;
        int rpe = w.rpe != null ? validateRpe(w.rpe) : fallbackRpe.getOrDefault(w.workoutType, 3);
        return w.durationMinutes * (double) rpe;
    }
    @Override public double total(List<WorkoutEntity> workouts) { double sum=0; if(workouts!=null) for(WorkoutEntity w:workouts) sum+=workoutLoad(w); return sum; }
    @Override public Double acuteChronicRatio(double sevenDayLoad,double twentyEightDayLoad) { double chronic=twentyEightDayLoad/4d; return chronic<=0 ? null : sevenDayLoad/chronic; }
    private static int validateRpe(int rpe){ if(rpe<1||rpe>10) throw new IllegalArgumentException("RPE must be 1..10"); return rpe; }
    private static Map<DomainEnums.WorkoutType,Integer> defaults(){ EnumMap<DomainEnums.WorkoutType,Integer> m=new EnumMap<>(DomainEnums.WorkoutType.class);m.put(DomainEnums.WorkoutType.RECOVERY,2);m.put(DomainEnums.WorkoutType.EASY,3);m.put(DomainEnums.WorkoutType.LONG,4);m.put(DomainEnums.WorkoutType.TEMPO,6);m.put(DomainEnums.WorkoutType.INTERVAL,8);return m; }
}
