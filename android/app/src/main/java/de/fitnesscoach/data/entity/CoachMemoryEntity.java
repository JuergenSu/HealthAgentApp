package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;

@Entity(tableName = "coach_memory")
public class CoachMemoryEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public DomainEnums.CoachMemoryType type;
    public String value;
    public boolean active;
    public Instant createdAt;
    public Instant updatedAt;
}