package de.fitnesscoach.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.GoalEntity;
import de.fitnesscoach.data.repository.GoalRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GoalRepositoryTest {
    private FitnessCoachDatabase db;
    private GoalRepository repository;

    @Before public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, FitnessCoachDatabase.class).allowMainThreadQueries().build();
        repository = new GoalRepository(db);
    }

    @After public void close() { db.close(); }

    @Test public void replacingPrimaryGoalLeavesExactlyOneActiveAndAuditsChange() {
        GoalEntity first = goal("First goal");
        long firstId = repository.saveAsPrimary(first, "create", "TEST");
        GoalEntity second = goal("Second goal");
        long secondId = repository.saveAsPrimary(second, "replace", "TEST");

        assertNotEquals(firstId, secondId);
        assertEquals(1, db.goalDao().countActive());
        assertEquals(secondId, repository.getActive().id);
        assertEquals(DomainEnums.GoalStatus.PAUSED, db.goalDao().getById(firstId).status);
        assertEquals(2, db.coachingDao().getDecisions().size());
    }

    private GoalEntity goal(String title) {
        GoalEntity goal = new GoalEntity();
        goal.type = DomainEnums.GoalType.CUSTOM;
        goal.title = title;
        return goal;
    }
}
