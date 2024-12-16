package com.saitejajanjirala.weather_tracker.domain.models.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Current(
    val condition: Condition? = null,
    @Json(name = "feelslike_c")
    val feelslikeC: Double? = null,
    @Json(name = "temp_c")
    val tempC: Double? = null,
    @Json(name = "uv")
    val uv: Double? = null,
    @Json(name = "humidity")
    val humidity: Int? = null,
)