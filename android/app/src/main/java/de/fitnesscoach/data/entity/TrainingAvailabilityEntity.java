package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training_availability")
public class TrainingAvailabilityEntity {
    @PrimaryKey public int dayOfWeek;
    public boolean available;
    public Integer maxDurationMinutes;
    public String preferredTime;
}