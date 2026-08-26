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
import de.fitnesscoach.data.repository.FitnessCoachRepository;
import de.fitnesscoach.data.repository.RoomFitnessCoachRepository;

public final class TodayOverviewViewModel extends AndroidViewModel {
 private final FitnessCoachRepository repo; private final FitnessCoachDatabase db; private final ExecutorService ex=Executors.newSingleThreadExecutor(); private final MutableLiveData<TodayOverviewUiState> state=new MutableLiveData<>(); private LocalDate date=LocalDate.now();
 public TodayOverviewViewModel(@NonNull Application a){super(a);db=FitnessCoachDatabase.getInstance(a);repo=new RoomFitnessCoachRepository(db);}public LiveData<TodayOverviewUiState> getState(){return state;}public void setDate(LocalDate d){date=d;refresh();}public void refresh(){LocalDate d=date;ex.execute(()->state.postValue(TodayOverviewMapper.map(d,repo.getDailyHealth(d),repo.getRecovery(d),repo.getBaseline("restingHeartRate",28),repo.getPlannedWorkouts(d,d),db.healthSyncDao().getState(),repo.getWorkouts())));}@Override protected void onCleared(){ex.shutdownNow();}
}
