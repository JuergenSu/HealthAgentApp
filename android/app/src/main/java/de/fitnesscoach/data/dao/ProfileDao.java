package de.fitnesscoach.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import de.fitnesscoach.data.entity.TrainingAvailabilityEntity;
import de.fitnesscoach.data.entity.UserProfileEntity;

@Dao
public interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertProfile(UserProfileEntity entity);
    @Query("SELECT * FROM user_profile LIMIT 1") UserProfileEntity getProfile();
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertAvailability(TrainingAvailabilityEntity entity);
    @Query("SELECT * FROM training_availability ORDER BY dayOfWeek") List<TrainingAvailabilityEntity> getAvailability();
    @Query("DELETE FROM training_availability") void deleteAvailability();
}
