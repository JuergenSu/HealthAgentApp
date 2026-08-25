package de.fitnesscoach.ui.today;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.DailyHealthEntity;
import de.fitnesscoach.data.entity.HealthSyncStateEntity;
import de.fitnesscoach.data.repository.RoomTodayDiagnosticsRepository;
import de.fitnesscoach.data.repository.TodayDiagnosticsRepository;
import de.fitnesscoach.health.HealthPermissionManager;
import de.fitnesscoach.health.HealthPermissionSnapshot;

public final class TodayDiagnosticsViewModel extends AndroidViewModel {
    private final TodayDiagnosticsRepository repository;
    private final HealthPermissionManager permissionManager;
    private final ExecutorService executor;
    private final MutableLiveData<TodayDiagnosticsUiState> state = new MutableLiveData<>();
    private LocalDate selectedDate = LocalDate.now();

    public TodayDiagnosticsViewModel(@NonNull Application application) {
        this(application,
                new RoomTodayDiagnosticsRepository(FitnessCoachDatabase.getInstance(application)),
                new HealthPermissionManager(application),
                Executors.newSingleThreadExecutor());
    }

    TodayDiagnosticsViewModel(@NonNull Application application,
                              TodayDiagnosticsRepository repository,
                              HealthPermissionManager permissionManager,
                              ExecutorService executor) {
        super(application);
        this.repository = repository;
        this.permissionManager = permissionManager;
        this.executor = executor;
    }

    public LiveData<TodayDiagnosticsUiState> getState() {
        return state;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void refresh() {
        load(selectedDate);
    }

    public void previousDay() {
        selectedDate = selectedDate.minusDays(1);
        load(selectedDate);
    }

    public void nextDay() {
        LocalDate today = LocalDate.now();
        if (selectedDate.isBefore(today)) {
            selectedDate = selectedDate.plusDays(1);
        }
        load(selectedDate);
    }

    private void load(LocalDate date) {
        executor.execute(() -> {
            DailyHealthEntity health = repository.getDailyHealth(date);
            HealthSyncStateEntity sync = repository.getSyncState();
            HealthPermissionSnapshot permissions = permissionManager.getSnapshot();
            state.postValue(TodayDiagnosticsMapper.map(date, health, sync, permissions));
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
