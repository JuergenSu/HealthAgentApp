package de.fitnesscoach.domain.plan;

import java.time.*;import java.time.temporal.ChronoUnit;import java.util.*;import de.fitnesscoach.data.entity.*;

public final class WorkoutMatcher {
 public enum MatchConfidence{HIGH,MEDIUM,LOW}public static final class Match{public final PlannedWorkoutEntity planned;public final MatchConfidence confidence;public final double score;Match(PlannedWorkoutEntity p,MatchConfidence c,double s){planned=p;confidence=c;score=s;}}
 public Match best(WorkoutEntity actual,List<PlannedWorkoutEntity>planned){if(actual==null||actual.startTime==null)return new Match(null,MatchConfidence.LOW,0);LocalDate d=actual.startTime.atZone(ZoneId.systemDefault()).toLocalDate();PlannedWorkoutEntity best=null;double bestScore=0;for(PlannedWorkoutEntity p:planned){if(p==null||p.date==null||!p.date.equals(d)||p.status==DomainEnums.WorkoutStatus.COMPLETED)continue;double s=0;if(compatible(actual.sportType,p.sportType))s+=.45;if(actual.durationMinutes!=null&&p.plannedDurationMinutes!=null){double diff=Math.abs(actual.durationMinutes-p.plannedDurationMinutes)/(double)Math.max(1,p.plannedDurationMinutes);s+=.30*Math.max(0,1-diff);}if(actual.distanceKm!=null&&p.plannedDistanceKm!=null){double diff=Math.abs(actual.distanceKm-p.plannedDistanceKm)/Math.max(.1,p.plannedDistanceKm);s+=.15*Math.max(0,1-diff);}else s+=.05;if(p.workoutType==actual.workoutType)s+=.10;if(s>bestScore){bestScore=s;best=p;}}MatchConfidence c=bestScore>=.72?MatchConfidence.HIGH:bestScore>=.50?MatchConfidence.MEDIUM:MatchConfidence.LOW;return new Match(c==MatchConfidence.LOW?null:best,c,bestScore);}
 private boolean compatible(DomainEnums.SportType a,DomainEnums.SportType p){return a!=null&&p!=null&&(a==p||(a==DomainEnums.SportType.WALKING&&p==DomainEnums.SportType.RUNNING));}
}
