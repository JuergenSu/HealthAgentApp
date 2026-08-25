package de.fitnesscoach.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.LocalDate;

import de.fitnesscoach.data.entity.HealthDailyAggregateEntity;
import de.fitnesscoach.data.entity.HealthRecordEntity;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;

@RunWith(AndroidJUnit4.class)
public class HealthSyncPersistenceTest {
    private FitnessCoachDatabase database;

    @Before
    public void setUp() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(), FitnessCoachDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void stagingRecordsAndAggregatesAreIdempotent() {
        HealthRecordEntity record = new HealthRecordEntity();
        record.recordKey = "WEIGHT:abc";
        record.recordType = "WEIGHT";
        record.externalRecordId = "abc";
        record.value1 = 80.0;
        record.lastModifiedTime = Instant.parse("2026-08-25T10:00:00Z");
        database.healthSyncDao().upsertRecord(record);

        record.value1 = 79.5;
        database.healthSyncDao().upsertRecord(record);
        assertEquals(1, database.healthSyncDao().countRecords("WEIGHT"));

        HealthDailyAggregateEntity aggregate = new HealthDailyAggregateEntity();
        aggregate.date = LocalDate.of(2026, 8, 25);
        aggregate.metric = "STEPS";
        aggregate.value = 1000.0;
        aggregate.calculatedAt = Instant.now();
        database.healthSyncDao().upsertAggregate(aggregate);

        aggregate.value = 1200.0;
        database.healthSyncDao().upsertAggregate(aggregate);
        assertEquals(1, database.healthSyncDao().countAggregates("STEPS"));
    }

    @Test
    public void syncStatePersistsSuccessfulWatermark() {
        HealthSyncStateEntity state = new HealthSyncStateEntity();
        state.lastSuccessfulSyncAt = Instant.parse("2026-08-25T12:00:00Z");
        database.healthSyncDao().upsertState(state);

        HealthSyncStateEntity restored = database.healthSyncDao().getState();
        assertNotNull(restored);
        assertEquals(state.lastSuccessfulSyncAt, restored.lastSuccessfulSyncAt);
    }
}
