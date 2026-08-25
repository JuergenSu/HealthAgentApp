package de.fitnesscoach.health;

import android.content.Context;
import android.health.connect.AggregateRecordsGroupedByPeriodResponse;
import android.health.connect.AggregateRecordsRequest;
import android.health.connect.HealthConnectException;
import android.health.connect.HealthConnectManager;
import android.health.connect.LocalTimeRangeFilter;
import android.health.connect.ReadRecordsRequestUsingFilters;
import android.health.connect.ReadRecordsResponse;
import android.health.connect.datatypes.ActiveCaloriesBurnedRecord;
import android.health.connect.datatypes.DistanceRecord;
import android.health.connect.datatypes.ExerciseSessionRecord;
import android.health.connect.datatypes.HeartRateRecord;
import android.health.connect.datatypes.Record;
import android.health.connect.datatypes.RestingHeartRateRecord;
import android.health.connect.datatypes.SleepSessionRecord;
import android.health.connect.datatypes.StepsRecord;
import android.health.connect.datatypes.WeightRecord;
import android.health.connect.datatypes.units.Energy;
import android.health.connect.datatypes.units.Length;
import android.os.OutcomeReceiver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import de.fitnesscoach.data.dao.HealthSyncDao;
import de.fitnesscoach.data.dao.WorkoutDao;
import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.HealthDailyAggregateEntity;
import de.fitnesscoach.data.entity.HealthRecordEntity;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;
import de.fitnesscoach.data.entity.WorkoutEntity;
import de.fitnesscoach.domain.fitness.DailyHealthAggregator;

/** Blocking synchronization facade. Call only from a background thread. */
public class HealthSyncService {
    private static final long CALLBACK_TIMEOUT_SECONDS = 30;
    private static final int PAGE_SIZE = 5000;
    private static final int DEFAULT_HISTORY_DAYS = 30;
    private static final int EXTENDED_HISTORY_DAYS = 365;
    private static final int INCREMENTAL_OVERLAP_HOURS = 24;

    private final HealthConnectManager manager;
    private final HealthPermissionManager permissions;
    private final HealthSyncDao syncDao;
    private final WorkoutDao workoutDao;
    private final ZoneId zoneId;
    private final DailyHealthAggregator dailyHealthAggregator;

    public HealthSyncService(Context context) {
        Context app = context.getApplicationContext();
        this.manager = app.getSystemService(HealthConnectManager.class);
        this.permissions = new HealthPermissionManager(app);
        FitnessCoachDatabase database = FitnessCoachDatabase.getInstance(app);
        this.syncDao = database.healthSyncDao();
        this.workoutDao = database.workoutDao();
        this.zoneId = ZoneId.systemDefault();
        this.dailyHealthAggregator = new DailyHealthAggregator(
                syncDao, workoutDao, database.healthDao(), zoneId, permissions::canRead);
    }

    HealthSyncService(HealthConnectManager manager, HealthPermissionManager permissions,
                      HealthSyncDao syncDao, WorkoutDao workoutDao, ZoneId zoneId) {
        this(manager, permissions, syncDao, workoutDao, zoneId, null);
    }

    HealthSyncService(HealthConnectManager manager, HealthPermissionManager permissions,
                      HealthSyncDao syncDao, WorkoutDao workoutDao, ZoneId zoneId,
                      DailyHealthAggregator dailyHealthAggregator) {
        this.manager = manager;
        this.permissions = permissions;
        this.syncDao = syncDao;
        this.workoutDao = workoutDao;
        this.zoneId = zoneId;
        this.dailyHealthAggregator = dailyHealthAggregator;
    }

    public HealthSyncResult sync() {
        Instant started = Instant.now();
        Map<String, Integer> counts = new LinkedHashMap<>();
        HealthSyncStateEntity previous = syncDao.getState();
        HealthSyncStateEntity state = previous != null ? previous : new HealthSyncStateEntity();
        state.lastAttemptAt = started;
        state.lastError = null;
        syncDao.upsertState(state);

        if (manager == null || !permissions.isHealthConnectAvailable()) {
            return fail(state, started, counts, "Health Connect is unavailable");
        }

        Instant end = Instant.now();
        Instant from = determineStart(previous, end);
        if (state.initialImportStart == null) state.initialImportStart = from;

        try {
            if (permissions.canRead(HealthPermissionSpec.STEPS)) {
                counts.put("steps", aggregateSteps(from, end));
            }
            if (permissions.canRead(HealthPermissionSpec.DISTANCE)) {
                counts.put("distance", aggregateDistance(from, end));
            }
            if (permissions.canRead(HealthPermissionSpec.ACTIVE_CALORIES)) {
                counts.put("activeCalories", aggregateActiveCalories(from, end));
            }
            if (permissions.canRead(HealthPermissionSpec.HEART_RATE)) {
                counts.put("heartRate", cacheRecords(readAll(HeartRateRecord.class, from, end), "HEART_RATE"));
            }
            if (permissions.canRead(HealthPermissionSpec.RESTING_HEART_RATE)) {
                counts.put("restingHeartRate", cacheRecords(readAll(RestingHeartRateRecord.class, from, end), "RESTING_HEART_RATE"));
            }
            if (permissions.canRead(HealthPermissionSpec.SLEEP)) {
                counts.put("sleep", cacheRecords(readAll(SleepSessionRecord.class, from, end), "SLEEP"));
            }
            if (permissions.canRead(HealthPermissionSpec.WEIGHT)) {
                counts.put("weight", cacheRecords(readAll(WeightRecord.class, from, end), "WEIGHT"));
            }
            if (permissions.canRead(HealthPermissionSpec.EXERCISE)) {
                counts.put("exercise", importWorkouts(readAll(ExerciseSessionRecord.class, from, end)));
            }

            if (dailyHealthAggregator != null) {
                dailyHealthAggregator.aggregateRange(
                        LocalDateTime.ofInstant(from, zoneId).toLocalDate(),
                        LocalDateTime.ofInstant(end, zoneId).toLocalDate(),
                        Instant.now());
            }

            Instant completed = Instant.now();
            state.lastSuccessfulSyncAt = completed;
            state.lastError = null;
            syncDao.upsertState(state);
            return new HealthSyncResult(true, started, completed, counts, null);
        } catch (Exception error) {
            return fail(state, started, counts, sanitizeError(error));
        }
    }

    private Instant determineStart(HealthSyncStateEntity previous, Instant end) {
        if (previous != null && previous.lastSuccessfulSyncAt != null) {
            return previous.lastSuccessfulSyncAt.minus(INCREMENTAL_OVERLAP_HOURS, ChronoUnit.HOURS);
        }
        boolean historyGranted = permissions.getSnapshot().canRead(HealthPermissionSpec.HISTORY_READ);
        return end.minus(historyGranted ? EXTENDED_HISTORY_DAYS : DEFAULT_HISTORY_DAYS, ChronoUnit.DAYS);
    }

    private HealthSyncResult fail(HealthSyncStateEntity state, Instant started,
                                  Map<String, Integer> counts, String error) {
        state.lastError = error;
        syncDao.upsertState(state);
        return new HealthSyncResult(false, started, Instant.now(), counts, error);
    }

    private String sanitizeError(Exception error) {
        String message = error.getMessage();
        return error.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }

    private int cacheRecords(List<? extends Record> records, String type) {
        List<HealthRecordEntity> entities = new ArrayList<>();
        for (Record record : records) entities.add(HealthRecordMapper.map(record, type));
        if (!entities.isEmpty()) syncDao.upsertRecords(entities);
        return entities.size();
    }

    private int importWorkouts(List<ExerciseSessionRecord> records) {
        for (ExerciseSessionRecord record : records) {
            String externalId = record.getMetadata().getId();
            WorkoutEntity entity = workoutDao.getByExternalRecordId(externalId);
            boolean existing = entity != null;
            if (!existing) entity = new WorkoutEntity();
            entity.sportType = DomainEnums.SportType.OTHER;
            entity.workoutType = DomainEnums.WorkoutType.OTHER;
            entity.startTime = record.getStartTime();
            entity.endTime = record.getEndTime();
            entity.durationMinutes = (int) ChronoUnit.MINUTES.between(record.getStartTime(), record.getEndTime());
            entity.source = record.getMetadata().getDataOrigin().getPackageName();
            entity.externalRecordId = externalId;
            entity.status = DomainEnums.WorkoutStatus.COMPLETED;
            if (entity.createdAt == null) entity.createdAt = Instant.now();
            if (existing) workoutDao.updateWorkout(entity); else workoutDao.insertWorkout(entity);
        }
        return records.size();
    }

    private int aggregateSteps(Instant from, Instant end) throws Exception {
        LocalDateTime startLocal = LocalDateTime.ofInstant(from, zoneId);
        LocalDateTime endLocal = LocalDateTime.ofInstant(end, zoneId);
        AggregateRecordsRequest<Long> request = new AggregateRecordsRequest.Builder<Long>(
                new LocalTimeRangeFilter.Builder().setStartTime(startLocal).setEndTime(endLocal).build())
                .addAggregationType(StepsRecord.STEPS_COUNT_TOTAL).build();
        List<AggregateRecordsGroupedByPeriodResponse<Long>> rows = aggregateByDay(request);
        List<HealthDailyAggregateEntity> entities = new ArrayList<>();
        for (AggregateRecordsGroupedByPeriodResponse<Long> row : rows) {
            Long value = row.get(StepsRecord.STEPS_COUNT_TOTAL);
            if (value != null) entities.add(aggregate(row.getStartTime(), "STEPS", value.doubleValue()));
        }
        if (!entities.isEmpty()) syncDao.upsertAggregates(entities);
        return entities.size();
    }

    private int aggregateDistance(Instant from, Instant end) throws Exception {
        LocalDateTime startLocal = LocalDateTime.ofInstant(from, zoneId);
        LocalDateTime endLocal = LocalDateTime.ofInstant(end, zoneId);
        AggregateRecordsRequest<Length> request = new AggregateRecordsRequest.Builder<Length>(
                new LocalTimeRangeFilter.Builder().setStartTime(startLocal).setEndTime(endLocal).build())
                .addAggregationType(DistanceRecord.DISTANCE_TOTAL).build();
        List<AggregateRecordsGroupedByPeriodResponse<Length>> rows = aggregateByDay(request);
        List<HealthDailyAggregateEntity> entities = new ArrayList<>();
        for (AggregateRecordsGroupedByPeriodResponse<Length> row : rows) {
            Length value = row.get(DistanceRecord.DISTANCE_TOTAL);
            if (value != null) entities.add(aggregate(row.getStartTime(), "DISTANCE_METERS", value.getInMeters()));
        }
        if (!entities.isEmpty()) syncDao.upsertAggregates(entities);
        return entities.size();
    }

    private int aggregateActiveCalories(Instant from, Instant end) throws Exception {
        LocalDateTime startLocal = LocalDateTime.ofInstant(from, zoneId);
        LocalDateTime endLocal = LocalDateTime.ofInstant(end, zoneId);
        AggregateRecordsRequest<Energy> request = new AggregateRecordsRequest.Builder<Energy>(
                new LocalTimeRangeFilter.Builder().setStartTime(startLocal).setEndTime(endLocal).build())
                .addAggregationType(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL).build();
        List<AggregateRecordsGroupedByPeriodResponse<Energy>> rows = aggregateByDay(request);
        List<HealthDailyAggregateEntity> entities = new ArrayList<>();
        for (AggregateRecordsGroupedByPeriodResponse<Energy> row : rows) {
            Energy value = row.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL);
            if (value != null) entities.add(aggregate(row.getStartTime(), "ACTIVE_CALORIES", value.getInCalories()));
        }
        if (!entities.isEmpty()) syncDao.upsertAggregates(entities);
        return entities.size();
    }

    private HealthDailyAggregateEntity aggregate(LocalDateTime start, String metric, double value) {
        HealthDailyAggregateEntity entity = new HealthDailyAggregateEntity();
        entity.date = start.toLocalDate();
        entity.metric = metric;
        entity.value = value;
        entity.calculatedAt = Instant.now();
        return entity;
    }

    private <T> List<AggregateRecordsGroupedByPeriodResponse<T>> aggregateByDay(
            AggregateRecordsRequest<T> request) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<AggregateRecordsGroupedByPeriodResponse<T>>> result = new AtomicReference<>();
        AtomicReference<HealthConnectException> error = new AtomicReference<>();
        manager.aggregateGroupByPeriod(request, Period.ofDays(1), Runnable::run,
                new OutcomeReceiver<List<AggregateRecordsGroupedByPeriodResponse<T>>, HealthConnectException>() {
                    @Override public void onResult(List<AggregateRecordsGroupedByPeriodResponse<T>> value) {
                        result.set(value); latch.countDown();
                    }
                    @Override public void onError(HealthConnectException value) {
                        error.set(value); latch.countDown();
                    }
                });
        await(latch);
        if (error.get() != null) throw error.get();
        return result.get() == null ? new ArrayList<>() : result.get();
    }

    private <T extends Record> List<T> readAll(Class<T> type, Instant from, Instant end) throws Exception {
        List<T> all = new ArrayList<>();
        long pageToken = -1;
        do {
            ReadRecordsRequestUsingFilters.Builder<T> builder = new ReadRecordsRequestUsingFilters.Builder<>(type)
                    .setTimeRangeFilter(new android.health.connect.TimeInstantRangeFilter.Builder()
                            .setStartTime(from).setEndTime(end).build())
                    .setPageSize(PAGE_SIZE)
                    .setAscending(true);
            if (pageToken != -1) builder.setPageToken(pageToken);
            ReadRecordsResponse<T> response = readPage(builder.build());
            all.addAll(response.getRecords());
            pageToken = response.getNextPageToken();
        } while (pageToken != -1);
        return all;
    }

    private <T extends Record> ReadRecordsResponse<T> readPage(ReadRecordsRequestUsingFilters<T> request)
            throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ReadRecordsResponse<T>> result = new AtomicReference<>();
        AtomicReference<HealthConnectException> error = new AtomicReference<>();
        manager.readRecords(request, Runnable::run,
                new OutcomeReceiver<ReadRecordsResponse<T>, HealthConnectException>() {
                    @Override public void onResult(ReadRecordsResponse<T> value) { result.set(value); latch.countDown(); }
                    @Override public void onError(HealthConnectException value) { error.set(value); latch.countDown(); }
                });
        await(latch);
        if (error.get() != null) throw error.get();
        return result.get();
    }

    private void await(CountDownLatch latch) throws Exception {
        if (!latch.await(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Health Connect callback timed out");
        }
    }
}
