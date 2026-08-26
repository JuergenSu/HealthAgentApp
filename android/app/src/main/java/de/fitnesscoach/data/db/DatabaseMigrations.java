package de.fitnesscoach.data.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class DatabaseMigrations {
    private DatabaseMigrations() {}
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `health_records` (`recordKey` TEXT NOT NULL, `recordType` TEXT NOT NULL, `externalRecordId` TEXT, `sourcePackage` TEXT, `startTime` TEXT, `endTime` TEXT, `value1` REAL, `value2` REAL, `sampleCount` INTEGER, `lastModifiedTime` TEXT, PRIMARY KEY(`recordKey`))");
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_recordType` ON `health_records` (`recordType`)"); database.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_startTime` ON `health_records` (`startTime`)");
        database.execSQL("CREATE TABLE IF NOT EXISTS `health_daily_aggregates` (`date` TEXT NOT NULL, `metric` TEXT NOT NULL, `value` REAL, `calculatedAt` TEXT, PRIMARY KEY(`date`, `metric`))");
        database.execSQL("CREATE TABLE IF NOT EXISTS `health_sync_state` (`id` INTEGER NOT NULL, `initialImportStart` TEXT, `lastAttemptAt` TEXT, `lastSuccessfulSyncAt` TEXT, `lastError` TEXT, PRIMARY KEY(`id`))"); }};
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `stepsQuality` TEXT"); database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `distanceQuality` TEXT"); database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `activeCaloriesQuality` TEXT"); database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `sleepQuality` TEXT"); database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `restingHeartRateQuality` TEXT"); database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `averageHeartRateQuality` TEXT"); database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `weightQuality` TEXT"); database.execSQL("ALTER TABLE `daily_health` ADD COLUMN `exerciseMinutesQuality` TEXT"); }};
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `preferredSports` TEXT"); }};
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE `goals` ADD COLUMN `startingValue` REAL"); database.execSQL("ALTER TABLE `goals` ADD COLUMN `startingUnit` TEXT"); database.execSQL("ALTER TABLE `goals` ADD COLUMN `startingSituation` TEXT"); }};
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) { @Override public void migrate(@NonNull SupportSQLiteDatabase database) { database.execSQL("ALTER TABLE `workouts` ADD COLUMN `reviewNote` TEXT"); }};
    public static final Migration[] ALL = new Migration[] {MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6};
}
