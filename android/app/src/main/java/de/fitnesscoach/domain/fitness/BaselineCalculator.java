package de.fitnesscoach.domain.fitness;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import de.fitnesscoach.data.entity.BaselineEntity;
import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.DomainEnums;

public final class BaselineCalculator {
    public static final class Config { public final int minimumSamples; public final double highRatio,mediumRatio; public Config(int minimumSamples,double highRatio,double mediumRatio){this.minimumSamples=minimumSamples;this.highRatio=highRatio;this.mediumRatio=mediumRatio;} public static Config defaults(){return new Config(3,.75,.50);} }
    private final Config config;
    public BaselineCalculator(){this(Config.defaults());} public BaselineCalculator(Config config){this.config=config;}
    public BaselineEntity calculate(String metric,int windowDays,List<DailyHealthEntity> days){List<Double> values=new ArrayList<>();Function<DailyHealthEntity,Double> f=extractor(metric);for(DailyHealthEntity d:days){Double v=f.apply(d);DomainEnums.DataQuality q=quality(d,metric);if(v!=null&&q!=DomainEnums.DataQuality.MISSING&&q!=DomainEnums.DataQuality.SUSPECT)values.add(v);}return result(metric,windowDays,values);}
    public BaselineEntity calculateValues(String metric,int windowDays,List<Double> values){List<Double> valid=new ArrayList<>();for(Double v:values)if(v!=null)valid.add(v);return result(metric,windowDays,valid);}
    private BaselineEntity result(String metric,int windowDays,List<Double> values){BaselineEntity b=new BaselineEntity();b.metric=metric;b.windowDays=windowDays;b.sampleCount=values.size();b.value=values.isEmpty()?null:median(values);b.confidence=confidence(values.size(),windowDays);b.calculatedAt=Instant.now();return b;}
    private DomainEnums.Confidence confidence(int n,int w){if(n<config.minimumSamples)return DomainEnums.Confidence.INSUFFICIENT;double r=w<=0?0:(double)n/w;if(r>=config.highRatio)return DomainEnums.Confidence.HIGH;if(r>=config.mediumRatio)return DomainEnums.Confidence.MEDIUM;return DomainEnums.Confidence.LOW;}
    private static double median(List<Double> v){v.sort(Double::compareTo);int n=v.size();return n%2==1?v.get(n/2):(v.get(n/2-1)+v.get(n/2))/2d;}
    private static Function<DailyHealthEntity,Double> extractor(String m){switch(m){case"sleepMinutes":return d->d.sleepMinutes==null?null:d.sleepMinutes.doubleValue();case"restingHeartRate":return d->d.restingHeartRate;case"steps":return d->d.steps==null?null:d.steps.doubleValue();case"distanceKm":return d->d.distanceKm;default:throw new IllegalArgumentException("Unsupported baseline metric: "+m);}}
    private static DomainEnums.DataQuality quality(DailyHealthEntity d,String m){switch(m){case"sleepMinutes":return d.sleepQuality;case"restingHeartRate":return d.restingHeartRateQuality;case"steps":return d.stepsQuality;case"distanceKm":return d.distanceQuality;default:return DomainEnums.DataQuality.MISSING;}}
}
