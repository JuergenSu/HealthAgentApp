package de.fitnesscoach.ui.coach;
import java.util.*;
public final class CoachUiState{public final List<CoachMessage>messages;public final boolean loading;public final String error;public final boolean validationError;public final List<String>suggestedActions;public final String debugContext;public CoachUiState(List<CoachMessage>m,boolean l,String e,boolean v,List<String>a,String d){messages=List.copyOf(m);loading=l;error=e;validationError=v;suggestedActions=List.copyOf(a);debugContext=d;}}
