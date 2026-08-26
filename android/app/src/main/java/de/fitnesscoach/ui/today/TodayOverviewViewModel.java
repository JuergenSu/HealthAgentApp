package de.fitnesscoach.ui.today;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.*;
import de.fitnesscoach.data.repository.*;

public final class TodayOverviewViewModel extends AndroidViewModel {
 private final FitnessCoachRepository repo; private final FitnessCoachDatabase db; private final ExecutorService ex=Executors.newSingleThreadExecutor(); private final MutableLiveData<TodayOverviewUiState> state=new MutableLiveData<>(); private LocalDate date=LocalDate.now();
 public TodayOverviewViewModel(@NonNull Application a){super(a);db=FitnessCoachDatabase.getInstance(a);repo=new RoomFitnessCoachRepository(db);}public LiveData<TodayOverviewUiState> getState(){return state;}public void setDate(LocalDate d){date=d;refresh();}public void refresh(){LocalDate d=date;ex.execute(()->state.postValue(load(d)));}
 private TodayOverviewUiState load(LocalDate d){DailyHealthEntity h=repo.getDailyHealth(d);RecoveryEntity r=repo.getRecovery(d);BaselineEntity hrBase=repo.getBaseline("restingHeartRate",28);List<PlannedWorkoutEntity> planned=repo.getPlannedWorkouts(d,d);HealthSyncStateEntity sync=db.healthSyncDao().getState();WorkoutEntity review=null;for(WorkoutEntity w:repo.getWorkouts()){if(w.startTime==null)continue;LocalDate wd=w.startTime.atZone(ZoneId.systemDefault()).toLocalDate();if(wd.equals(d)&&w.status==DomainEnums.WorkoutStatus.COMPLETED){review=w;break;}}
 StringBuilder b=new StringBuilder();b.append("Recovery\n");if(r==null||r.score==null)b.append("Unavailable\nConfidence: unavailable\nRecommendation: unavailable\n");else b.append(r.score).append("/100 · ").append(r.recommendation).append("\nConfidence: ").append(r.confidence).append("\n");b.append("\nHealth\nSleep: ").append(h==null||h.sleepMinutes==null?"unavailable":formatMinutes(h.sleepMinutes)).append("\nResting HR: ").append(h==null||h.restingHeartRate==null?"unavailable":fmt(h.restingHeartRate)+" bpm");if(h!=null&&h.restingHeartRate!=null&&hrBase!=null&&hrBase.value!=null)b.append(" (Δ ").append(signed(h.restingHeartRate-hrBase.value)).append(" bpm vs 28d baseline)");b.append("\n\nToday's plan\n");if(planned.isEmpty())b.append("No workout planned.");else{PlannedWorkoutEntity p=planned.get(0);b.append(p.title==null?p.workoutType:p.title).append(" · ").append(p.status).append(" · v").append(p.version).append("\nDuration: ").append(p.plannedDurationMinutes==null?"unavailable":p.plannedDurationMinutes+" min").append("\nDistance: ").append(p.plannedDistanceKm==null?"unavailable":fmt(p.plannedDistanceKm)+" km").append("\nHR target: ").append(p.targetHeartRateMin==null||p.targetHeartRateMax==null?"unavailable":fmt(p.targetHeartRateMin)+"–"+fmt(p.targetHeartRateMax)+" bpm").append("\nPace target: ").append(p.targetPaceMinSecKm==null||p.targetPaceMaxSecKm==null?"unavailable":pace(p.targetPaceMinSecKm)+"–"+pace(p.targetPaceMaxSecKm)+" /km").append("\n").append(p.description==null?"":p.description).append("\nReason: ").append(reason(r,p));}
 b.append("\n\nHealth sync: ").append(sync==null||sync.lastSuccessfulSyncAt==null?"never":DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault()).format(sync.lastSuccessfulSyncAt));return new TodayOverviewUiState(d,b.toString(),review==null?null:review.id);}
 private String reason(RecoveryEntity r,PlannedWorkoutEntity p){if(p.status==DomainEnums.WorkoutStatus.ADAPTED)return"Plan adapted from deterministic recovery rules.";if(r==null||r.recommendation==null)return"No recovery assessment available; plan remains conservative.";return"Deterministic recommendation from recovery: "+r.recommendation+".";}private String formatMinutes(int m){return(m/60)+" h "+(m%60)+" min";}private String fmt(double v){return String.format(Locale.ROOT,"%.1f",v);}private String signed(double v){return(v>=0?"+":"")+fmt(v);}private String pace(int s){return(s/60)+":"+String.format(Locale.ROOT,"%02d",s%60);}@Override protected void onCleared(){ex.shutdownNow();}
}
