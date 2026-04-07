package com.wisdometer.di

import android.content.Context
import androidx.room.Room
import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.db.MIGRATION_1_2
import com.wisdometer.data.db.WisdometerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WisdometerDatabase =
        Room.databaseBuilder(context, WisdometerDatabase::class.java, "wisdometer.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providePredictionDao(db: WisdometerDatabase): PredictionDao = db.predictionDao()
}
