package de.fitnesscoach.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.time.LocalDate;
import java.util.List;
import de.fitnesscoach.data.entity.DailyHealthEntity;

@Dao
public interface HealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsert(DailyHealthEntity entity);
    @Query("SELECT * FROM daily_health WHERE date = :date") DailyHealthEntity get(LocalDate date);
    @Query("SELECT * FROM daily_health WHERE date BETWEEN :from AND :to ORDER BY date") List<DailyHealthEntity> range(LocalDate from, LocalDate to);
}