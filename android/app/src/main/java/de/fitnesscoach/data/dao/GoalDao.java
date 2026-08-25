package de.fitnesscoach.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import de.fitnesscoach.data.entity.GoalEntity;

@Dao
public interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) long insert(GoalEntity entity);
    @Update void update(GoalEntity entity);
    @Query("SELECT * FROM goals ORDER BY createdAt DESC") List<GoalEntity> getAll();
    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY priority ASC LIMIT 1") GoalEntity getActive();
}