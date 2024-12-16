package com.saitejajanjirala.weather_tracker.domain.models.remote


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResult(
    @Json(name = "current")
    val current: Current? = null,
    @Json(name = "location")
    val location: Location? = null
)