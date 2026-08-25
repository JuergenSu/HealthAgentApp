package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.time.Instant;

@Entity(tableName = "workouts", indices = {@Index(value = "externalRecordId", unique = true), @Index("plannedWorkoutId")})
public class WorkoutEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public Long plannedWorkoutId;
    public DomainEnums.SportType sportType;
    public DomainEnums.WorkoutType workoutType;
    public Instant startTime;
    public Instant endTime;
    public Integer durationMinutes;
    public Double distanceKm;
    public Double averageHeartRate;
    public Double maxHeartRate;
    public Integer averagePaceSecPerKm;
    public Integer rpe;
    public String source;
    public String externalRecordId;
    public DomainEnums.WorkoutStatus status;
    public Instant createdAt;
}