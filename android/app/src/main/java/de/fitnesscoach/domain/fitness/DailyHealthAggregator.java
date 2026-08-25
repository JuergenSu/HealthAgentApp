package de.fitnesscoach.domain.fitness;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.fitnesscoach.data.dao.HealthDao;
import de.fitnesscoach.data.dao.HealthSyncDao;
import de.fitnesscoach.data.dao.WorkoutDao;
import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.HealthDailyAggregateEntity;
import de.fitnesscoach.data.entity.HealthRecordEntity;
import de.fitnesscoach.data.entity.WorkoutEntity;
import de.fitnesscoach.health.HealthPermissionSpec;

/**
 * Converts synchronized Health Connect staging data into stable calendar-day metrics.
 * This class is deterministic for a fixed input dataset, zone and calculation timestamp.
 */
public final class DailyHealthAggregator {

    public interface ReadAccess {
        boolean canRead(HealthPermissionSpec spec);
    }

    private static final double MAX_DISTANCE_KM = 200.0;
    private static final long MAX_STEPS = 100_000L;
    private static final int MAX_ACTIVE_CALORIES = 20_000;
    private static final int MAX_SLEEP_MINUTES = 24 * 60;
    private static final double MIN_HEART_RATE = 25.0;
    private static final double MAX_AVERAGE_HEART_RATE = 240.0;
    private static final double MAX_RESTING_HEART_RATE = 220.0;
    private static final double MIN_WEIGHT_KG = 20.0;
    private static final double MAX_WEIGHT_KG = 500.0;
    private static final int MAX_EXERCISE_MINUTES = 24 * 60;

    private final HealthSyncDao syncDao;
    private final WorkoutDao workoutDao;
    private final HealthDao healthDao;
    private final ZoneId zoneId;
    private final ReadAccess readAccess;

    public DailyHealthAggregator(HealthSyncDao syncDao, WorkoutDao workoutDao,
                                 HealthDao healthDao, ZoneId zoneId, ReadAccess readAccess) {
        this.syncDao = syncDao;
        this.workoutDao = workoutDao;
        this.healthDao = healthDao;
        this.zoneId = zoneId;
        this.readAccess = readAccess;
    }

    public void aggregateRange(LocalDate firstDay, LocalDate lastDayInclusive, Instant calculatedAt) {
        if (firstDay == null || lastDayInclusive == null || firstDay.isAfter(lastDayInclusive)) return;
        LocalDate day = firstDay;
        while (!day.isAfter(lastDayInclusive)) {
            healthDao.upsert(aggregateDay(day, calculatedAt));
            day = day.plusDays(1);
        }
    }

    public DailyHealthEntity aggregateDay(LocalDate day, Instant calculatedAt) {
        Instant start = day.atStartOfDay(zoneId).toInstant();
        Instant end = day.plusDays(1).atStartOfDay(zoneId).toInstant();

        DailyHealthEntity result = new DailyHealthEntity();
        result.date = day;
        result.calculatedAt = calculatedAt;

        Metric<Long> steps = aggregateLongMetric(day, "STEPS", HealthPermissionSpec.STEPS,
                1.0, value -> value >= 0 && value <= MAX_STEPS);
        result.steps = steps.value;
        result.stepsQuality = steps.quality;

        Metric<Double> distance = aggregateDoubleMetric(day, "DISTANCE_METERS", HealthPermissionSpec.DISTANCE,
                1.0 / 1000.0, value -> value >= 0 && value <= MAX_DISTANCE_KM);
        result.distanceKm = distance.value;
        result.distanceQuality = distance.quality;

        Metric<Integer> calories = aggregateIntMetric(day, "ACTIVE_CALORIES", HealthPermissionSpec.ACTIVE_CALORIES,
                value -> value >= 0 && value <= MAX_ACTIVE_CALORIES);
        result.activeCalories = calories.value;
        result.activeCaloriesQuality = calories.quality;

        Metric<Integer> sleep = sleepMinutes(start, end);
        result.sleepMinutes = sleep.value;
        result.sleepQuality = sleep.quality;

        Metric<Double> restingHeartRate = restingHeartRate(start, end);
        result.restingHeartRate = restingHeartRate.value;
        result.restingHeartRateQuality = restingHeartRate.quality;

        Metric<Double> averageHeartRate = averageHeartRate(start, end);
        result.averageHeartRate = averageHeartRate.value;
        result.averageHeartRateQuality = averageHeartRate.quality;

        Metric<Double> weight = weight(start, end);
        result.weightKg = weight.value;
        result.weightQuality = weight.quality;

        Metric<Integer> exercise = exerciseMinutes(start, end);
        result.exerciseMinutes = exercise.value;
        result.exerciseMinutesQuality = exercise.quality;

        result.dataQuality = overallQuality(result);
        return result;
    }

    private Metric<Long> aggregateLongMetric(LocalDate day, String metric, HealthPermissionSpec permission,
                                              double factor, LongValidator validator) {
        if (!readAccess.canRead(permission)) return Metric.partial(null);
        HealthDailyAggregateEntity row = syncDao.getAggregate(day, metric);
        if (row == null || row.value == null || !Double.isFinite(row.value)) return Metric.missing();
        long value = Math.round(row.value * factor);
        return validator.valid(value) ? Metric.available(value) : Metric.suspect(value);
    }

    private Metric<Integer> aggregateIntMetric(LocalDate day, String metric, HealthPermissionSpec permission,
                                                IntValidator validator) {
        if (!readAccess.canRead(permission)) return Metric.partial(null);
        HealthDailyAggregateEntity row = syncDao.getAggregate(day, metric);
        if (row == null || row.value == null || !Double.isFinite(row.value)) return Metric.missing();
        long rounded = Math.round(row.value);
        int value = rounded > Integer.MAX_VALUE ? Integer.MAX_VALUE :
                (rounded < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) rounded);
        return validator.valid(value) && rounded == value ? Metric.available(value) : Metric.suspect(value);
    }

    private Metric<Double> aggregateDoubleMetric(LocalDate day, String metric, HealthPermissionSpec permission,
                                                  double factor, DoubleValidator validator) {
        if (!readAccess.canRead(permission)) return Metric.partial(null);
        HealthDailyAggregateEntity row = syncDao.getAggregate(day, metric);
        if (row == null || row.value == null || !Double.isFinite(row.value)) return Metric.missing();
        double value = row.value * factor;
        return validator.valid(value) ? Metric.available(value) : Metric.suspect(value);
    }

    private Metric<Integer> sleepMinutes(Instant start, Instant end) {
        if (!readAccess.canRead(HealthPermissionSpec.SLEEP)) return Metric.partial(null);
        List<HealthRecordEntity> records = syncDao.recordsOverlapping("SLEEP", start, end);
        if (records.isEmpty()) return Metric.missing();
        IntervalResult intervals = mergedMinutes(records, start, end);
        if (intervals.minutes == null) return Metric.partial(null);
        int value = intervals.minutes;
        if (value < 0 || value > MAX_SLEEP_MINUTES) return Metric.suspect(value);
        return intervals.partial ? Metric.partial(value) : Metric.available(value);
    }

    private Metric<Double> restingHeartRate(Instant start, Instant end) {
        if (!readAccess.canRead(HealthPermissionSpec.RESTING_HEART_RATE)) return Metric.partial(null);
        List<HealthRecordEntity> records = syncDao.recordsOverlapping("RESTING_HEART_RATE", start, end);
        if (records.isEmpty()) return Metric.missing();
        double sum = 0;
        int count = 0;
        boolean partial = false;
        for (HealthRecordEntity record : records) {
            if (record.value1 == null || !Double.isFinite(record.value1)) {
                partial = true;
                continue;
            }
            sum += record.value1;
            count++;
        }
        if (count == 0) return Metric.partial(null);
        double value = sum / count;
        if (value < MIN_HEART_RATE || value > MAX_RESTING_HEART_RATE) return Metric.suspect(value);
        return partial ? Metric.partial(value) : Metric.available(value);
    }

    private Metric<Double> averageHeartRate(Instant start, Instant end) {
        if (!readAccess.canRead(HealthPermissionSpec.HEART_RATE)) return Metric.partial(null);
        List<HealthRecordEntity> records = syncDao.recordsOverlapping("HEART_RATE", start, end);
        if (records.isEmpty()) return Metric.missing();
        double weightedSum = 0;
        long sampleCount = 0;
        boolean partial = false;
        for (HealthRecordEntity record : records) {
            if (record.value1 == null || !Double.isFinite(record.value1)
                    || record.sampleCount == null || record.sampleCount <= 0) {
                partial = true;
                continue;
            }
            weightedSum += record.value1 * record.sampleCount;
            sampleCount += record.sampleCount;
        }
        if (sampleCount == 0) return Metric.partial(null);
        double value = weightedSum / sampleCount;
        if (value < MIN_HEART_RATE || value > MAX_AVERAGE_HEART_RATE) return Metric.suspect(value);
        return partial ? Metric.partial(value) : Metric.available(value);
    }

    private Metric<Double> weight(Instant start, Instant end) {
        if (!readAccess.canRead(HealthPermissionSpec.WEIGHT)) return Metric.partial(null);
        List<HealthRecordEntity> records = syncDao.recordsOverlapping("WEIGHT", start, end);
        if (records.isEmpty()) return Metric.missing();
        HealthRecordEntity latest = null;
        boolean partial = false;
        for (HealthRecordEntity record : records) {
            if (record.value1 == null || !Double.isFinite(record.value1) || record.startTime == null) {
                partial = true;
                continue;
            }
            if (latest == null || record.startTime.isAfter(latest.startTime)) latest = record;
        }
        if (latest == null) return Metric.partial(null);
        double value = latest.value1;
        if (value < MIN_WEIGHT_KG || value > MAX_WEIGHT_KG) return Metric.suspect(value);
        return partial ? Metric.partial(value) : Metric.available(value);
    }

    private Metric<Integer> exerciseMinutes(Instant start, Instant end) {
        if (!readAccess.canRead(HealthPermissionSpec.EXERCISE)) return Metric.partial(null);
        List<WorkoutEntity> workouts = workoutDao.workoutsOverlapping(start, end);
        if (workouts.isEmpty()) return Metric.missing();
        List<Interval> intervals = new ArrayList<>();
        boolean partial = false;
        for (WorkoutEntity workout : workouts) {
            if (workout.startTime == null || workout.endTime == null || !workout.endTime.isAfter(workout.startTime)) {
                partial = true;
                continue;
            }
            Instant clippedStart = workout.startTime.isAfter(start) ? workout.startTime : start;
            Instant clippedEnd = workout.endTime.isBefore(end) ? workout.endTime : end;
            if (clippedEnd.isAfter(clippedStart)) intervals.add(new Interval(clippedStart, clippedEnd));
        }
        if (intervals.isEmpty()) return Metric.partial(null);
        int minutes = mergeIntervals(intervals);
        if (minutes < 0 || minutes > MAX_EXERCISE_MINUTES) return Metric.suspect(minutes);
        return partial ? Metric.partial(minutes) : Metric.available(minutes);
    }

    private IntervalResult mergedMinutes(List<HealthRecordEntity> records, Instant start, Instant end) {
        List<Interval> intervals = new ArrayList<>();
        boolean partial = false;
        for (HealthRecordEntity record : records) {
            if (record.startTime == null || record.endTime == null || !record.endTime.isAfter(record.startTime)) {
                partial = true;
                continue;
            }
            Instant clippedStart = record.startTime.isAfter(start) ? record.startTime : start;
            Instant clippedEnd = record.endTime.isBefore(end) ? record.endTime : end;
            if (clippedEnd.isAfter(clippedStart)) intervals.add(new Interval(clippedStart, clippedEnd));
        }
        if (intervals.isEmpty()) return new IntervalResult(null, true);
        return new IntervalResult(mergeIntervals(intervals), partial);
    }

    static int mergeIntervals(List<Interval> intervals) {
        intervals.sort(Comparator.comparing(interval -> interval.start));
        Instant currentStart = intervals.get(0).start;
        Instant currentEnd = intervals.get(0).end;
        long totalMillis = 0;
        for (int i = 1; i < intervals.size(); i++) {
            Interval next = intervals.get(i);
            if (!next.start.isAfter(currentEnd)) {
                if (next.end.isAfter(currentEnd)) currentEnd = next.end;
            } else {
                totalMillis += Duration.between(currentStart, currentEnd).toMillis();
                currentStart = next.start;
                currentEnd = next.end;
            }
        }
        totalMillis += Duration.between(currentStart, currentEnd).toMillis();
        return (int) Math.round(totalMillis / 60_000.0);
    }

    private DomainEnums.DataQuality overallQuality(DailyHealthEntity entity) {
        DomainEnums.DataQuality[] qualities = {
                entity.stepsQuality, entity.distanceQuality, entity.activeCaloriesQuality,
                entity.sleepQuality, entity.restingHeartRateQuality, entity.averageHeartRateQuality,
                entity.weightQuality, entity.exerciseMinutesQuality
        };
        boolean anyAvailable = false;
        boolean anyPartial = false;
        boolean anyMissing = false;
        for (DomainEnums.DataQuality quality : qualities) {
            if (quality == DomainEnums.DataQuality.SUSPECT) return DomainEnums.DataQuality.SUSPECT;
            if (quality == DomainEnums.DataQuality.AVAILABLE) anyAvailable = true;
            else if (quality == DomainEnums.DataQuality.PARTIAL) anyPartial = true;
            else if (quality == DomainEnums.DataQuality.MISSING) anyMissing = true;
        }
        if (anyAvailable && !anyPartial && !anyMissing) return DomainEnums.DataQuality.AVAILABLE;
        if (anyAvailable || anyPartial) return DomainEnums.DataQuality.PARTIAL;
        return DomainEnums.DataQuality.MISSING;
    }

    public static final class Interval {
        final Instant start;
        final Instant end;
        public Interval(Instant start, Instant end) { this.start = start; this.end = end; }
    }

    private static final class IntervalResult {
        final Integer minutes;
        final boolean partial;
        IntervalResult(Integer minutes, boolean partial) { this.minutes = minutes; this.partial = partial; }
    }

    private static final class Metric<T> {
        final T value;
        final DomainEnums.DataQuality quality;
        private Metric(T value, DomainEnums.DataQuality quality) { this.value = value; this.quality = quality; }
        static <T> Metric<T> available(T value) { return new Metric<>(value, DomainEnums.DataQuality.AVAILABLE); }
        static <T> Metric<T> missing() { return new Metric<>(null, DomainEnums.DataQuality.MISSING); }
        static <T> Metric<T> partial(T value) { return new Metric<>(value, DomainEnums.DataQuality.PARTIAL); }
        static <T> Metric<T> suspect(T value) { return new Metric<>(value, DomainEnums.DataQuality.SUSPECT); }
    }

    private interface LongValidator { boolean valid(long value); }
    private interface IntValidator { boolean valid(int value); }
    private interface DoubleValidator { boolean valid(double value); }
}
