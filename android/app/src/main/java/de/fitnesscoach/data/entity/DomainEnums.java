package de.fitnesscoach.data.entity;

public final class DomainEnums {
    private DomainEnums() {}

    public enum FitnessLevel { BEGINNER, RECREATIONAL, REGULAR, ADVANCED }
    public enum GoalType { RUN_5K_TIME, RUN_10K_TIME, HALF_MARATHON_TIME, WEEKLY_ACTIVITY, GENERAL_ENDURANCE, CUSTOM }
    public enum GoalStatus { ACTIVE, ACHIEVED, PAUSED, CANCELLED }
    public enum SportType { RUNNING, WALKING, CYCLING, STRENGTH, OTHER }
    public enum WorkoutType { REST, RECOVERY, EASY, LONG, INTERVAL, TEMPO, STRENGTH, MOBILITY, CROSS_TRAINING, OTHER }
    public enum WorkoutStatus { PLANNED, ADAPTED, COMPLETED, SKIPPED, CANCELLED }
    public enum DataQuality { AVAILABLE, MISSING, PARTIAL, SUSPECT }
    public enum Confidence { HIGH, MEDIUM, LOW, INSUFFICIENT }
    public enum RecoveryRecommendation { FULL, NORMAL, REDUCED, RECOVERY_ONLY, REST, UNKNOWN }
    public enum CoachMemoryType { TRAINING_PREFERENCE, AVAILABILITY, DISLIKE, EQUIPMENT, LONG_TERM_PREFERENCE }
    public enum CoachDecisionType { REDUCE_WORKOUT, RESCHEDULE_WORKOUT, REPLACE_WORKOUT, CANCEL_WORKOUT, CREATE_WORKOUT, CHANGE_GOAL }
}