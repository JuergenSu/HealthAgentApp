package de.fitnesscoach.data.repository;

import java.time.LocalDate;
import java.util.List;
import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.*;

public class RoomFitnessCoachRepository implements FitnessCoachRepository {
    private final FitnessCoachDatabase db;
    public RoomFitnessCoachRepository(FitnessCoachDatabase db) { this.db = db; }

    @Override public UserProfileEntity getProfile() { return db.profileDao().getProfile(); }
    @Override public void saveProfile(UserProfileEntity v) { db.profileDao().upsertProfile(v); }
    @Override public List<TrainingAvailabilityEntity> getAvailability() { return db.profileDao().getAvailability(); }
    @Override public void saveAvailability(TrainingAvailabilityEntity v) { db.profileDao().upsertAvailability(v); }
    @Override public GoalEntity getActiveGoal() { return db.goalDao().getActive(); }
    @Override public long insertGoal(GoalEntity v) { return db.goalDao().insert(v); }
    @Override public DailyHealthEntity getDailyHealth(LocalDate d) { return db.healthDao().get(d); }
    @Override public void saveDailyHealth(DailyHealthEntity v) { db.healthDao().upsert(v); }
    @Override public List<WorkoutEntity> getWorkouts() { return db.workoutDao().getWorkouts(); }
    @Override public long insertWorkout(WorkoutEntity v) { return db.workoutDao().insertWorkout(v); }
    @Override public List<PlannedWorkoutEntity> getPlannedWorkouts(LocalDate f, LocalDate t) { return db.workoutDao().plannedRange(f, t); }
    @Override public long insertPlannedWorkout(PlannedWorkoutEntity v) { return db.workoutDao().insertPlanned(v); }
    @Override public CheckInEntity getCheckIn(LocalDate d) { return db.coachingDao().getCheckIn(d); }
    @Override public void saveCheckIn(CheckInEntity v) { db.coachingDao().upsertCheckIn(v); }
    @Override public BaselineEntity getBaseline(String m, int w) { return db.coachingDao().getBaseline(m, w); }
    @Override public void saveBaseline(BaselineEntity v) { db.coachingDao().upsertBaseline(v); }
    @Override public RecoveryEntity getRecovery(LocalDate d) { return db.coachingDao().getRecovery(d); }
    @Override public void saveRecovery(RecoveryEntity v) { db.coachingDao().upsertRecovery(v); }
    @Override public List<CoachMemoryEntity> getActiveMemories() { return db.coachingDao().getActiveMemories(); }
    @Override public long insertMemory(CoachMemoryEntity v) { return db.coachingDao().insertMemory(v); }
    @Override public List<CoachDecisionEntity> getDecisions() { return db.coachingDao().getDecisions(); }
    @Override public long insertDecision(CoachDecisionEntity v) { return db.coachingDao().insertDecision(v); }
}