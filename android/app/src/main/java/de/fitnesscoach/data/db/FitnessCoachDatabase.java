package de.fitnesscoach.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import de.fitnesscoach.data.dao.CoachingDao;
import de.fitnesscoach.data.dao.GoalDao;
import de.fitnesscoach.data.dao.HealthDao;
import de.fitnesscoach.data.dao.HealthSyncDao;
import de.fitnesscoach.data.dao.ProfileDao;
import de.fitnesscoach.data.dao.WorkoutDao;
import de.fitnesscoach.data.entity.*;

@Database(entities = {
        UserProfileEntity.class, GoalEntity.class, TrainingAvailabilityEntity.class,
        DailyHealthEntity.class, WorkoutEntity.class, PlannedWorkoutEntity.class,
        CheckInEntity.class, BaselineEntity.class, RecoveryEntity.class,
        CoachMemoryEntity.class, CoachDecisionEntity.class,
        HealthRecordEntity.class, HealthDailyAggregateEntity.class, HealthSyncStateEntity.class
}, version = 3, exportSchema = true)
@TypeConverters(RoomConverters.class)
public abstract class FitnessCoachDatabase extends RoomDatabase {
    public abstract ProfileDao profileDao();
    public abstract GoalDao goalDao();
    public abstract HealthDao healthDao();
    public abstract HealthSyncDao healthSyncDao();
    public abstract WorkoutDao workoutDao();
    public abstract CoachingDao coachingDao();

    private static volatile FitnessCoachDatabase INSTANCE;

    public static FitnessCoachDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FitnessCoachDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), FitnessCoachDatabase.class, "fitness-coach.db")
                            .addMigrations(DatabaseMigrations.ALL)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
