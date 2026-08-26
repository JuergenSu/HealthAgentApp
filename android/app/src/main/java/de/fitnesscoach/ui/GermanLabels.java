package de.fitnesscoach.ui;

import java.time.DayOfWeek;
import de.fitnesscoach.data.entity.DomainEnums;

public final class GermanLabels {
    private GermanLabels() {}
    public static String fitness(DomainEnums.FitnessLevel v){if(v==null)return"Nicht verfügbar";switch(v){case BEGINNER:return"Einsteiger";case RECREATIONAL:return"Freizeit";case REGULAR:return"Regelmäßig";case ADVANCED:return"Fortgeschritten";default:return v.name();}}
    public static String sport(DomainEnums.SportType v){if(v==null)return"Nicht verfügbar";switch(v){case RUNNING:return"Laufen";case WALKING:return"Gehen";case CYCLING:return"Radfahren";case STRENGTH:return"Krafttraining";case OTHER:return"Sonstiges";default:return v.name();}}
    public static String workout(DomainEnums.WorkoutType v){if(v==null)return"Nicht verfügbar";switch(v){case REST:return"Ruhetag";case RECOVERY:return"Regeneration";case EASY:return"Locker";case LONG:return"Langer Lauf";case INTERVAL:return"Intervalle";case TEMPO:return"Tempolauf";case STRENGTH:return"Krafttraining";case MOBILITY:return"Mobilität";case CROSS_TRAINING:return"Ausgleichstraining";case OTHER:return"Sonstiges";default:return v.name();}}
    public static String workoutStatus(DomainEnums.WorkoutStatus v){if(v==null)return"Nicht verfügbar";switch(v){case PLANNED:return"Geplant";case ADAPTED:return"Angepasst";case COMPLETED:return"Abgeschlossen";case SKIPPED:return"Ausgelassen";case CANCELLED:return"Abgebrochen";default:return v.name();}}
    public static String confidence(DomainEnums.Confidence v){if(v==null)return"Nicht verfügbar";switch(v){case HIGH:return"Hoch";case MEDIUM:return"Mittel";case LOW:return"Niedrig";case INSUFFICIENT:return"Unzureichend";default:return v.name();}}
    public static String quality(DomainEnums.DataQuality v){if(v==null)return"Nicht verfügbar";switch(v){case AVAILABLE:return"Verfügbar";case MISSING:return"Fehlt";case PARTIAL:return"Teilweise";case SUSPECT:return"Auffällig";default:return v.name();}}
    public static String recovery(DomainEnums.RecoveryRecommendation v){if(v==null)return"Nicht verfügbar";switch(v){case FULL:return"Volles Training";case NORMAL:return"Normal trainieren";case REDUCED:return"Reduziert trainieren";case RECOVERY_ONLY:return"Nur Regeneration";case REST:return"Ruhetag";case UNKNOWN:return"Unbekannt";default:return v.name();}}
    public static String goalStatus(DomainEnums.GoalStatus v){if(v==null)return"Nicht verfügbar";switch(v){case ACTIVE:return"Aktiv";case ACHIEVED:return"Erreicht";case PAUSED:return"Pausiert";case CANCELLED:return"Abgebrochen";default:return v.name();}}
    public static String goalType(DomainEnums.GoalType v){if(v==null)return"Nicht verfügbar";switch(v){case RUN_5K_TIME:return"5-km-Zeit";case RUN_10K_TIME:return"10-km-Zeit";case HALF_MARATHON_TIME:return"Halbmarathon-Zeit";case WEEKLY_ACTIVITY:return"Wöchentliche Aktivität";case GENERAL_ENDURANCE:return"Allgemeine Ausdauer";case CUSTOM:return"Individuelles Ziel";default:return v.name();}}
    public static String day(DayOfWeek d){if(d==null)return"";switch(d){case MONDAY:return"Montag";case TUESDAY:return"Dienstag";case WEDNESDAY:return"Mittwoch";case THURSDAY:return"Donnerstag";case FRIDAY:return"Freitag";case SATURDAY:return"Samstag";case SUNDAY:return"Sonntag";default:return d.name();}}
}
