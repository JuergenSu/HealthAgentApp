package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;

@Entity(tableName = "health_sync_state")
public class HealthSyncStateEntity {
    @PrimaryKey public int id = 1;
    public Instant initialImportStart;
    public Instant lastAttemptAt;
    public Instant lastSuccessfulSyncAt;
    public String lastError;
}
