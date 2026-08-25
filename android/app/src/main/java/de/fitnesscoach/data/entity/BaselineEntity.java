package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.time.Instant;

@Entity(tableName = "baselines", indices = {@Index(value = {"metric", "windowDays"}, unique = true)})
public class BaselineEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String metric;
    public int windowDays;
    public Double value;
    public int sampleCount;
    public DomainEnums.Confidence confidence;
    public Instant calculatedAt;
}