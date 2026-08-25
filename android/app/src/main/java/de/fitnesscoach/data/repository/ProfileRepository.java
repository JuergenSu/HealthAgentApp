package de.fitnesscoach.data.repository;

import androidx.room.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fitnesscoach.data.dao.ProfileDao;
import de.fitnesscoach.data.entity.TrainingAvailabilityEntity;
import de.fitnesscoach.data.entity.UserProfileEntity;

/** Single access point for the coach-facing fitness profile and weekly availability. */
public final class ProfileRepository {
    private final ProfileDao dao;

    public ProfileRepository(ProfileDao dao) { this.dao = dao; }

    @Transaction
    public ProfileSnapshot getSnapshot() {
        UserProfileEntity profile = dao.getProfile();
        List<TrainingAvailabilityEntity> availability = dao.getAvailability();
        return new ProfileSnapshot(profile, availability == null ? Collections.emptyList() : availability);
    }

    @Transaction
    public void save(UserProfileEntity profile, List<TrainingAvailabilityEntity> availability) {
        Instant now = Instant.now();
        UserProfileEntity existing = dao.getProfile();
        profile.id = 1L;
        profile.createdAt = existing != null && existing.createdAt != null ? existing.createdAt : now;
        profile.updatedAt = now;
        dao.upsertProfile(profile);
        dao.deleteAvailability();
        for (TrainingAvailabilityEntity day : availability) dao.upsertAvailability(day);
    }

    public static final class ProfileSnapshot {
        public final UserProfileEntity profile;
        public final List<TrainingAvailabilityEntity> availability;

        public ProfileSnapshot(UserProfileEntity profile, List<TrainingAvailabilityEntity> availability) {
            this.profile = profile;
            this.availability = Collections.unmodifiableList(new ArrayList<>(availability));
        }
    }
}
