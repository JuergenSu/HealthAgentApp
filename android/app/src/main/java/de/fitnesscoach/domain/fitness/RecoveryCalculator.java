package de.fitnesscoach.domain.fitness;

import java.time.Instant;
import java.time.LocalDate;
import de.fitnesscoach.data.entity.*;

public final class RecoveryCalculator {
 public RecoveryEntity calculate(LocalDate date,DailyHealthEntity today,BaselineEntity sleepBase,BaselineEntity rhrBase,double acuteChronic,CheckInEntity checkIn){RecoveryEntity r=new RecoveryEntity();r.date=date;double weighted=0,weights=0;int inputs=0;
  if(today!=null&&today.sleepMinutes!=null&&sleepBase!=null&&sleepBase.value!=null&&sleepBase.value>0){r.sleepComponent=clamp(100d*today.sleepMinutes/sleepBase.value);weighted+=r.sleepComponent*.35;weights+=.35;inputs++;}
  if(today!=null&&today.restingHeartRate!=null&&rhrBase!=null&&rhrBase.value!=null&&rhrBase.value>0){double delta=(today.restingHeartRate-rhrBase.value)/rhrBase.value;r.restingHeartRateComponent=clamp(100-delta*300);weighted+=r.restingHeartRateComponent*.25;weights+=.25;inputs++;}
  r.trainingLoadComponent=clamp(100-Math.max(0,acuteChronic-1)*60);weighted+=r.trainingLoadComponent*.20;weights+=.20;inputs++;
  if(checkIn!=null){double s=subjective(checkIn);if(!Double.isNaN(s)){r.subjectiveComponent=s;weighted+=s*.20;weights+=.20;inputs++;}}
  if(weights==0){r.score=null;r.confidence=DomainEnums.Confidence.INSUFFICIENT;r.recommendation=DomainEnums.RecoveryRecommendation.UNKNOWN;}else{r.score=(int)Math.round(weighted/weights);r.confidence=inputs>=4?DomainEnums.Confidence.HIGH:inputs==3?DomainEnums.Confidence.MEDIUM:inputs==2?DomainEnums.Confidence.LOW:DomainEnums.Confidence.INSUFFICIENT;r.recommendation=recommend(r.score,r.confidence);}r.calculatedAt=Instant.now();return r;}
 private static double subjective(CheckInEntity c){int n=0;double v=0;if(c.energy!=null){v+=scale(c.energy);n++;}if(c.motivation!=null){v+=scale(c.motivation);n++;}if(c.muscleFatigue!=null){v+=100-scale(c.muscleFatigue);n++;}if(c.stress!=null){v+=100-scale(c.stress);n++;}return n==0?Double.NaN:v/n;}
 private static double scale(int v){return clamp((v-1)*25d);}private static double clamp(double v){return Math.max(0,Math.min(100,v));}
 private static DomainEnums.RecoveryRecommendation recommend(int s,DomainEnums.Confidence c){if(c==DomainEnums.Confidence.INSUFFICIENT)return DomainEnums.RecoveryRecommendation.UNKNOWN;if(s>=85)return DomainEnums.RecoveryRecommendation.FULL;if(s>=70)return DomainEnums.RecoveryRecommendation.NORMAL;if(s>=55)return DomainEnums.RecoveryRecommendation.REDUCED;if(s>=40)return DomainEnums.RecoveryRecommendation.RECOVERY_ONLY;return DomainEnums.RecoveryRecommendation.REST;}
}
