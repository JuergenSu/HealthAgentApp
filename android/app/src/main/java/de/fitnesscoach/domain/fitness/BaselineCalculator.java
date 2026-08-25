package de.fitnesscoach.domain.fitness;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import de.fitnesscoach.data.entity.BaselineEntity;
import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.DomainEnums;

public final class BaselineCalculator {
 public BaselineEntity calculate(String metric,int windowDays,List<DailyHealthEntity> days){List<Double> values=new ArrayList<>();Function<DailyHealthEntity,Double> f=extractor(metric);for(DailyHealthEntity d:days){Double v=f.apply(d);if(v!=null&&quality(d,metric)!=DomainEnums.DataQuality.SUSPECT)values.add(v);}BaselineEntity b=new BaselineEntity();b.metric=metric;b.windowDays=windowDays;b.sampleCount=values.size();b.value=values.isEmpty()?null:median(values);b.confidence=confidence(values.size(),windowDays);b.calculatedAt=Instant.now();return b;}
 private static DomainEnums.Confidence confidence(int n,int w){double r=w<=0?0:(double)n/w;if(n<3)return DomainEnums.Confidence.INSUFFICIENT;if(r>=.75)return DomainEnums.Confidence.HIGH;if(r>=.5)return DomainEnums.Confidence.MEDIUM;return DomainEnums.Confidence.LOW;}
 private static double median(List<Double> v){v.sort(Double::compareTo);int n=v.size();return n%2==1?v.get(n/2):(v.get(n/2-1)+v.get(n/2))/2d;}
 private static Function<DailyHealthEntity,Double> extractor(String m){switch(m){case"sleepMinutes":return d->d.sleepMinutes==null?null:d.sleepMinutes.doubleValue();case"restingHeartRate":return d->d.restingHeartRate;case"steps":return d->d.steps==null?null:d.steps.doubleValue();case"distanceKm":return d->d.distanceKm;case"exerciseMinutes":return d->d.exerciseMinutes==null?null:d.exerciseMinutes.doubleValue();default:throw new IllegalArgumentException("Unsupported baseline metric: "+m);}}
 private static DomainEnums.DataQuality quality(DailyHealthEntity d,String m){switch(m){case"sleepMinutes":return d.sleepQuality;case"restingHeartRate":return d.restingHeartRateQuality;case"steps":return d.stepsQuality;case"distanceKm":return d.distanceQuality;case"exerciseMinutes":return d.exerciseMinutesQuality;default:return DomainEnums.DataQuality.MISSING;}}
}
