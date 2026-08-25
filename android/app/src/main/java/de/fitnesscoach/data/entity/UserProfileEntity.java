package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;

@Entity(tableName = "user_profile")
public class UserProfileEntity {
    @PrimaryKey public long id;
    public Integer birthYear;
    public String sex;
    public Double heightCm;
    public DomainEnums.FitnessLevel fitnessLevel;
    /** Comma-separated SportType names; kept scalar to make Room migrations explicit and portable. */
    public String preferredSports;
    public String preferredTrainingTime;
    public Instant createdAt;
    public Instant updatedAt;
}
