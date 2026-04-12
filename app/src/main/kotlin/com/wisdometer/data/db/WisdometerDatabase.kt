package com.wisdometer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption

@Database(
    entities = [Prediction::class, PredictionOption::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class WisdometerDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao

    companion object {
        /** Rename probability (0-100) → weight (1-10). Table recreation for Android 8+ compat. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE prediction_options_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        predictionId INTEGER NOT NULL,
                        label TEXT NOT NULL,
                        weight INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        FOREIGN KEY(predictionId) REFERENCES predictions(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO prediction_options_new (id, predictionId, label, weight, sortOrder)
                    SELECT id, predictionId, label,
                           MAX(1, MIN(10, CAST(ROUND(probability / 10.0) AS INTEGER))),
                           sortOrder
                    FROM prediction_options
                """.trimIndent())
                db.execSQL("DROP TABLE prediction_options")
                db.execSQL("ALTER TABLE prediction_options_new RENAME TO prediction_options")
                db.execSQL("CREATE INDEX index_prediction_options_predictionId ON prediction_options(predictionId)")
            }
        }
    }
}
