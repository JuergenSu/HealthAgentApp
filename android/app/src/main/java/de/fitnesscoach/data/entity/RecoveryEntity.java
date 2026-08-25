package de.fitnesscoach.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;
import java.time.LocalDate;

@Entity(tableName = "recovery")
public class RecoveryEntity {
    @PrimaryKey public LocalDate date;
    public Integer score;
    public Double sleepComponent;
    public Double restingHeartRateComponent;
    public Double trainingLoadComponent;
    public Double subjectiveComponent;
    public DomainEnums.Confidence confidence;
    public DomainEnums.RecoveryRecommendation recommendation;
    public Instant calculatedAt;
}