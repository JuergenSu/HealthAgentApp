package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.time.Instant;
import java.time.LocalDate;

@Entity(tableName = "planned_workouts", indices = {@Index("date"), @Index("planId")})
public class PlannedWorkoutEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public Long planId;
    public LocalDate date;
    public DomainEnums.SportType sportType;
    public DomainEnums.WorkoutType workoutType;
    public String title;
    public String description;
    public Integer plannedDurationMinutes;
    public Double plannedDistanceKm;
    public Double targetHeartRateMin;
    public Double targetHeartRateMax;
    public Integer targetPaceMinSecKm;
    public Integer targetPaceMaxSecKm;
    public DomainEnums.WorkoutStatus status;
    public Long originalWorkoutId;
    public int version;
    public Instant createdAt;
    public Instant updatedAt;
}