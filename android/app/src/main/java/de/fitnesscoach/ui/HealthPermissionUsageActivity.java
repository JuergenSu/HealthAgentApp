package de.fitnesscoach.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.fitnesscoach.R;

public class HealthPermissionUsageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setText(R.string.health_permission_usage_title);
        title.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_HeadlineMedium);
        container.addView(title);

        TextView body = new TextView(this);
        body.setText(R.string.health_permission_usage_body);
        body.setPadding(0, padding, 0, 0);
        body.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        container.addView(body);

        setContentView(container);
    }
}
