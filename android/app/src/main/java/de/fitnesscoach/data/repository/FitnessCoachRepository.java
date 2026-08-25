package de.fitnesscoach.data.repository;

import java.time.LocalDate;
import java.util.List;
import de.fitnesscoach.data.entity.*;

public interface FitnessCoachRepository {
    UserProfileEntity getProfile();
    void saveProfile(UserProfileEntity profile);
    List<TrainingAvailabilityEntity> getAvailability();
    void saveAvailability(TrainingAvailabilityEntity availability);
    GoalEntity getActiveGoal();
    long insertGoal(GoalEntity goal);
    DailyHealthEntity getDailyHealth(LocalDate date);
    void saveDailyHealth(DailyHealthEntity health);
    List<WorkoutEntity> getWorkouts();
    long insertWorkout(WorkoutEntity workout);
    List<PlannedWorkoutEntity> getPlannedWorkouts(LocalDate from, LocalDate to);
    long insertPlannedWorkout(PlannedWorkoutEntity workout);
    CheckInEntity getCheckIn(LocalDate date);
    void saveCheckIn(CheckInEntity checkIn);
    BaselineEntity getBaseline(String metric, int windowDays);
    void saveBaseline(BaselineEntity baseline);
    RecoveryEntity getRecovery(LocalDate date);
    void saveRecovery(RecoveryEntity recovery);
    List<CoachMemoryEntity> getActiveMemories();
    long insertMemory(CoachMemoryEntity memory);
    List<CoachDecisionEntity> getDecisions();
    long insertDecision(CoachDecisionEntity decision);
}