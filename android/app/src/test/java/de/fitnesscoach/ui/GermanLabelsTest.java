package de.fitnesscoach.ui;

import static org.junit.Assert.*;import java.time.DayOfWeek;import org.junit.Test;import de.fitnesscoach.data.entity.DomainEnums;

public class GermanLabelsTest {
 @Test public void mapsCoreDomainEnumsToGermanPresentation(){assertEquals("Einsteiger",GermanLabels.fitness(DomainEnums.FitnessLevel.BEGINNER));assertEquals("Laufen",GermanLabels.sport(DomainEnums.SportType.RUNNING));assertEquals("Langer Lauf",GermanLabels.workout(DomainEnums.WorkoutType.LONG));assertEquals("Angepasst",GermanLabels.workoutStatus(DomainEnums.WorkoutStatus.ADAPTED));assertEquals("Unzureichend",GermanLabels.confidence(DomainEnums.Confidence.INSUFFICIENT));assertEquals("Auffällig",GermanLabels.quality(DomainEnums.DataQuality.SUSPECT));assertEquals("Nur Regeneration",GermanLabels.recovery(DomainEnums.RecoveryRecommendation.RECOVERY_ONLY));assertEquals("10-km-Zeit",GermanLabels.goalType(DomainEnums.GoalType.RUN_10K_TIME));assertEquals("Mittwoch",GermanLabels.day(DayOfWeek.WEDNESDAY));}
 @Test public void presentationDoesNotExposeInternalEnumNames(){assertNotEquals(DomainEnums.WorkoutType.RECOVERY.name(),GermanLabels.workout(DomainEnums.WorkoutType.RECOVERY));assertNotEquals(DomainEnums.GoalStatus.CANCELLED.name(),GermanLabels.goalStatus(DomainEnums.GoalStatus.CANCELLED));}
}
