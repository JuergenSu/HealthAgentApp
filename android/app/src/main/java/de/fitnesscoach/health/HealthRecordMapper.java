package de.fitnesscoach.health;

import android.health.connect.datatypes.ActiveCaloriesBurnedRecord;
import android.health.connect.datatypes.HeartRateRecord;
import android.health.connect.datatypes.IntervalRecord;
import android.health.connect.datatypes.Record;
import android.health.connect.datatypes.RestingHeartRateRecord;
import android.health.connect.datatypes.SleepSessionRecord;
import android.health.connect.datatypes.WeightRecord;
import java.time.Instant;
import java.util.List;
import de.fitnesscoach.data.entity.HealthRecordEntity;

final class HealthRecordMapper {
    private HealthRecordMapper() {}

    static HealthRecordEntity map(Record record, String type) {
        HealthRecordEntity entity = new HealthRecordEntity();
        String id = record.getMetadata().getId();
        entity.recordKey = type + ":" + id;
        entity.recordType = type;
        entity.externalRecordId = id;
        entity.sourcePackage = record.getMetadata().getDataOrigin().getPackageName();
        entity.lastModifiedTime = record.getMetadata().getLastModifiedTime();
        if (record instanceof IntervalRecord) {
            entity.startTime = ((IntervalRecord) record).getStartTime();
            entity.endTime = ((IntervalRecord) record).getEndTime();
        } else if (record instanceof WeightRecord) {
            entity.startTime = ((WeightRecord) record).getTime();
            entity.endTime = entity.startTime;
        } else if (record instanceof RestingHeartRateRecord) {
            entity.startTime = ((RestingHeartRateRecord) record).getTime();
            entity.endTime = entity.startTime;
        }
        if (record instanceof WeightRecord) {
            entity.value1 = ((WeightRecord) record).getWeight().getInGrams() / 1000.0;
        } else if (record instanceof RestingHeartRateRecord) {
            entity.value1 = (double) ((RestingHeartRateRecord) record).getBeatsPerMinute();
        } else if (record instanceof ActiveCaloriesBurnedRecord) {
            entity.value1 = ((ActiveCaloriesBurnedRecord) record).getEnergy().getInCalories();
        } else if (record instanceof SleepSessionRecord) {
            entity.value1 = (double) (((SleepSessionRecord) record).getEndTime().toEpochMilli()
                    - ((SleepSessionRecord) record).getStartTime().toEpochMilli()) / 60000.0;
        } else if (record instanceof HeartRateRecord) {
            List<HeartRateRecord.HeartRateSample> samples = ((HeartRateRecord) record).getSamples();
            entity.sampleCount = samples.size();
            if (!samples.isEmpty()) {
                long sum = 0;
                long max = Long.MIN_VALUE;
                for (HeartRateRecord.HeartRateSample sample : samples) {
                    sum += sample.getBeatsPerMinute();
                    max = Math.max(max, sample.getBeatsPerMinute());
                }
                entity.value1 = sum / (double) samples.size();
                entity.value2 = (double) max;
            }
        }
        return entity;
    }
}
