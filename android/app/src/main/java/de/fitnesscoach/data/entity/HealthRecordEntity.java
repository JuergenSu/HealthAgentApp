package de.fitnesscoach.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.time.Instant;

@Entity(tableName = "health_records", indices = {@Index("recordType"), @Index("startTime")})
public class HealthRecordEntity {
    @PrimaryKey @NonNull public String recordKey;
    @NonNull public String recordType;
    public String externalRecordId;
    public String sourcePackage;
    public Instant startTime;
    public Instant endTime;
    public Double value1;
    public Double value2;
    public Integer sampleCount;
    public Instant lastModifiedTime;
}
