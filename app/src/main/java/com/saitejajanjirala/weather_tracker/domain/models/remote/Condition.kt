package com.saitejajanjirala.weather_tracker.domain.models.remote


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Condition(
    @Json(name = "code")
    val code: Int? = null,
    @Json(name = "icon")
    val icon: String? = null,
    @Json(name = "text")
    val text: String? = null
)