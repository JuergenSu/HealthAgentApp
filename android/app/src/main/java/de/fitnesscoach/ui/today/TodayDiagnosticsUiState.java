package de.fitnesscoach.ui.today;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public final class TodayDiagnosticsUiState {
    public final LocalDate date;
    public final boolean healthConnectAvailable;
    public final boolean hasDailyData;
    public final String emptyMessage;
    public final List<MetricRow> metrics;
    public final Instant lastAttemptAt;
    public final Instant lastSuccessfulSyncAt;
    public final String lastSyncError;

    public TodayDiagnosticsUiState(LocalDate date,
                                   boolean healthConnectAvailable,
                                   boolean hasDailyData,
                                   String emptyMessage,
                                   List<MetricRow> metrics,
                                   Instant lastAttemptAt,
                                   Instant lastSuccessfulSyncAt,
                                   String lastSyncError) {
        this.date = date;
        this.healthConnectAvailable = healthConnectAvailable;
        this.hasDailyData = hasDailyData;
        this.emptyMessage = emptyMessage;
        this.metrics = metrics == null ? Collections.emptyList() : Collections.unmodifiableList(metrics);
        this.lastAttemptAt = lastAttemptAt;
        this.lastSuccessfulSyncAt = lastSuccessfulSyncAt;
        this.lastSyncError = lastSyncError;
    }

    public static final class MetricRow {
        public final String label;
        public final String value;
        public final String quality;
        public final String explanation;

        public MetricRow(String label, String value, String quality, String explanation) {
            this.label = label;
            this.value = value;
            this.quality = quality;
            this.explanation = explanation;
        }
    }
}
