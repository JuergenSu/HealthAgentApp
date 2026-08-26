package de.fitnesscoach.ui.plan;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.repository.FitnessCoachRepository;
import de.fitnesscoach.data.repository.RoomFitnessCoachRepository;

public final class PlanDiagnosticsViewModel extends AndroidViewModel {
    private final FitnessCoachRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<PlanDiagnosticsUiState> state = new MutableLiveData<>();
    private LocalDate weekStart;

    public PlanDiagnosticsViewModel(@NonNull Application app) {
        super(app);
        repo = new RoomFitnessCoachRepository(FitnessCoachDatabase.getInstance(app));
    }

    public LiveData<PlanDiagnosticsUiState> getState() { return state; }

    public void setWeek(LocalDate weekStart) {
        this.weekStart = weekStart;
        refresh();
    }

    public void refresh() {
        if (weekStart == null) return;
        LocalDate selected = weekStart;
        executor.execute(() -> state.postValue(PlanDiagnosticsMapper.map(selected, repo)));
    }

    @Override protected void onCleared() {
        executor.shutdownNow();
    }
}
