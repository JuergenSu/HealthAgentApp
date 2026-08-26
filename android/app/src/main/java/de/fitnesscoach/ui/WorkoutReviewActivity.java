package de.fitnesscoach.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.PlannedWorkoutEntity;
import de.fitnesscoach.data.entity.WorkoutEntity;
import de.fitnesscoach.data.repository.FitnessCoachRepository;
import de.fitnesscoach.data.repository.RoomFitnessCoachRepository;
import de.fitnesscoach.databinding.ActivityWorkoutReviewBinding;
import de.fitnesscoach.domain.fitness.FitnessIntelligenceService;

public class WorkoutReviewActivity extends AppCompatActivity {
 public static final String EXTRA_WORKOUT_ID="workout_id";
 private ActivityWorkoutReviewBinding binding; private ExecutorService ex=Executors.newSingleThreadExecutor(); private FitnessCoachRepository repo; private WorkoutEntity workout; private PlannedWorkoutEntity planned;
 @Override protected void onCreate(Bundle s){super.onCreate(s);binding=ActivityWorkoutReviewBinding.inflate(getLayoutInflater());setContentView(binding.getRoot());repo=new RoomFitnessCoachRepository(FitnessCoachDatabase.getInstance(this));long id=getIntent().getLongExtra(EXTRA_WORKOUT_ID,-1);binding.workoutReviewSave.setEnabled(false);binding.workoutReviewSkip.setOnClickListener(v->finish());binding.workoutReviewSave.setOnClickListener(v->save());ex.execute(()->load(id));}
 private void load(long id){for(WorkoutEntity w:repo.getWorkouts())if(w.id==id){workout=w;break;}if(workout!=null&&workout.plannedWorkoutId!=null&&workout.startTime!=null){LocalDate d=workout.startTime.atZone(ZoneId.systemDefault()).toLocalDate();List<PlannedWorkoutEntity> list=repo.getPlannedWorkouts(d,d);for(PlannedWorkoutEntity p:list)if(p.id==workout.plannedWorkoutId){planned=p;break;}}runOnUiThread(()->render());}
 private void render(){if(workout==null){binding.workoutReviewComparison.setText("Workout unavailable.");return;}StringBuilder b=new StringBuilder();b.append("ACTUAL\nDuration: ").append(val(workout.durationMinutes," min")).append("\nDistance: ").append(val(workout.distanceKm," km")).append("\nAverage HR: ").append(val(workout.averageHeartRate," bpm")).append("\nPace: ").append(workout.averagePaceSecPerKm==null?"unavailable":pace(workout.averagePaceSecPerKm)+" /km").append("\n\nPLANNED\n");if(planned==null)b.append("No matched planned workout.");else b.append("Duration: ").append(val(planned.plannedDurationMinutes," min")).append("\nDistance: ").append(val(planned.plannedDistanceKm," km")).append("\nHR target: ").append(planned.targetHeartRateMin==null||planned.targetHeartRateMax==null?"unavailable":fmt(planned.targetHeartRateMin)+"–"+fmt(planned.targetHeartRateMax)+" bpm").append("\nPace target: ").append(planned.targetPaceMinSecKm==null||planned.targetPaceMaxSecKm==null?"unavailable":pace(planned.targetPaceMinSecKm)+"–"+pace(planned.targetPaceMaxSecKm)+" /km");binding.workoutReviewComparison.setText(b.toString());binding.workoutReviewRpe.setText(workout.rpe==null?"":String.valueOf(workout.rpe));binding.workoutReviewNote.setText(workout.reviewNote==null?"":workout.reviewNote);binding.workoutReviewSave.setEnabled(true);}
 private void save(){binding.workoutReviewError.setVisibility(View.GONE);String raw=binding.workoutReviewRpe.getText().toString().trim();Integer rpe=null;if(!raw.isEmpty()){try{rpe=Integer.valueOf(raw);}catch(Exception e){error("RPE must be a number from 1 to 10.");return;}if(rpe<1||rpe>10){error("RPE must be between 1 and 10.");return;}}workout.rpe=rpe;String note=binding.workoutReviewNote.getText().toString().trim();workout.reviewNote=note.isEmpty()?null:note;binding.workoutReviewSave.setEnabled(false);ex.execute(()->{repo.updateWorkout(workout);if(workout.startTime!=null)new FitnessIntelligenceService(repo).refresh(workout.startTime.atZone(ZoneId.systemDefault()).toLocalDate());runOnUiThread(this::finish);});}
 private void error(String m){binding.workoutReviewError.setText(m);binding.workoutReviewError.setVisibility(View.VISIBLE);}private String val(Object v,String unit){return v==null?"unavailable":v+unit;}private String fmt(double v){return String.format(Locale.ROOT,"%.1f",v);}private String pace(int s){return(s/60)+":"+String.format(Locale.ROOT,"%02d",s%60);}@Override protected void onDestroy(){super.onDestroy();ex.shutdownNow();}
}
