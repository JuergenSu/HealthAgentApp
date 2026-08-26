package de.fitnesscoach.domain.fitness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fitnesscoach.data.dao.HealthSyncDao;
import de.fitnesscoach.data.dao.WorkoutDao;
import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.HealthDailyAggregateEntity;
import de.fitnesscoach.data.entity.HealthRecordEntity;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;
import de.fitnesscoach.data.entity.PlannedWorkoutEntity;
import de.fitnesscoach.data.entity.WorkoutEntity;
import de.fitnesscoach.health.HealthPermissionSpec;

public class DailyHealthAggregatorTest {

    private static final LocalDate DAY = LocalDate.of(2026, 8, 25);
    private static final Instant CALCULATED_AT = Instant.parse("2026-08-25T12:00:00Z");

    @Test
    public void missingMeasurementsRemainNullAndDeniedPermissionIsPartial() {
        FakeHealthSyncDao sync = new FakeHealthSyncDao();
        DailyHealthAggregator aggregator = new DailyHealthAggregator(sync, new FakeWorkoutDao(), null,
                ZoneId.of("Europe/Berlin"), spec -> spec == HealthPermissionSpec.STEPS);

        DailyHealthEntity result = aggregator.aggregateDay(DAY, CALCULATED_AT);

        assertNull(result.steps);
        assertEquals(DomainEnums.DataQuality.MISSING, result.stepsQuality);
        assertNull(result.distanceKm);
        assertEquals(DomainEnums.DataQuality.PARTIAL, result.distanceQuality);
        assertEquals(DomainEnums.DataQuality.PARTIAL, result.dataQuality);
    }

    @Test
    public void suspectAggregateIsPreservedAndFlagged() {
        FakeHealthSyncDao sync = new FakeHealthSyncDao();
        sync.aggregate(DAY, "STEPS", 150_000.0);
        DailyHealthAggregator aggregator = new DailyHealthAggregator(sync, new FakeWorkoutDao(), null,
                ZoneId.of("UTC"), spec -> true);

        DailyHealthEntity result = aggregator.aggregateDay(DAY, CALCULATED_AT);

        assertEquals(Long.valueOf(150_000L), result.steps);
        assertEquals(DomainEnums.DataQuality.SUSPECT, result.stepsQuality);
        assertEquals(DomainEnums.DataQuality.SUSPECT, result.dataQuality);
    }

    @Test
    public void heartRateUsesSampleWeightedAverage() {
        FakeHealthSyncDao sync = new FakeHealthSyncDao();
        sync.record(record("HEART_RATE", "2026-08-25T08:00:00Z", "2026-08-25T08:10:00Z", 100.0, 2));
        sync.record(record("HEART_RATE", "2026-08-25T09:00:00Z", "2026-08-25T09:10:00Z", 160.0, 6));
        DailyHealthAggregator aggregator = new DailyHealthAggregator(sync, new FakeWorkoutDao(), null,
                ZoneId.of("UTC"), spec -> true);

        DailyHealthEntity result = aggregator.aggregateDay(DAY, CALCULATED_AT);

        assertEquals(145.0, result.averageHeartRate, 0.001);
        assertEquals(DomainEnums.DataQuality.AVAILABLE, result.averageHeartRateQuality);
    }

    @Test
    public void sleepUsesCalendarDayInConfiguredZoneAndMergesOverlap() {
        FakeHealthSyncDao sync = new FakeHealthSyncDao();
        LocalDate dstDay = LocalDate.of(2026, 3, 29);
        sync.record(record("SLEEP", "2026-03-28T22:30:00Z", "2026-03-29T05:30:00Z", 420.0, null));
        sync.record(record("SLEEP", "2026-03-29T04:30:00Z", "2026-03-29T06:00:00Z", 90.0, null));
        DailyHealthAggregator aggregator = new DailyHealthAggregator(sync, new FakeWorkoutDao(), null,
                ZoneId.of("Europe/Berlin"), spec -> true);

        DailyHealthEntity result = aggregator.aggregateDay(dstDay, CALCULATED_AT);

        assertEquals(Integer.valueOf(420), result.sleepMinutes);
        assertEquals(DomainEnums.DataQuality.AVAILABLE, result.sleepQuality);
    }

    @Test
    public void exerciseOverlapsAreUnionedRatherThanDoubleCounted() {
        FakeWorkoutDao workouts = new FakeWorkoutDao();
        workouts.items.add(workout("2026-08-25T08:00:00Z", "2026-08-25T09:00:00Z"));
        workouts.items.add(workout("2026-08-25T08:30:00Z", "2026-08-25T09:30:00Z"));
        DailyHealthAggregator aggregator = new DailyHealthAggregator(new FakeHealthSyncDao(), workouts, null,
                ZoneId.of("UTC"), spec -> true);

        DailyHealthEntity result = aggregator.aggregateDay(DAY, CALCULATED_AT);

        assertEquals(Integer.valueOf(90), result.exerciseMinutes);
        assertEquals(DomainEnums.DataQuality.AVAILABLE, result.exerciseMinutesQuality);
    }

    private static HealthRecordEntity record(String type, String start, String end, Double value, Integer samples) {
        HealthRecordEntity entity = new HealthRecordEntity();
        entity.recordKey = type + ":" + start;
        entity.recordType = type;
        entity.startTime = Instant.parse(start);
        entity.endTime = Instant.parse(end);
        entity.value1 = value;
        entity.sampleCount = samples;
        return entity;
    }

    private static WorkoutEntity workout(String start, String end) {
        WorkoutEntity entity = new WorkoutEntity();
        entity.startTime = Instant.parse(start);
        entity.endTime = Instant.parse(end);
        return entity;
    }

    private static final class FakeHealthSyncDao implements HealthSyncDao {
        private final Map<String, HealthDailyAggregateEntity> aggregates = new HashMap<>();
        private final List<HealthRecordEntity> records = new ArrayList<>();
        private HealthSyncStateEntity state;

        void aggregate(LocalDate date, String metric, double value) {
            HealthDailyAggregateEntity entity = new HealthDailyAggregateEntity();
            entity.date = date;
            entity.metric = metric;
            entity.value = value;
            aggregates.put(date + "|" + metric, entity);
        }

        void record(HealthRecordEntity entity) { records.add(entity); }

        @Override public void upsertRecord(HealthRecordEntity entity) { records.add(entity); }
        @Override public void upsertRecords(List<HealthRecordEntity> entities) { records.addAll(entities); }
        @Override public void upsertAggregate(HealthDailyAggregateEntity entity) { aggregates.put(entity.date + "|" + entity.metric, entity); }
        @Override public void upsertAggregates(List<HealthDailyAggregateEntity> entities) { for (HealthDailyAggregateEntity e : entities) upsertAggregate(e); }
        @Override public void upsertState(HealthSyncStateEntity entity) { state = entity; }
        @Override public HealthSyncStateEntity getState() { return state; }
        @Override public int countRecords(String recordType) { return (int) records.stream().filter(r -> recordType.equals(r.recordType)).count(); }
        @Override public int countAggregates(String metric) { return (int) aggregates.values().stream().filter(a -> metric.equals(a.metric)).count(); }
        @Override public HealthDailyAggregateEntity getAggregate(LocalDate date, String metric) { return aggregates.get(date + "|" + metric); }
        @Override public List<HealthRecordEntity> recordsOverlapping(String recordType, Instant startInclusive, Instant endExclusive) {
            List<HealthRecordEntity> result = new ArrayList<>();
            for (HealthRecordEntity r : records) {
                if (!recordType.equals(r.recordType) || r.startTime == null) continue;
                Instant recordEnd = r.endTime == null ? r.startTime : r.endTime;
                if (r.startTime.isBefore(endExclusive) && !recordEnd.isBefore(startInclusive)) result.add(r);
            }
            return result;
        }
    }

    private static final class FakeWorkoutDao implements WorkoutDao {
        private final List<WorkoutEntity> items = new ArrayList<>();
        @Override public long insertWorkout(WorkoutEntity entity) { items.add(entity); return items.size(); }
        @Override public void updateWorkout(WorkoutEntity entity) { }
        @Override public WorkoutEntity getByExternalRecordId(String externalRecordId) { return null; }
        @Override public List<WorkoutEntity> getWorkouts() { return items; }
        @Override public List<WorkoutEntity> workoutsOverlapping(Instant startInclusive, Instant endExclusive) {
            List<WorkoutEntity> result = new ArrayList<>();
            for (WorkoutEntity w : items) {
                if (w.startTime != null && w.endTime != null && w.startTime.isBefore(endExclusive) && w.endTime.isAfter(startInclusive)) result.add(w);
            }
            return result;
        }
        @Override public long insertPlanned(PlannedWorkoutEntity entity) { return 0; }
        @Override public void updatePlanned(PlannedWorkoutEntity entity) { }
        @Override public List<PlannedWorkoutEntity> plannedRange(LocalDate from, LocalDate to) { return new ArrayList<>(); }
        @Override public PlannedWorkoutEntity getPlanned(long id) { return null; }
    }
}
