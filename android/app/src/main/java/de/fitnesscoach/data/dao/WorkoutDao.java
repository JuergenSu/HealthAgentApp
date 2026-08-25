package de.fitnesscoach.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.time.LocalDate;
import java.util.List;
import de.fitnesscoach.data.entity.PlannedWorkoutEntity;
import de.fitnesscoach.data.entity.WorkoutEntity;

@Dao
public interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) long insertWorkout(WorkoutEntity entity);
    @Update void updateWorkout(WorkoutEntity entity);
    @Query("SELECT * FROM workouts ORDER BY startTime DESC") List<WorkoutEntity> getWorkouts();
    @Insert(onConflict = OnConflictStrategy.ABORT) long insertPlanned(PlannedWorkoutEntity entity);
    @Update void updatePlanned(PlannedWorkoutEntity entity);
    @Query("SELECT * FROM planned_workouts WHERE date BETWEEN :from AND :to ORDER BY date") List<PlannedWorkoutEntity> plannedRange(LocalDate from, LocalDate to);
}