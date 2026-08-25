package de.fitnesscoach.ui.today;

import java.time.LocalDate;

public final class FitnessDiagnosticsUiState {
    public final LocalDate date;
    public final String text;
    public FitnessDiagnosticsUiState(LocalDate date, String text) { this.date=date; this.text=text; }
}
