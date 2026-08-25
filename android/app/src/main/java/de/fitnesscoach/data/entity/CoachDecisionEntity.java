package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;

@Entity(tableName = "coach_decisions")
public class CoachDecisionEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public Instant timestamp;
    public DomainEnums.CoachDecisionType decisionType;
    public Long targetEntityId;
    public String beforeJson;
    public String afterJson;
    public String reasonCode;
    public String reasonText;
    public String source;
}