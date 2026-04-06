package com.wisdometer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption

@Database(
    entities = [Prediction::class, PredictionOption::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class WisdometerDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao
}
