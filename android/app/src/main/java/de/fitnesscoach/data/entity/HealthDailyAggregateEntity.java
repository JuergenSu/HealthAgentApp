package de.fitnesscoach.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import java.time.Instant;
import java.time.LocalDate;

@Entity(tableName = "health_daily_aggregates", primaryKeys = {"date", "metric"})
public class HealthDailyAggregateEntity {
    @NonNull public LocalDate date;
    @NonNull public String metric;
    public Double value;
    public Instant calculatedAt;
}
