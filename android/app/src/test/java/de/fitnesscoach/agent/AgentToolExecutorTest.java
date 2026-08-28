package de.fitnesscoach.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.PlannedWorkoutEntity;
import de.fitnesscoach.data.entity.TrainingAvailabilityEntity;

public class AgentToolExecutorTest {

    @Test
    public void unknownToolFailsClosed() throws JSONException {
        JSONObject result = new AgentToolExecutor(new AgentTestRepository())
                .execute("read_entire_room_database", new JSONObject());

        assertFalse(result.getBoolean("success"));
        assertEquals("REJECTED", result.getString("code"));
    }

    @Test
    public void syntacticallyValidRescheduleCanBeRejectedLocally() throws JSONException {
        AgentTestRepository repo = new AgentTestRepository();
        LocalDate monday = LocalDate.of(2026, 8, 24);

        PlannedWorkoutEntity workout = new PlannedWorkoutEntity();
        workout.id = 1;
        workout.date = monday;
        workout.workoutType = DomainEnums.WorkoutType.EASY;
        workout.status = DomainEnums.WorkoutStatus.PLANNED;
        workout.version = 1;
        repo.planned.add(workout);

        TrainingAvailabilityEntity unavailable = new TrainingAvailabilityEntity();
        unavailable.dayOfWeek = 2;
        unavailable.available = false;
        repo.availability.add(unavailable);

        JSONObject args = new JSONObject()
                .put("workoutId", 1)
                .put("date", monday.plusDays(1).toString());
        JSONObject result = new AgentToolExecutor(repo).execute("reschedule_workout", args);

        assertFalse(result.getBoolean("success"));
        assertEquals(monday, workout.date);
    }

    @Test
    public void successfulModifyCreatesAuditRecord() throws JSONException {
        AgentTestRepository repo = new AgentTestRepository();
        LocalDate date = LocalDate.of(2026, 8, 24);

        TrainingAvailabilityEntity availability = new TrainingAvailabilityEntity();
        availability.dayOfWeek = date.getDayOfWeek().getValue();
        availability.available = true;
        availability.maxDurationMinutes = 60;
        repo.availability.add(availability);

        PlannedWorkoutEntity workout = new PlannedWorkoutEntity();
        workout.id = 2;
        workout.date = date;
        workout.workoutType = DomainEnums.WorkoutType.EASY;
        workout.status = DomainEnums.WorkoutStatus.PLANNED;
        workout.version = 1;
        repo.planned.add(workout);

        JSONObject result = new AgentToolExecutor(repo).execute(
                "modify_workout",
                new JSONObject().put("workoutId", 2).put("durationMinutes", 45));

        assertTrue(result.getBoolean("success"));
        assertEquals(Integer.valueOf(45), workout.plannedDurationMinutes);
        assertEquals(DomainEnums.WorkoutStatus.ADAPTED, workout.status);
        assertEquals(1, repo.decisions.size());
    }

    @Test
    public void adjacentIntenseRescheduleIsRejected() throws JSONException {
        AgentTestRepository repo = new AgentTestRepository();
        LocalDate date = LocalDate.of(2026, 8, 24);

        for (int dayOfWeek : new int[]{1, 2}) {
            TrainingAvailabilityEntity availability = new TrainingAvailabilityEntity();
            availability.dayOfWeek = dayOfWeek;
            availability.available = true;
            availability.maxDurationMinutes = 90;
            repo.availability.add(availability);
        }

        PlannedWorkoutEntity first = new PlannedWorkoutEntity();
        first.id = 1;
        first.date = date;
        first.workoutType = DomainEnums.WorkoutType.INTERVAL;
        first.status = DomainEnums.WorkoutStatus.PLANNED;
        repo.planned.add(first);

        PlannedWorkoutEntity second = new PlannedWorkoutEntity();
        second.id = 2;
        second.date = date.plusDays(3);
        second.workoutType = DomainEnums.WorkoutType.LONG;
        second.status = DomainEnums.WorkoutStatus.PLANNED;
        repo.planned.add(second);

        JSONObject result = new AgentToolExecutor(repo).execute(
                "reschedule_workout",
                new JSONObject().put("workoutId", 2).put("date", date.plusDays(1).toString()));

        assertFalse(result.getBoolean("success"));
        assertEquals(date.plusDays(3), second.date);
    }
}
