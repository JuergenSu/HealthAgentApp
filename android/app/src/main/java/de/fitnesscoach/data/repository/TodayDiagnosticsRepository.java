package de.fitnesscoach.data.repository;

import java.time.LocalDate;

import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;

public interface TodayDiagnosticsRepository {
    DailyHealthEntity getDailyHealth(LocalDate date);
    HealthSyncStateEntity getSyncState();
}
