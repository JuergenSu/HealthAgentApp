package de.fitnesscoach.ui.plan;

import java.time.LocalDate;

public final class PlanDiagnosticsUiState {
    public final LocalDate weekStart;
    public final String text;

    public PlanDiagnosticsUiState(LocalDate weekStart, String text) {
        this.weekStart = weekStart;
        this.text = text;
    }
}
