package de.fitnesscoach.ui.today;

import android.app.Application;import androidx.annotation.NonNull;import androidx.lifecycle.*;import java.time.LocalDate;import java.util.concurrent.*;import de.fitnesscoach.data.db.FitnessCoachDatabase;import de.fitnesscoach.data.repository.*;import de.fitnesscoach.domain.fitness.FitnessIntelligenceService;

public final class FitnessDiagnosticsViewModel extends AndroidViewModel {
 private final FitnessCoachRepository repo;private final FitnessIntelligenceService service;private final ExecutorService executor=Executors.newSingleThreadExecutor();private final MutableLiveData<FitnessDiagnosticsUiState> state=new MutableLiveData<>();private LocalDate selectedDate=LocalDate.now();
 public FitnessDiagnosticsViewModel(@NonNull Application app){super(app);repo=new RoomFitnessCoachRepository(FitnessCoachDatabase.getInstance(app));service=new FitnessIntelligenceService(repo);}
 public LiveData<FitnessDiagnosticsUiState> getState(){return state;}public void selectDate(LocalDate d){selectedDate=d;refresh();}public void refresh(){LocalDate d=selectedDate;executor.execute(()->state.postValue(FitnessDiagnosticsMapper.map(d,repo)));}public void recalculate(){LocalDate d=selectedDate;executor.execute(()->{service.refresh(d);state.postValue(FitnessDiagnosticsMapper.map(d,repo));});}@Override protected void onCleared(){executor.shutdownNow();}
}
