package de.fitnesscoach.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;
import java.time.LocalDate;

@Entity(tableName = "daily_health")
public class DailyHealthEntity {
    @PrimaryKey @NonNull public LocalDate date;

    public Long steps;
    public DomainEnums.DataQuality stepsQuality;

    public Double distanceKm;
    public DomainEnums.DataQuality distanceQuality;

    public Integer activeCalories;
    public DomainEnums.DataQuality activeCaloriesQuality;

    public Integer sleepMinutes;
    public DomainEnums.DataQuality sleepQuality;

    public Double restingHeartRate;
    public DomainEnums.DataQuality restingHeartRateQuality;

    public Double averageHeartRate;
    public DomainEnums.DataQuality averageHeartRateQuality;

    public Double weightKg;
    public DomainEnums.DataQuality weightQuality;

    public Integer exerciseMinutes;
    public DomainEnums.DataQuality exerciseMinutesQuality;

    /** Overall summary quality. Per-metric quality fields remain the authoritative detail. */
    public DomainEnums.DataQuality dataQuality;
    public Instant calculatedAt;
}
