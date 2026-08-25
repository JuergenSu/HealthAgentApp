package de.fitnesscoach.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import de.fitnesscoach.data.entity.HealthDailyAggregateEntity;
import de.fitnesscoach.data.entity.HealthRecordEntity;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;

@Dao
public interface HealthSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertRecord(HealthRecordEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertRecords(List<HealthRecordEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertAggregate(HealthDailyAggregateEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertAggregates(List<HealthDailyAggregateEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertState(HealthSyncStateEntity entity);
    @Query("SELECT * FROM health_sync_state WHERE id = 1") HealthSyncStateEntity getState();
    @Query("SELECT COUNT(*) FROM health_records WHERE recordType = :recordType") int countRecords(String recordType);
    @Query("SELECT COUNT(*) FROM health_daily_aggregates WHERE metric = :metric") int countAggregates(String metric);
}
