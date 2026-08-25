package de.fitnesscoach.data.db;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDate;

import de.fitnesscoach.data.entity.DomainEnums;

public final class RoomConverters {
    private RoomConverters() {}

    @TypeConverter public static String fromInstant(Instant value) { return value == null ? null : value.toString(); }
    @TypeConverter public static Instant toInstant(String value) { return value == null ? null : Instant.parse(value); }
    @TypeConverter public static String fromLocalDate(LocalDate value) { return value == null ? null : value.toString(); }
    @TypeConverter public static LocalDate toLocalDate(String value) { return value == null ? null : LocalDate.parse(value); }

    @TypeConverter public static String fromFitnessLevel(DomainEnums.FitnessLevel v) { return enumName(v); }
    @TypeConverter public static DomainEnums.FitnessLevel toFitnessLevel(String v) { return enumValue(DomainEnums.FitnessLevel.class, v); }
    @TypeConverter public static String fromGoalType(DomainEnums.GoalType v) { return enumName(v); }
    @TypeConverter public static DomainEnums.GoalType toGoalType(String v) { return enumValue(DomainEnums.GoalType.class, v); }
    @TypeConverter public static String fromGoalStatus(DomainEnums.GoalStatus v) { return enumName(v); }
    @TypeConverter public static DomainEnums.GoalStatus toGoalStatus(String v) { return enumValue(DomainEnums.GoalStatus.class, v); }
    @TypeConverter public static String fromSportType(DomainEnums.SportType v) { return enumName(v); }
    @TypeConverter public static DomainEnums.SportType toSportType(String v) { return enumValue(DomainEnums.SportType.class, v); }
    @TypeConverter public static String fromWorkoutType(DomainEnums.WorkoutType v) { return enumName(v); }
    @TypeConverter public static DomainEnums.WorkoutType toWorkoutType(String v) { return enumValue(DomainEnums.WorkoutType.class, v); }
    @TypeConverter public static String fromWorkoutStatus(DomainEnums.WorkoutStatus v) { return enumName(v); }
    @TypeConverter public static DomainEnums.WorkoutStatus toWorkoutStatus(String v) { return enumValue(DomainEnums.WorkoutStatus.class, v); }
    @TypeConverter public static String fromDataQuality(DomainEnums.DataQuality v) { return enumName(v); }
    @TypeConverter public static DomainEnums.DataQuality toDataQuality(String v) { return enumValue(DomainEnums.DataQuality.class, v); }
    @TypeConverter public static String fromConfidence(DomainEnums.Confidence v) { return enumName(v); }
    @TypeConverter public static DomainEnums.Confidence toConfidence(String v) { return enumValue(DomainEnums.Confidence.class, v); }
    @TypeConverter public static String fromRecoveryRecommendation(DomainEnums.RecoveryRecommendation v) { return enumName(v); }
    @TypeConverter public static DomainEnums.RecoveryRecommendation toRecoveryRecommendation(String v) { return enumValue(DomainEnums.RecoveryRecommendation.class, v); }
    @TypeConverter public static String fromMemoryType(DomainEnums.CoachMemoryType v) { return enumName(v); }
    @TypeConverter public static DomainEnums.CoachMemoryType toMemoryType(String v) { return enumValue(DomainEnums.CoachMemoryType.class, v); }
    @TypeConverter public static String fromDecisionType(DomainEnums.CoachDecisionType v) { return enumName(v); }
    @TypeConverter public static DomainEnums.CoachDecisionType toDecisionType(String v) { return enumValue(DomainEnums.CoachDecisionType.class, v); }

    private static String enumName(Enum<?> value) { return value == null ? null : value.name(); }
    private static <T extends Enum<T>> T enumValue(Class<T> type, String value) { return value == null ? null : Enum.valueOf(type, value); }
}