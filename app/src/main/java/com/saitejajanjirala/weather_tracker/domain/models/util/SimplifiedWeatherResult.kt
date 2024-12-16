package com.saitejajanjirala.weather_tracker.domain.models.util

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SimplifiedWeatherResult(
    @Json(name = "location_name")
    val locationName: String? = null,
    @Json(name = "temp_c")
    val tempC: Double? = null,
    @Json(name = "feelslike_c")
    val feelslikeC: Double? = null,
    @Json(name = "humidity")
    val humidity: Int? = null,
    @Json(name = "uv")
    val uv: Double? = null,
    @Json(name = "icon")
    val icon: String? = null
):Parcelable