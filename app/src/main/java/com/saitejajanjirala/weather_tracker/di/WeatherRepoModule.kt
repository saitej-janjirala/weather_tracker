package com.saitejajanjirala.weather_tracker.di

import com.saitejajanjirala.weather_tracker.data.repo.WeatherRepoImpl
import com.saitejajanjirala.weather_tracker.domain.repo.WeatherRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherRepoModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepo(
        weatherRepoImpl: WeatherRepoImpl
    ): WeatherRepo

}
