package com.saitejajanjirala.weather_tracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.saitejajanjirala.weather_tracker.SimplifiedWeatherResultProto
import com.saitejajanjirala.weather_tracker.domain.models.remote.WeatherResult
import com.saitejajanjirala.weather_tracker.util.Util.simplifiedWeatherDataStore
import com.squareup.moshi.Json
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class DataStoreManager @Inject constructor(private val context: Context)  {
    companion object {
        val WEATHER_KEY = stringPreferencesKey("weather_data")
    }

    val weatherFlow: Flow<SimplifiedWeatherResultProto> = context.simplifiedWeatherDataStore.data

    suspend fun saveWeather(weatherResult: SimplifiedWeatherResultProto) {
        context.simplifiedWeatherDataStore.updateData { weatherResult }
    }
}