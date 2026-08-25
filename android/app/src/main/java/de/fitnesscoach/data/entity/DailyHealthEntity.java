package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;
import java.time.LocalDate;

@Entity(tableName = "daily_health")
public class DailyHealthEntity {
    @PrimaryKey public LocalDate date;
    public Long steps;
    public Double distanceKm;
    public Integer activeCalories;
    public Integer sleepMinutes;
    public Double restingHeartRate;
    public Double averageHeartRate;
    public Double weightKg;
    public Integer exerciseMinutes;
    public DomainEnums.DataQuality dataQuality;
    public Instant calculatedAt;
}