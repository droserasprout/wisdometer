package com.wisdometer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE predictions ADD COLUMN description TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE predictions ADD COLUMN updated_at INTEGER")
    }
}

@Database(
    entities = [Prediction::class, PredictionOption::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class WisdometerDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao
}
