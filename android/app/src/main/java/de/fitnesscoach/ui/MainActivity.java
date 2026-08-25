package de.fitnesscoach.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import de.fitnesscoach.R;
import de.fitnesscoach.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            showDestination(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_today);
        }
    }

    private void showDestination(int itemId) {
        if (itemId == R.id.nav_today) {
            setPlaceholder(R.string.nav_today, R.string.placeholder_today);
        } else if (itemId == R.id.nav_plan) {
            setPlaceholder(R.string.nav_plan, R.string.placeholder_plan);
        } else if (itemId == R.id.nav_progress) {
            setPlaceholder(R.string.nav_progress, R.string.placeholder_progress);
        } else if (itemId == R.id.nav_coach) {
            setPlaceholder(R.string.nav_coach, R.string.placeholder_coach);
        } else if (itemId == R.id.nav_profile) {
            setPlaceholder(R.string.nav_profile, R.string.placeholder_profile);
        }
    }

    private void setPlaceholder(int titleResource, int bodyResource) {
        binding.screenTitle.setText(titleResource);
        binding.screenBody.setText(bodyResource);
    }
}
