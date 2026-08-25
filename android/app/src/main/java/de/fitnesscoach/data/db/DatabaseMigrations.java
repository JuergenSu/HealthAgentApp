package de.fitnesscoach.data.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class DatabaseMigrations {
    private DatabaseMigrations() {}

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `health_records` (`recordKey` TEXT NOT NULL, `recordType` TEXT NOT NULL, `externalRecordId` TEXT, `sourcePackage` TEXT, `startTime` TEXT, `endTime` TEXT, `value1` REAL, `value2` REAL, `sampleCount` INTEGER, `lastModifiedTime` TEXT, PRIMARY KEY(`recordKey`))");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_recordType` ON `health_records` (`recordType`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_startTime` ON `health_records` (`startTime`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `health_daily_aggregates` (`date` TEXT NOT NULL, `metric` TEXT NOT NULL, `value` REAL, `calculatedAt` TEXT, PRIMARY KEY(`date`, `metric`))");
            database.execSQL("CREATE TABLE IF NOT EXISTS `health_sync_state` (`id` INTEGER NOT NULL, `initialImportStart` TEXT, `lastAttemptAt` TEXT, `lastSuccessfulSyncAt` TEXT, `lastError` TEXT, PRIMARY KEY(`id`))");
        }
    };

    public static final Migration[] ALL = new Migration[] {MIGRATION_1_2};
}
