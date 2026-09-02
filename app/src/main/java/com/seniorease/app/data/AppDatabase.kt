package com.seniorease.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfile::class,
        EmergencyContact::class,
        PlaceEntity::class,
        CustomAction::class,
        AppSettings::class,
        DocumentEntity::class,
        NoteEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create places table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `places` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `phoneNumber` TEXT,
                        `notes` TEXT,
                        `isEmergency` INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Copy hospitals data
                db.execSQL("""
                    INSERT INTO `places` (`name`, `type`, `address`, `phoneNumber`, `notes`, `isEmergency`)
                    SELECT name, 'HOSPITAL', address, phoneNumber, 'Ambulance: ' || ambulanceNumber, 1 FROM hospitals
                """)
                
                // Copy saved_locations data
                db.execSQL("""
                    INSERT INTO `places` (`name`, `type`, `address`, `phoneNumber`, `notes`, `isEmergency`)
                    SELECT label, 'OTHER', address, NULL, NULL, 0 FROM saved_locations
                """)
                
                // Drop old tables
                db.execSQL("DROP TABLE IF EXISTS hospitals")
                db.execSQL("DROP TABLE IF EXISTS saved_locations")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN cardOrderJson TEXT NOT NULL DEFAULT 'EMERGENCY,CALLS,HOSPITAL,CAB,DOCUMENTS'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "seniorease_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
