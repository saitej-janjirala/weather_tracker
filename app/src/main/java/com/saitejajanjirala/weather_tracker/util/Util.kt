package com.saitejajanjirala.weather_tracker.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.saitejajanjirala.weather_tracker.SimplifiedWeatherResultProto
import com.saitejajanjirala.weather_tracker.data.local.SimplifiedWeatherResultSerializer
import com.saitejajanjirala.weather_tracker.domain.models.remote.WeatherResult
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult

object Util {

    const val API_BASE_URL = "https://api.weatherapi.com/v1/"

    fun ConnectivityManager.isInternetAvailable():Boolean{
        val network = this.activeNetwork ?: return false
        val capabilities = this.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    fun WeatherResult.mapToSimplifiedWeatherResult(): SimplifiedWeatherResult {
        return SimplifiedWeatherResult(
            locationName = this.location?.name,
            tempC = this.current?.tempC,
            feelslikeC = this.current?.feelslikeC,
            humidity = this.current?.humidity,
            uv = this.current?.uv,
            icon = this.current?.condition?.icon
        )
    }
    val Context.simplifiedWeatherDataStore: DataStore<SimplifiedWeatherResultProto> by dataStore(
        fileName = "simplified_weather_result.pb",
        serializer = SimplifiedWeatherResultSerializer
    )
    fun SimplifiedWeatherResult.toProtoModel(): SimplifiedWeatherResultProto {
        return SimplifiedWeatherResultProto.newBuilder()
            .setLocationName(this.locationName ?: "")
            .setTempC(this.tempC ?: 0.0)
            .setFeelslikeC(this.feelslikeC ?: 0.0)
            .setHumidity(this.humidity ?: 0)
            .setUv(this.uv ?: 0.0)
            .setIcon(this.icon ?: "")
            .build()
    }
    fun SimplifiedWeatherResultProto.toModel(): SimplifiedWeatherResult {
        return SimplifiedWeatherResult(
            locationName = this.locationName,
            tempC = this.tempC,
            feelslikeC = this.feelslikeC,
            humidity = this.humidity,
            uv = this.uv,
            icon = this.icon
        )
    }
}