package de.fitnesscoach.ui.today;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;
import de.fitnesscoach.health.HealthPermissionSnapshot;
import de.fitnesscoach.health.HealthPermissionSpec;

public final class TodayDiagnosticsMapper {
    private TodayDiagnosticsMapper() {}

    public static TodayDiagnosticsUiState map(LocalDate date,
                                              DailyHealthEntity health,
                                              HealthSyncStateEntity syncState,
                                              HealthPermissionSnapshot permissions) {
        List<TodayDiagnosticsUiState.MetricRow> rows = new ArrayList<>();
        if (health != null) {
            rows.add(metric("Steps", health.steps == null ? null : String.valueOf(health.steps),
                    health.stepsQuality, permissions, HealthPermissionSpec.STEPS));
            rows.add(metric("Distance", health.distanceKm == null ? null : String.format(Locale.US, "%.2f km", health.distanceKm),
                    health.distanceQuality, permissions, HealthPermissionSpec.DISTANCE));
            rows.add(metric("Active calories", health.activeCalories == null ? null : health.activeCalories + " kcal",
                    health.activeCaloriesQuality, permissions, HealthPermissionSpec.ACTIVE_CALORIES));
            rows.add(metric("Sleep", health.sleepMinutes == null ? null : formatMinutes(health.sleepMinutes),
                    health.sleepQuality, permissions, HealthPermissionSpec.SLEEP));
            rows.add(metric("Resting heart rate", health.restingHeartRate == null ? null : String.format(Locale.US, "%.0f bpm", health.restingHeartRate),
                    health.restingHeartRateQuality, permissions, HealthPermissionSpec.RESTING_HEART_RATE));
            rows.add(metric("Average heart rate", health.averageHeartRate == null ? null : String.format(Locale.US, "%.0f bpm", health.averageHeartRate),
                    health.averageHeartRateQuality, permissions, HealthPermissionSpec.HEART_RATE));
            rows.add(metric("Weight", health.weightKg == null ? null : String.format(Locale.US, "%.1f kg", health.weightKg),
                    health.weightQuality, permissions, HealthPermissionSpec.WEIGHT));
            rows.add(metric("Exercise", health.exerciseMinutes == null ? null : formatMinutes(health.exerciseMinutes),
                    health.exerciseMinutesQuality, permissions, HealthPermissionSpec.EXERCISE));
        }

        boolean available = permissions != null && permissions.isHealthConnectAvailable();
        String emptyMessage = health == null
                ? "No aggregated health data exists for this day. Synchronize Health Connect or select an imported day."
                : null;

        return new TodayDiagnosticsUiState(
                date,
                available,
                health != null,
                emptyMessage,
                rows,
                syncState == null ? null : syncState.lastAttemptAt,
                syncState == null ? null : syncState.lastSuccessfulSyncAt,
                syncState == null ? null : syncState.lastError);
    }

    private static TodayDiagnosticsUiState.MetricRow metric(String label,
                                                            String value,
                                                            DomainEnums.DataQuality quality,
                                                            HealthPermissionSnapshot permissions,
                                                            HealthPermissionSpec permission) {
        DomainEnums.DataQuality effectiveQuality = quality == null ? DomainEnums.DataQuality.MISSING : quality;
        String displayValue = value == null ? "Unavailable" : value;
        return new TodayDiagnosticsUiState.MetricRow(
                label,
                displayValue,
                effectiveQuality.name(),
                explanation(effectiveQuality, permissions, permission));
    }

    private static String explanation(DomainEnums.DataQuality quality,
                                      HealthPermissionSnapshot permissions,
                                      HealthPermissionSpec permission) {
        if (quality == DomainEnums.DataQuality.PARTIAL && permissions != null
                && permissions.getState(permission) != HealthPermissionSnapshot.State.GRANTED) {
            return "Health Connect permission is not granted for this metric.";
        }
        switch (quality) {
            case AVAILABLE:
                return "Aggregated source data is available.";
            case SUSPECT:
                return "The source value looks implausible and was flagged without being changed.";
            case PARTIAL:
                return "Only partial information is available for this metric.";
            case MISSING:
            default:
                return "No source measurement is available for this day.";
        }
    }

    private static String formatMinutes(int minutes) {
        int hours = minutes / 60;
        int remaining = minutes % 60;
        return hours > 0 ? String.format(Locale.US, "%dh %02dm", hours, remaining) : remaining + " min";
    }
}
