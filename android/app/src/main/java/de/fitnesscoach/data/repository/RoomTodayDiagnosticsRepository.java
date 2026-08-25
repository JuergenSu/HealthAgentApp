package de.fitnesscoach.data.repository;

import java.time.LocalDate;

import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;

public final class RoomTodayDiagnosticsRepository implements TodayDiagnosticsRepository {
    private final FitnessCoachDatabase database;

    public RoomTodayDiagnosticsRepository(FitnessCoachDatabase database) {
        this.database = database;
    }

    @Override
    public DailyHealthEntity getDailyHealth(LocalDate date) {
        return database.healthDao().get(date);
    }

    @Override
    public HealthSyncStateEntity getSyncState() {
        return database.healthSyncDao().getState();
    }
}
