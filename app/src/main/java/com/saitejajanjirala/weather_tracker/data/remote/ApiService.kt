package com.saitejajanjirala.weather_tracker.data.remote

import com.saitejajanjirala.weather_tracker.domain.models.remote.WeatherResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("current.json")
    suspend fun getWeather(@Query("q") location : String): Response<WeatherResult>
}