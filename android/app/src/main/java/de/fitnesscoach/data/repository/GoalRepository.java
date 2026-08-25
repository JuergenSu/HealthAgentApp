package de.fitnesscoach.data.repository;

import java.time.Instant;
import java.util.List;

import de.fitnesscoach.data.db.FitnessCoachDatabase;
import de.fitnesscoach.data.entity.CoachDecisionEntity;
import de.fitnesscoach.data.entity.DomainEnums;
import de.fitnesscoach.data.entity.GoalEntity;

/** Domain service enforcing the MVP invariant of exactly zero-or-one ACTIVE primary goal. */
public final class GoalRepository {
    private final FitnessCoachDatabase db;

    public GoalRepository(FitnessCoachDatabase db) { this.db = db; }

    public GoalEntity getActive() { return db.goalDao().getActive(); }
    public List<GoalEntity> getAll() { return db.goalDao().getAll(); }

    public long saveAsPrimary(GoalEntity incoming, String reason, String source) {
        final long[] result = new long[1];
        db.runInTransaction(() -> {
            Instant now = Instant.now();
            GoalEntity before = incoming.id > 0 ? db.goalDao().getById(incoming.id) : db.goalDao().getActive();
            if (incoming.id == 0) {
                incoming.createdAt = now;
                incoming.updatedAt = now;
                incoming.priority = 0;
                incoming.status = DomainEnums.GoalStatus.ACTIVE;
                incoming.id = db.goalDao().insert(incoming);
            } else {
                GoalEntity persisted = db.goalDao().getById(incoming.id);
                incoming.createdAt = persisted != null && persisted.createdAt != null ? persisted.createdAt : now;
                incoming.updatedAt = now;
                incoming.priority = 0;
                incoming.status = DomainEnums.GoalStatus.ACTIVE;
                db.goalDao().update(incoming);
            }
            db.goalDao().setOtherActiveGoalsStatus(incoming.id, DomainEnums.GoalStatus.PAUSED, now);
            if (db.goalDao().countActive() > 1) throw new IllegalStateException("More than one active primary goal");
            audit(before, incoming, reason, source, now);
            result[0] = incoming.id;
        });
        return result[0];
    }

    public void changeStatus(long id, DomainEnums.GoalStatus status, String reason, String source) {
        db.runInTransaction(() -> {
            GoalEntity goal = db.goalDao().getById(id);
            if (goal == null) return;
            String before = serialize(goal);
            goal.status = status;
            goal.updatedAt = Instant.now();
            db.goalDao().update(goal);
            CoachDecisionEntity d = decision(id, before, serialize(goal), reason, source, goal.updatedAt);
            db.coachingDao().insertDecision(d);
        });
    }

    private void audit(GoalEntity before, GoalEntity after, String reason, String source, Instant now) {
        db.coachingDao().insertDecision(decision(after.id, before == null ? null : serialize(before), serialize(after), reason, source, now));
    }

    private CoachDecisionEntity decision(long id, String before, String after, String reason, String source, Instant now) {
        CoachDecisionEntity d = new CoachDecisionEntity();
        d.timestamp = now; d.decisionType = DomainEnums.CoachDecisionType.CHANGE_GOAL; d.targetEntityId = id;
        d.beforeJson = before; d.afterJson = after; d.reasonCode = "PRIMARY_GOAL_CHANGED";
        d.reasonText = reason == null ? "Primary goal changed" : reason; d.source = source == null ? "USER" : source;
        return d;
    }

    private String serialize(GoalEntity g) {
        return "{\"id\":" + g.id + ",\"type\":\"" + g.type + "\",\"title\":\"" + safe(g.title) + "\",\"targetDate\":\"" + (g.targetDate == null ? "" : g.targetDate) + "\",\"targetValue\":" + g.targetValue + ",\"targetUnit\":\"" + safe(g.targetUnit) + "\",\"status\":\"" + g.status + "\"}";
    }

    private String safe(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
