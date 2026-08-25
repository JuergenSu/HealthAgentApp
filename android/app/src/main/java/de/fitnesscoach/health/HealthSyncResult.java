package de.fitnesscoach.health;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HealthSyncResult {
    private final boolean successful;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Map<String, Integer> importedCounts;
    private final String error;

    public HealthSyncResult(boolean successful, Instant startedAt, Instant completedAt,
                            Map<String, Integer> importedCounts, String error) {
        this.successful = successful;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.importedCounts = Collections.unmodifiableMap(new LinkedHashMap<>(importedCounts));
        this.error = error;
    }

    public boolean isSuccessful() { return successful; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Map<String, Integer> getImportedCounts() { return importedCounts; }
    public String getError() { return error; }
}
