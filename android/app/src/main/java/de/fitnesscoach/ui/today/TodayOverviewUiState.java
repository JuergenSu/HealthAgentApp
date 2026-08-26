package de.fitnesscoach.ui.today;

import java.time.LocalDate;

public final class TodayOverviewUiState {
    public final LocalDate date;
    public final String summary;
    public final Long reviewableWorkoutId;
    public TodayOverviewUiState(LocalDate date, String summary, Long reviewableWorkoutId) {
        this.date = date; this.summary = summary; this.reviewableWorkoutId = reviewableWorkoutId;
    }
}
