package de.fitnesscoach.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.time.LocalDate;
import java.util.List;
import de.fitnesscoach.data.entity.BaselineEntity;
import de.fitnesscoach.data.entity.CheckInEntity;
import de.fitnesscoach.data.entity.CoachDecisionEntity;
import de.fitnesscoach.data.entity.CoachMemoryEntity;
import de.fitnesscoach.data.entity.RecoveryEntity;

@Dao
public interface CoachingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertCheckIn(CheckInEntity entity);
    @Query("SELECT * FROM check_ins WHERE date = :date") CheckInEntity getCheckIn(LocalDate date);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertBaseline(BaselineEntity entity);
    @Query("SELECT * FROM baselines WHERE metric = :metric AND windowDays = :windowDays LIMIT 1") BaselineEntity getBaseline(String metric, int windowDays);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertRecovery(RecoveryEntity entity);
    @Query("SELECT * FROM recovery WHERE date = :date") RecoveryEntity getRecovery(LocalDate date);
    @Insert(onConflict = OnConflictStrategy.ABORT) long insertMemory(CoachMemoryEntity entity);
    @Query("SELECT * FROM coach_memory WHERE active = 1 ORDER BY updatedAt DESC") List<CoachMemoryEntity> getActiveMemories();
    @Insert(onConflict = OnConflictStrategy.ABORT) long insertDecision(CoachDecisionEntity entity);
    @Query("SELECT * FROM coach_decisions ORDER BY timestamp DESC") List<CoachDecisionEntity> getDecisions();
}