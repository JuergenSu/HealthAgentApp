package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.time.Instant;
import java.time.LocalDate;

@Entity(tableName = "goals", indices = {@Index("status")})
public class GoalEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public DomainEnums.GoalType type;
    public String title;
    public LocalDate targetDate;
    public Double targetValue;
    public String targetUnit;
    public int priority;
    public DomainEnums.GoalStatus status;
    public Instant createdAt;
    public Instant updatedAt;
}