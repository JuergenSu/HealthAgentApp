package de.fitnesscoach.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;
import java.time.LocalDate;

@Entity(tableName = "check_ins")
public class CheckInEntity {
    @PrimaryKey @NonNull public LocalDate date;
    public Integer energy;
    public Integer muscleFatigue;
    public Integer motivation;
    public Integer stress;
    public String freeText;
    public Instant createdAt;
}