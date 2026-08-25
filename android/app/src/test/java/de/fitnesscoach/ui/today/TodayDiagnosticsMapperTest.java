package de.fitnesscoach.ui.today;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.util.EnumMap;

import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.health.HealthPermissionSnapshot;
import de.fitnesscoach.health.HealthPermissionSpec;

public class TodayDiagnosticsMapperTest {

    @Test
    public void mapsAvailableMissingPartialAndSuspectWithoutInventingZero() {
        DailyHealthEntity health = new DailyHealthEntity();
        health.date = LocalDate.of(2026, 8, 25);
        health.steps = 8423L;
        health.stepsQuality = DomainEnums.DataQuality.AVAILABLE;
        health.distanceKm = null;
        health.distanceQuality = DomainEnums.DataQuality.MISSING;
        health.activeCalories = null;
        health.activeCaloriesQuality = DomainEnums.DataQuality.PARTIAL;
        health.sleepMinutes = 440;
        health.sleepQuality = DomainEnums.DataQuality.SUSPECT;
        health.restingHeartRateQuality = DomainEnums.DataQuality.MISSING;
        health.averageHeartRateQuality = DomainEnums.DataQuality.MISSING;
        health.weightQuality = DomainEnums.DataQuality.MISSING;
        health.exerciseMinutesQuality = DomainEnums.DataQuality.MISSING;

        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states = grantedStates();
        states.put(HealthPermissionSpec.ACTIVE_CALORIES, HealthPermissionSnapshot.State.DENIED);
        HealthPermissionSnapshot permissions = new HealthPermissionSnapshot(true, states);

        TodayDiagnosticsUiState state = TodayDiagnosticsMapper.map(health.date, health, null, permissions);

        assertTrue(state.hasDailyData);
        assertEquals("8423", state.metrics.get(0).value);
        assertEquals("AVAILABLE", state.metrics.get(0).quality);
        assertEquals("Unavailable", state.metrics.get(1).value);
        assertEquals("MISSING", state.metrics.get(1).quality);
        assertEquals("Unavailable", state.metrics.get(2).value);
        assertEquals("PARTIAL", state.metrics.get(2).quality);
        assertTrue(state.metrics.get(2).explanation.contains("permission"));
        assertEquals("SUSPECT", state.metrics.get(3).quality);
        assertFalse(state.metrics.get(1).value.equals("0"));
    }

    @Test
    public void mapsEmptyDayWithUsefulEmptyState() {
        HealthPermissionSnapshot permissions = new HealthPermissionSnapshot(true, grantedStates());
        TodayDiagnosticsUiState state = TodayDiagnosticsMapper.map(
                LocalDate.of(2026, 8, 24), null, null, permissions);

        assertFalse(state.hasDailyData);
        assertTrue(state.metrics.isEmpty());
        assertTrue(state.emptyMessage.contains("No aggregated health data"));
    }

    @Test
    public void reportsUnavailableHealthConnect() {
        TodayDiagnosticsUiState state = TodayDiagnosticsMapper.map(
                LocalDate.of(2026, 8, 25), null, null,
                new HealthPermissionSnapshot(false, new EnumMap<>(HealthPermissionSpec.class)));

        assertFalse(state.healthConnectAvailable);
    }

    private EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> grantedStates() {
        EnumMap<HealthPermissionSpec, HealthPermissionSnapshot.State> states =
                new EnumMap<>(HealthPermissionSpec.class);
        for (HealthPermissionSpec spec : HealthPermissionSpec.values()) {
            states.put(spec, HealthPermissionSnapshot.State.GRANTED);
        }
        return states;
    }
}
