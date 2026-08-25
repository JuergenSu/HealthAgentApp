package de.fitnesscoach.data.db;

import static org.junit.Assert.*;
import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.Instant;
import java.time.LocalDate;
import de.fitnesscoach.data.entity.*;

@RunWith(AndroidJUnit4.class)
public class FitnessCoachDatabaseTest {
    private FitnessCoachDatabase db;

    @Before public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, FitnessCoachDatabase.class).allowMainThreadQueries().build();
    }
    @After public void tearDown() { db.close(); }

    @Test public void databaseCreatesAndCriticalEntitiesRoundTrip() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.id = 1L;
        profile.fitnessLevel = DomainEnums.FitnessLevel.REGULAR;
        profile.createdAt = Instant.parse("2026-08-25T12:00:00Z");
        profile.updatedAt = profile.createdAt;
        db.profileDao().upsertProfile(profile);
        assertEquals(DomainEnums.FitnessLevel.REGULAR, db.profileDao().getProfile().fitnessLevel);

        DailyHealthEntity health = new DailyHealthEntity();
        health.date = LocalDate.of(2026, 8, 25);
        health.sleepMinutes = null;
        health.dataQuality = DomainEnums.DataQuality.MISSING;
        health.calculatedAt = profile.createdAt;
        db.healthDao().upsert(health);
        DailyHealthEntity stored = db.healthDao().get(health.date);
        assertNull(stored.sleepMinutes);
        assertEquals(DomainEnums.DataQuality.MISSING, stored.dataQuality);

        GoalEntity goal = new GoalEntity();
        goal.type = DomainEnums.GoalType.RUN_10K_TIME;
        goal.title = "10K";
        goal.status = DomainEnums.GoalStatus.ACTIVE;
        goal.priority = 0;
        goal.createdAt = profile.createdAt;
        goal.updatedAt = profile.createdAt;
        db.goalDao().insert(goal);
        assertEquals(DomainEnums.GoalType.RUN_10K_TIME, db.goalDao().getActive().type);
    }
}