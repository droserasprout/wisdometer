package com.wisdometer.di

import com.wisdometer.data.repository.PredictionRepository
import com.wisdometer.data.repository.PredictionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindPredictionRepository(impl: PredictionRepositoryImpl): PredictionRepository
}
