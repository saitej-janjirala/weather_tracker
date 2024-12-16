package com.saitejajanjirala.weather_tracker.domain.repo

import com.saitejajanjirala.weather_tracker.domain.models.remote.WeatherResult
import com.saitejajanjirala.weather_tracker.domain.models.util.Result
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepo {
    suspend fun getWeather(location: String): Flow<Result<SimplifiedWeatherResult>>
    suspend fun getWeather(): Flow<Result<SimplifiedWeatherResult>>
}
